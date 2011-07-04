package untyped

import com.google.javascript.jscomp._

import sbt._

trait ClosureCompilerConfig extends MavenStyleWebScalaPaths {

  // Configuration ------------------------------

  // This is a convenience method from Project.
  def descendents(parent: PathFinder, include: FileFilter): PathFinder

  /**
   * Returns true if the path refers to a file that should be templated
   *
   * It is templated if the file name contains .template. E.g. foo.template.js
   */
  def closureJsIsTemplated(path: Path): Boolean = path.name.contains(".template")

  /**
   * Where we should look to find properties files that supply values we use when templating
   */
  def closurePropertiesPath: Path = mainResourcesPath

  def closureSourcePath: Path = webappPath

  def closureJsSourceFilter: NameFilter = GlobFilter("*.js")
  def closureJsSources: PathFinder = descendents(closureSourcePath, closureJsSourceFilter)
  
  def closureManifestSourceFilter: NameFilter = GlobFilter("*.jsm") | "*.jsmanifest"
  def closureManifestSources: PathFinder = descendents(closureSourcePath, closureManifestSourceFilter)
  
  def closureOutputPath: Path = (outputPath / "sbt-closure-temp") ##
  
  var _closurePrettyPrint = false
  def closurePrettyPrint = _closurePrettyPrint
  
  var _closureVariableRenamingPolicy = VariableRenamingPolicy.LOCAL
  def closureVariableRenamingPolicy = _closureVariableRenamingPolicy
  
  def closureCompilerOptions = {
    val options = new CompilerOptions
    options.variableRenaming = closureVariableRenamingPolicy
    options.prettyPrint = closurePrettyPrint
    options
  }  

}
