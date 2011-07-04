package untyped

import org.scalatest._
import sbt._
import java.io.File

class ManifestObjectSuite extends FunSuite {

  test("ManifestUrl.content"){
    val url = ManifestUrl("http://www.untyped.com/")

    assert(url.content.contains("Untyped"))
  }

  test("ManifestUrl filenames"){
    expect("http___untyped.com_"){ ManifestUrl("http://untyped.com/").filename }
    expect("http___code.jquery.com_jquery_1.5.1.js"){ 
      ManifestUrl("http://code.jquery.com/jquery-1.5.1.js").filename
    }
  }

  test("ManifestFile path where filename contains forward slashes"){
    expect(Path.fromFile(new File("foo")) / "bar" / "baz.js"){
      ManifestFile("bar/baz.js").path(Path.fromFile(new File("foo")))
    }
  }
}
