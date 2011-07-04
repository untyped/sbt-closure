package untyped

import org.scalatest._

class ManifestOpsSuite extends FunSuite {

  import ManifestOps._

  test("stripComments") {
    expect(""){ stripComments("#this and that") }
    expect(""){ stripComments("  #this and that") }
    expect(""){ stripComments("#") }
    expect(""){ stripComments("###") }

    expect("this and that"){ stripComments("this and that  #this and that") }
    expect("this and that"){ stripComments("this and that") }
  }

  test("isUrl") {
    expect(true){ isUrl("http://www.untyped.com/") }
    expect(true){ isUrl("https://www.untyped.com/") }
    expect(true){ isUrl("   http://www.untyped.com/  ") }

    expect(false){ isUrl("foobar") }
    expect(false){ isUrl("untyped.com") }
    expect(false){ isUrl("#http://www.untyped.com/") }
  }

  test("parse") {
    expect(List(ManifestFile("foo.js"))){ parse("foo.js") }
    expect(List(ManifestFile("foo.js"), 
                ManifestUrl("http://untyped.com/"),
                ManifestFile("bar.js"))){
      parse("""foo.js
#A Comment
http://untyped.com/ #T3h best website evar!

bar.js""")
    }
  }
}
