name := "sbt-keychain-credentials"

description := "Adds com.xvyg.sbt.keychain.Credentials object capable of loading passwords from Mac OS X Keychain"

sbtPlugin := true

scalaVersion := "2.12.6"

organization in ThisBuild := "com.xvyg"
licenses in ThisBuild += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

enablePlugins(GitVersioning)

publishMavenStyle in ThisBuild := false
bintrayRepository in ThisBuild := "sbt-plugins"
bintrayOrganization in bintray := None

crossSbtVersions := Vector("0.13.16", "1.1.2")
