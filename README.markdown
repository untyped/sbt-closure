SBT Closure Plugin
==================

[Simple Build Tool] plugin for compiling Javascript files from multiple sources using Google's [Closure compiler].

Copyright (c) 2011 [Dave Gurnell] of [Untyped].

[Simple Build Tool]: http://simple-build-tool.googlecode.com
[Closure compiler]: http://code.google.com/p/closure-compiler
[Dave Gurnell]: http://boxandarrow.com
[Untyped]: http://untyped.com

Installation
============

For SBT 0.11:

Create a `project/plugins.sbt` file and paste the following content into it:

    resolvers += "Untyped Public Repo" at "http://repo.untyped.com"

    addSbtPlugin("untyped" % "sbt-closure" % "0.6-SNAPSHOT")

Then, in your build.sbt file, put:

    seq(closureSettings:_*)

If you're using [xsbt-web-plugin](https://github.com/siasia/xsbt-web-plugin "xsbt-web-plugin"), add the output files to the webapp with:

    // add managed resources to the webapp
    (webappResources in Compile) <+= (resourceManaged in Compile)

To change the directory that is scanned, use:

    (sourceDirectory in (Compile, ClosureKeys.closure)) <<= (sourceDirectory in Compile)(_ / "path" / "to" / "jsmfiles")

To make the closure task run with compile:

    // make compile depend on closure
    (compile in Compile) <<= compile in Compile dependsOn (ClosureKeys.closure in Compile)

The plugin is currently untested under SBT 0.10. If you manage to get it to work,
let me know and I'll update these docs.

Usage
=====

The default behaviour of the plugin is to scan your `src/main` directory
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
file is at `src/main/javascript/static/js/kitchen-sink.jsm` in the source tree, the final
path would be `resource_managed/main/static/js/kitchen-sink.js` in the target tree.

If, on compilation, the plugin finds remote scripts already cached on your
filesystem, it won't try to download them again. Running `sbt clean` will
delete the cache.

To execute the plugin's compilation step  use `sbt closure`. See above on how to make it depend on compile.

Templating - NOT IMPLEMENTED FOR SBT 0.11
================

It is sometime useful to template Javascript files. For example, you might want
scripts to refer to localhost while developing and your live server when
deployed. This plugin supports templating Javascript files using the [Mustache]
format and [Lift style properties] (though the implementation has no dependency
on Lift).

In summary, properties are looked for in `src/main/resources/prop` (by default;
see below for customization). They are in the standard `key=value` Java format. If you
aren't interested in changing your properties depending on your build
configuration just place the properties in `default.props`. Otherwise property
files should be named `modeName.props`, where modeName is the setting of the
`run.mode` system property, which can take on values of `test`, `staging`,
`production`, `pilot`, or `default`. If `run.mode` is not set, `default` is
assumed.

Any Javascript file that contains `.template` will be passed through a Mustache
template processor before being processed by the Google compiler.

Parameters controlling templating are:

   - `closureJsIsTemplated` is function that indicates if a given Javascript
     file should be run through the template processor

   - `closurePropertiesPath` determines where properties are found

[Mustache]: http://mustache.github.com/
[Lift style]: http://www.assembla.com/spaces/liftweb/wiki/Properties

Acknowledgements
================

v0.6+ for SBT 0.11 based on [less-sbt](https://github.com/softprops/less-sbt), Copyright (c) 2011 Doug Tangren.
v0.1-v0.5 for SBT 0.7 based on [Coffee Script SBT plugin], Copyright (c) 2010 Luke Amdor.

Heavily influenced by the [YUI Compressor SBT plugin] by Jon Hoffman.

[Coffee Script SBT plugin]: https://github.com/rubbish/coffee-script-sbt-plugin
[YUI Compressor SBT plugin]: https://github.com/hoffrocket/sbt-yui

SBT 0.11 migration done by [Tim Nelson](https://github.com/eltimn)

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
