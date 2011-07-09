package untyped

// Copyright 2011 Untyped Ltd
// Contact noel@untyped.com
//
// Released under the terms of the MIT License available here:
//   http://www.opensource.org/licenses/mit-license.php

import java.io.{File,FileInputStream}
import java.net.InetAddress
import java.util.Properties

// Heavily based on Lift's Props. Reimplemented here as
// there were a few things in Lift's properties handling
// that made it hard to reuse.

/**
 * Enumeration of available run modes.
 */
object RunModes extends Enumeration {
  val Development = Value(1, "Development")
  val Test = Value(2, "Test")
  val Staging = Value(3, "Staging")
  val Production = Value(4, "Production")
  val Pilot = Value(5, "Pilot")
  val Profile = Value(6, "Profile")
}

class Props(val basePath: File, runMode: String, user: String) {
  import RunModes._

  def addDot(s: String):String = s match {
    case null | "" => s
    case _ => s + "."
  }

  implicit def file2String(in: File) = in.getCanonicalPath
  
  lazy val mode = runMode.toLowerCase match {
    case "test" => Test
    case "staging" => Staging
    case "production" => Production
    case "pilot" => Pilot
    case "profile" => Profile
    case _ => Development
  }
  
  //println("mode: %s".format(mode.toString))
    
  lazy val modeName = if (mode == Development) "" else addDot(mode.toString.toLowerCase)
  lazy val userName = addDot(user.toLowerCase)
  lazy val hostName = addDot(InetAddress.getLocalHost.getHostName.toLowerCase)

  // Lift has weird behaviour that we replicate:
  //
  // If the mode is development, the modename is dropped from the
  // search.  The 'spec' doesn't say anything about this but
  // it's in the implementation so we do it
  lazy val searchPaths: List[String] = {
    List(
      basePath + "/props/" + modeName + userName + hostName + "props",
      basePath + "/props/" + modeName + userName + "props",
      basePath + "/props/" + modeName + hostName + "props",
      basePath + "/props/" + modeName + "default." + "props",
      basePath + "/" + modeName + userName + hostName + "props",
      basePath + "/" + modeName + userName + "props",
      basePath + "/" + modeName + hostName + "props",
      basePath + "/" + modeName + "default." + "props")
  }

  lazy val properties: Option[Properties] = {
    searchPaths.find(p => new File(p).exists()).map{ propFile =>
      val props = new Properties()
      props.load(new FileInputStream(new File(propFile)))
      props
    }
  }
}
