SBT Closure Plugin
==================

[Simple Build Tool] plugin for compiling Javascript filesfrom multiple sources using Google's [Closure compiler].

Copyright (c) 2011 [Dave Gurnell] of [Untyped].

[Simple Build Tool]: http://simple-build-tool.googlecode.com
[Closure compiler]: http://code.google.com/p/closure-compiler
[Dave Gurnell]: http://boxandarrow.com
[Untyped]: http://untyped.com

Usage
=====

First, create a `project/plugins/Plugins.scala` file and paste the following 
content into it:

    import sbt._

    class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
      val untypedRepo = "Untyped Repo" at "http://repo.untyped.com"
      val closureCompiler = "untyped" % "sbt-closure" % "0.4"
    }

This will give you the ability to use the plugin in your project file. For example:

    import sbt._
    
    class MyProject(info: ProjectInfo) extends DefaultWebProject(info)
      with untyped.ClosureCompilerPlugin {
    
      // and so on...
    
    }

The default behaviour of the plugin is to scan your `src/main/webapp` directory
and look for files of extension `.jsmanifest`, or `.jsm` for short. These files
should contain ordered lists of JavaScript source locations. For example:

    # You can specify remote files using URLs...
    http://code.jquery.com/jquery-1.5.1.js
    
    # ...and local files using regular paths
    #    (relative to the location of the manifest):
    lib/foo.js
    bar.js
    
    # Blank lines and bash-style comments are also supported.
    # These may be swapped for JS-style comments in the future.

The plugin compiles this in two phases: first, it downloads and caches any
remote scripts. Second, it feeds all of the specified scripts into the Closure
compiler. The compiler outputs a file of the same name and relative path
of the manifest, but with a `.js` extension. For example, if your manifest
file is at `webapps/static/js/kitchen-sink.jsm` in the source tree, the final 
path would be `webapps/static/js/kitchen-sink.js` in the target tree.

If, on compilation, the plugin finds remote scripts already cached on your
filesystem, it won't try to download them again. Running `sbt clean` will
delete the cache.

You can change the compiler options by overriding the `closureCompilerOptions`
method. See the source for details.

Finally, you can execute the plugin's compilation step independently of
`prepare-webapp` using `sbt compile-js`.

Templating
================

It is sometime useful to template Javascript files. For example, you might want
scripts to refer to localhost while developing and your live server when
deployed. This plugin supports templating Javascript files using the [Mustache]
format and [Lift style properties] (though the implementation has no dependency
on Lift).

In summary, properties are looked for in =src/main/resources/prop= (by default;
see below for customization). They are in the standard Java format. If you
aren't interested in changing your properties depending on your build
configuration just place the properties in =default.props=. Otherwise property
files should be named =modeName.props=, where modeName is the setting of the
=run.mode= system property, which can take on values of =test=, =staging=,
=production=, =pilot=, or =default=.. If =run.mode= is not set, =default= is
assumed.

Any Javascript file that contains =.template= will be passed through a Mustache
template processor before being processed by the Google compiler.

Parameters controlling templating are:

   - =closureJsIsTemplated= is function that indicates if a given Javascript
     file should be run through the template processor

   - =closurePropertiesPath= determines where properties are found

[Mustache]: http://mustache.github.com/
[Lift style]: http://www.assembla.com/spaces/liftweb/wiki/Properties

Acknowledgements
================

Based on the [Coffee Script SBT plugin], Copyright (c) 2010 Luke Amdor.

Heavily influenced by the [YUI Compressor SBT plugin] by Jon Hoffman.

[Coffee Script SBT plugin]: https://github.com/rubbish/coffee-script-sbt-plugin
[YUI Compressor SBT plugin]: https://github.com/hoffrocket/sbt-yui

Licence
=======

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
