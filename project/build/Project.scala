import sbt._

import java.io.File

class Project(info: ProjectInfo) extends PluginProject(info) with test.ScalaScripted {
  
  val closure = "com.google.javascript" % "closure-compiler" % "r706"
  val jtache  = "com.samskivert" % "jmustache" % "1.3"
  val testing = "org.scalatest" % "scalatest" % "1.1"

  override def scriptedSbt = "0.7.4"
  override def scriptedBufferLog = false

  override def testAction = testNoScripted

  lazy val default = scripted dependsOn(publishLocal) describedAs("Publishes locally and tests against example projects")
  
  // This doesn't work for inexplicable reasons
  // lazy val untypedResolver = {
  //   def OptionOf[T](v: T): Option[T] = {
  //     if (v == null) 
  //       None
  //     else
  //       Some(v)
  //   }

  //   for { host <- OptionOf(System.getenv("DEFAULT_REPO_HOST"))
  //         path <- OptionOf(System.getenv("DEFAULT_REPO_PATH"))
  //         user <- OptionOf(System.getenv("DEFAULT_REPO_USER"))
  //         keyfile <- OptionOf(System.getenv("DEFAULT_REPO_KEYFILE")) 
  //   } yield Resolver.sftp("Default Repo", host, path).as(user, new java.io.File(keyfile))
  // }

  lazy val publishTo = Resolver.sftp("Default",
                                     System.getenv("DEFAULT_REPO_HOST"),
                                     System.getenv("DEFAULT_REPO_PATH")).
                        as(System.getenv("DEFAULT_REPO_USER"), 
                           new File(System.getenv("DEFAULT_REPO_KEYFILE"))) 

}
