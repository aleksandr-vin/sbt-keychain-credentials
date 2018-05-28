package com.xvyg.sbt.keychain

import java.io.FileInputStream
import java.util.Properties
import sys.process.Process

object Credentials {

  def apply(file: java.io.File, logger: sbt.Logger = Credentials.Logger): sbt.Credentials = {
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
      val process = Process(s"security find-generic-password -a $user -s $host -w").lineStream_!
      if (process.length == 1) {
        sbt.Credentials(realm, host, user, process.head)
      } else {
        logger.error("Problem occurred while obtaining password from system's keychain")
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
      logger.warn(s"   security add-generic-password -a $user -s $host -w")
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
