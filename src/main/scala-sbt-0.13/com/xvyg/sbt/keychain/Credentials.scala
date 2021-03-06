package com.xvyg.sbt.keychain

import java.io.FileInputStream
import java.util.Properties

import scala.util.{Failure, Success, Try}
import sys.process.Process

object Credentials {

  sealed trait KeychainType {
    def command(user: String, host: String): String
    def createMessage(user: String, host: String, realm: String): String
    def errorMessage: String
  }
  case object MAC extends KeychainType {
    override def command(user:String, host: String) = s"security find-generic-password -a $user -s $host -w"
    override def createMessage(user: String, host: String, realm: String) = s"security add-generic-password -a $user -s $host -w"
    override val errorMessage = "Problem occurred while obtaining password from system's keychain"
  }
  case object GNOME extends KeychainType {
    override def command(user:String, host: String) = s"secret-tool lookup $host $user"
    override def createMessage(user: String, host: String, realm: String) = s"secret-tool store  --label '$realm' $host $user"
    override val errorMessage = "Problem occurred while obtaining password from system's keychain, make sure that 'secret-tool' is installed"
  }

  def apply(file: java.io.File, logger: sbt.Logger = Credentials.Logger, keychainType: KeychainType = MAC): sbt.Credentials = {
    logger.info(s"Reading credentials from $file ...")
    val (realm, host, user, password) =
      try {
        val prop = new Properties()
        prop.load(new FileInputStream(file))
        (
          prop.getProperty("realm"),
          prop.getProperty("host"),
          prop.getProperty("user"),
          prop.getProperty("password")
        )
      } catch {
        case e: Exception =>
          e.printStackTrace()
          sys.exit(1)
      }
    if (password == null || password.trim == "") {
      logger.info("Obtaining password from system's keychain ...")
      Try(Process(keychainType.command(user, host)).lineStream_!) match {
        case Success(p) if p.length == 1 => sbt.Credentials(realm, host, user, p.head)
        case Failure(_) | Success(_) =>
          logger.error(keychainType.errorMessage)
          sys.exit(1)
      }
    } else {
      logger.warn("")
      logger.warn("************************************************************************")
      logger.warn("  Password appears to be stored as plain text in the file!")
      logger.warn(s"    $file")
      logger.warn("")
      logger.warn("  Consider removing it from that file and storing in system's Keychain.")
      logger.warn("  You can use the command below for this:")
      logger.warn("")
      logger.warn(s"   ${keychainType.createMessage(user, host, realm)}")
      logger.warn("************************************************************************")
      logger.warn("")
      sbt.Credentials(realm, host, user, password)
    }
  }

  private object Logger extends sbt.Logger {
    def trace(t: => Throwable): Unit = println(t)
    def success(message: => String): Unit = println(message)
    def log(level: sbt.Level.Value, message: => String): Unit = println(message)
  }
}
