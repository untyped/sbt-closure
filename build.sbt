sbtPlugin := true

name := "sbt-closure"

organization := "untyped"

version <<= sbtVersion(v =>
  if(v.startsWith("0.11")) "0.6-SNAPSHOT"
  else error("unsupported sbt version %s" format v)
)

libraryDependencies += "com.google.javascript" % "closure-compiler" % "r1459"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.6.1" % "test"

seq(scriptedSettings:_*)

scalacOptions ++= Seq("-deprecation", "-unchecked")