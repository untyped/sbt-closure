package untyped

object Plugin extends sbt.Plugin {
  import sbt._
  import sbt.Keys._
  import ClosureKeys._
  import java.nio.charset.Charset

  object ClosureKeys {
    lazy val closure = TaskKey[Seq[File]]("closure", "Compiles .jsm javascript manifest files")
    lazy val charset = SettingKey[Charset]("charset", "Sets the character encoding used in file IO. Defaults to utf-8")
    lazy val downloadDirectory = SettingKey[File]("download-dir", "Directory to download ManifestUrls to")
    //lazy val compilerOptions = SettingKey[CompilerOptions]("compiler-options", "Compiler options")
  }

  def closureSettings: Seq[Setting[_]] =
    closureSettingsIn(Compile) ++ closureSettingsIn(Test)

  def closureSettingsIn(conf: Configuration): Seq[Setting[_]] =
    inConfig(conf)(Seq(
      charset in closure := Charset.forName("utf-8"),
      includeFilter in closure := ("*.jsm" || "*.jsmanifest"),
      excludeFilter in closure := (".*" - ".") || HiddenFileFilter,
      sourceDirectory in closure <<= (sourceDirectory in conf),
      resourceManaged in closure <<= (resourceManaged in conf),
      downloadDirectory in closure <<= (target in conf) { _ / "closure-downloads" },
      unmanagedSources in closure <<= closureSourcesTask,
      clean in closure <<= closureCleanTask,
      closure <<= closureCompilerTask
    )) ++ Seq(
      cleanFiles <+= (resourceManaged in closure in conf),
      watchSources <++= (unmanagedSources in closure in conf)
    )

  private def closureCleanTask =
    (streams, resourceManaged in closure) map {
      (out, target) =>
        out.log.info("Cleaning generated JavaScript under " + target)
        IO.delete(target)
    }

  private def closureCompilerTask =
    (streams, sourceDirectory in closure, resourceManaged in closure,
     includeFilter in closure, excludeFilter in closure, charset in closure,
     downloadDirectory in closure) map {
      (out, sourceDir, targetDir, incl, excl, cs, dldir) =>
        compileChanged(sourceDir, targetDir, incl, excl, cs, dldir, out.log)
    }

  private def closureSourcesTask =
    (sourceDirectory in closure, includeFilter in closure, excludeFilter in closure) map {
      (sourceDir, incl, excl) =>
         sourceDir.descendentsExcept(incl, excl).get
    }

  private def compileChanged(sources: File, target: File, include: FileFilter, exclude: FileFilter, charset: Charset, downloadDir: File, log: Logger) = {
    (for {
      manifest <- sources.descendentsExcept(include, exclude).get
      javascript <- javascript(sources, manifest, target)
      if (manifest newerThan javascript)
    } yield { (manifest, javascript) }) match {
      case Nil =>
        log.info("No JavaScript manifest files to compile")
      case xs =>
        log.info("Compiling %d jsm files to %s" format(xs.size, target))
        xs map doCompile(downloadDir, charset, log)
        log.debug("Compiled %s jsm files" format xs.size)
    }
    compiled(target)
  }

  private def doCompile(downloadDir: File, charset: Charset, log: Logger)(pair: (File, File)) = {
    val (jsm, js) = pair
    log.debug("Compiling %s" format jsm)
    val srcFiles = Manifest.files(jsm, downloadDir, charset)
    val compiler = new Compiler()
    compiler.compile(srcFiles, Nil, js, log)
  }

  private def compiled(under: File) = (under ** "*.js").get

  private def javascript(sources: File, manifest: File, targetDir: File) =
    Some(new File(targetDir, IO.relativize(sources, manifest).get.replaceAll("""[.]jsm(anifest)?$""", ".js")))
}