package untyped

import sbt._

import java.io.{File,FileReader,BufferedReader}
import java.net.URL
import java.util.Properties

import scala.io.Source

import com.google.javascript.jscomp._

import com.samskivert.mustache.{Mustache,Template}


trait ClosureCompilerPlugin extends DefaultWebProject with ClosureCompilerConfig {

  // Configuration ------------------------------

  log.debug("Closure compiler config:")
  log.debug("  - closureSourcePath           : " + closureSourcePath)
  log.debug("  - closureOutputPath           : " + closureOutputPath)
  log.debug("  - closureJsSourceFilter       : " + closureJsSourceFilter)
  log.debug("  - closureJsSources            : " + closureJsSources)
  log.debug("  - closureManifestSourceFilter : " + closureManifestSourceFilter)
  log.debug("  - closureManifestSources      : " + closureManifestSources)

  // Top-level stuff ----------------------------

  lazy val compileJs = task { args =>
    if (args.length == 1)
      compileJsActionConstructor(args(0))
    else
      compileJsActionConstructor(closureRunMode)
  }

  def compileJsActionConstructor(runMode: String) =
    dynamic(compileJsAction(runMode)) describedAs "Compiles Javascript manifest files"

  def compileJsAction(runMode: String) = task{
    closureManifestSources.get.flatMap(m => new ManifestHelper(m, runMode).compile).toList.firstOption
  }.named("closure-compile")

  def closurePrettifyAction = task{
    _closureVariableRenamingPolicy = VariableRenamingPolicy.OFF
    _closurePrettyPrint = true
    None
  }.named("closure-prettify")

  // prepare-webapp and package tasks that allow passing the runMode on the cli
  lazy val prepareWebappGc = task { args =>
    if (args.length == 1)
      closurePrepareWebappAction(args(0))
    else
      closurePrepareWebappAction(closureRunMode)
  }

  def closurePrepareWebappAction(runMode: String) = super.prepareWebappAction.dependsOn(compileJsActionConstructor(runMode))

  lazy val packageGc = task { args =>
    if (args.length == 1)
      closurePackageAction(args(0))
    else
      closurePackageAction(closureRunMode)
  }

  def closurePackageAction(runMode: String) =
    packageWarAction(temporaryWarPath, webappUnmanaged, warPath, Nil) dependsOn(closurePrepareWebappAction(runMode)) describedAs "Creates a war file."


  override def jettyRunAction = super.jettyRunAction.dependsOn(closurePrettifyAction)
  override def prepareWebappAction = closurePrepareWebappAction(closureRunMode)
  override def extraWebappFiles = super.extraWebappFiles +++ (closureOutputPath ** "*")
  override def webappResources = super.webappResources --- closureManifestSources
  override def watchPaths = super.watchPaths +++ closureJsSources +++ closureManifestSources

  // Implementation -----------------------------

  class ManifestHelper(val manifestPath: Path, runMode: String) {

    /** Instantiate the properties used for Mustache templating and other things */
    val attributes: Properties = {
      val props = new Props(closurePropertiesPath.asFile, runMode, closureUserName)
      props.properties.getOrElse {
        log.warn("Closure Compiler Plugin: No properties found for processing Mustache templates. Looked in " + props.searchPaths)
        new Properties
      }
    }

    val outputPath: Path = toOutputPath(manifestPath)
    val directoryPath: Path = Path.fromFile(manifestPath.asFile.getParent)

    def toOutputPath(in: Path): Path = {
      // Put in output directory:
      val name0 = in.absolutePath.toString.
                     replace(closureSourcePath.absolutePath.toString,
                             closureOutputPath.absolutePath.toString)

      // Remove file extension (.jsm or .jsmanifest). We'll add .js later.
      val name1 = """[.]jsm(anifest)?$""".r.replaceAllIn(name0, "")

      /*
       * A prefix can be set in the propes file (closure.prefix)
       */
      val propPrefix = attributes.getProperty("closure.prefix", "")

      /**
       * Get the js module key to look for in the Props file. The
       * key in the props file will be prefix.path.module_name
       *
       * Eg. {propPrefix}.js.main=3 corresponds to /js/main_3.js which
       * has a manifest file of /js/main.jsm[anifest]
       */
      val modKey = (propPrefix :: name1.
        replace(closureOutputPath.absolutePath.toString, "").
        split('/').
        toList).
          filter(_.length > 0).
          mkString(".")

      val name2 = {
        val ver = attributes.getProperty(modKey, "")
        if (ver.length > 0) "%s_%s.js".format(name1, ver)
        else "%s.js".format(name1)
      }

      log.debug("name2: %s".format(name2))

      Path.fromFile(new File(name2))
    }

