package untyped

import java.io.File

object ManifestOps {
  def stripComments(line: String) = "#.*$".r.replaceAllIn(line, "").trim
  def isSkippable(line: String): Boolean = stripComments(line) == ""
  def isUrl(line: String): Boolean = stripComments(line).matches("^https?:.*")

  def parse(manifest: String): List[ManifestObject] = 
    manifest.split("[\r\n]+").
             map(stripComments _).
             filter(item => !isSkippable(item)).
             map(line => if(isUrl(line)) ManifestUrl(line) else ManifestFile(line)).
             toList

}
