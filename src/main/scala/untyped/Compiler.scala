package untyped

import com.google.javascript.jscomp.{Compiler => ClosureCompiler, CompilerOptions, JSError, JSSourceFile}

import sbt._

class Compiler(options: CompilerOptions = new CompilerOptions) {

  def compile(sources: List[File], externs: List[File], target: File, log: Logger): Unit = {
    val compiler = new ClosureCompiler

    val result = compiler.compile(
      externs.map(JSSourceFile.fromFile _).toArray,
      sources.map(JSSourceFile.fromFile _).toArray,
      options
    )

    val errors = result.errors.toList
    val warnings = result.warnings.toList

    if (!errors.isEmpty) {
      //log.error(errors.length + " errors compiling " + manifestPath.name + ":")
      errors.foreach { (err: JSError) => log.error(err.toString) }

      //Some("Failed to compile " + manifestPath.name)
    } else {
      if (!warnings.isEmpty) {
        //log.warn(warnings.length + " warnings compiling " + manifestPath.name + ":")
        warnings.foreach { (err: JSError) => log.warn(err.toString) }
      }

      IO.createDirectory(file(target.getParent))
      IO.write(target, compiler.toSource)
      //None
    }
  }
}