package untyped

import java.net.URL
import scala.io.Source
import sbt._

sealed abstract class ManifestObject {
  def filename: String
  def path(parent: Path): Path = 
    filename.split("""[/\\]""").foldLeft(parent)(_ / _)
}

case class ManifestFile(val filename: String) extends ManifestObject 

case class ManifestUrl(val url: String) extends ManifestObject {
  lazy val filename: String = """[^A-Za-z0-9.]""".r.replaceAllIn(url, "_")

  def content: String = Source.fromInputStream(new URL(url).openStream).mkString
}


