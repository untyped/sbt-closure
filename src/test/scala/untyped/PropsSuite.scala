package untyped

import java.io.{File,FileWriter}
import java.util.Properties
import org.scalatest._

class PropsSuite extends FunSuite {

  test("Props loads default.props if no other properties file found") {
    val f = new File("default.props")
    val writer = new FileWriter(f)

    writer.write("foo = bar")
    writer.close()

    println(f)
    println(f.getCanonicalFile.getParentFile)
    try {
      val props = new Props(f.getCanonicalFile.getParentFile)
      expect("bar"){ props.properties.get.getProperty("foo", "not-found") }
    } finally {
      f.delete()
    }
  }
}