    log.debug("JS manifest config:")
    log.debug("  - manifestPath : " + manifestPath)
    log.debug("  - outputPath   : " + outputPath)
    log.debug("  - lines        : " + manifestObjects)
    log.debug("  - urls         : " + urlObjects)
//    log.debug("  - urlPaths     : " + urlPaths)
//    log.debug("  - sourcePaths  : " + sourcePaths)

    // Reading the manifest ---------------------

    import ManifestOps._

    // Before we can build a JS file, we have to read its manifest,
    // chop out comments, and skip blank lines:


    lazy val manifestObjects: List[ManifestObject] =
      parse(FileUtilities.readString(manifestPath.asFile, log).
                          right.
                          get)

    lazy val urlObjects: List[ManifestUrl] =
      manifestObjects.foldRight(Nil: List[ManifestUrl]){(elt, lst) =>
        elt match {
          case e:ManifestUrl => e :: lst
          case _ => lst
        }}


    // URLs -------------------------------------

    // The first part of building a JS file is downloading and caching
    // any URLs specified in the manifest:

    // def urlLines: List[String] = lines.filter(isUrl _)
    // def urls: List[URL] = urlLines.map(new URL(_))
    // def urlPaths: List[Path] = urlLines.map(linePath _)

    // Templating -------------------------------

    /**
     * By default the JMustache implementation will treat
     * variables named like.this as a two part name and look
     * for a variable called this within one called like
     * (called compound variables in the docs). This breaks
     * things with the default naming conventions for
     * Java/Lift properties so we turn it off.
     */
    lazy val compiler = Mustache.compiler().standardsMode(true)

    def renderTemplate(path: Path): String = {
      val tmpl = compiler.compile(new BufferedReader(new FileReader(path.asFile)))
      tmpl.execute(attributes)
    }

    def download(url: ManifestUrl, path: Path): Unit =
      FileUtilities.createDirectory(Path.fromFile(path.asFile.getParent), log) match {
        case Some(errorMsg) =>
          throw new Exception("Failed to download " + url.url + ": " + errorMsg)

        case None =>
          FileUtilities.write(path.asFile, url.content, log)
      }

    // Compilation ------------------------------

    // Once URLs have been downloaded and cached, we
    // concatenate everything into one big file and run it
    // through the Closure compiler

    def objectPath(obj: ManifestObject): Path =
      obj match {
        case file: ManifestFile =>
          file.path(directoryPath)

        // We download and cache ManifestUrls lines in a staging directory
        case url: ManifestUrl =>
          val outputDir = toOutputPath(directoryPath)
          val outputPath = url.path(outputDir)
          download(url, outputPath)
          outputPath
      }

    def externPaths: List[Path] = Nil

    def sourcePaths: List[Path] = manifestObjects.map(objectPath _)

    def pathToJSSourceFile(path: Path): JSSourceFile =
      if(closureJsIsTemplated(path))
        JSSourceFile.fromCode(path.asFile.getAbsolutePath, renderTemplate(path))
      else
        JSSourceFile.fromFile(path.asFile)

    def compile: Option[String] = {
      val compiler = new Compiler

      val externs = externPaths.map(pathToJSSourceFile _).toArray
      val sources = sourcePaths.map(pathToJSSourceFile _).toArray
      val options = closureCompilerOptions

      val result = compiler.compile(externs, sources, options)

      val errors = result.errors.toList
      val warnings = result.warnings.toList

      if(!errors.isEmpty) {
        log.error(errors.length + " errors compiling " + manifestPath.name + ":")
        errors.foreach { (err: JSError) => log.error(err.toString) }

        Some("Failed to compile " + manifestPath.name)
      } else {
        if(!warnings.isEmpty) {
          log.warn(warnings.length + " warnings compiling " + manifestPath.name + ":")
          warnings.foreach { (err: JSError) => log.warn(err.toString) }
        }

        FileUtilities.createDirectory(Path.fromFile(outputPath.asFile.getParent), log).
          orElse(FileUtilities.write(outputPath.asFile, compiler.toSource, log))
      }
    }

    def compileTask: Task = {
      val label = "closure-compile " + outputPath.name
      val product = outputPath from (manifestPath :: sourcePaths)

      fileTask(label, product){
        log.debug("to " + outputPath.toString)
        compile
      }.named(label)
    }

  }

}
