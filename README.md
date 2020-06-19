# lein-jflex

A Leiningen plugin to compile jflex specification  to Java source files.

## Usage

Put `[lein-jflex "0.1.0"]` into the `:plugins` vector of your `project.clj` and
set `:jflex-source-paths` to the path to your Ragel source files.

Then compile any JFlex source files to Java source files by running:

    $ lein jflex

For optimal usefulness, you will probably want to include `:jflex-compile-path`
(default `target/jflex`) in your `:java-source-paths` and `jflex` in your
`:prep-tasks`.  Example:

```clj
(defproject example-project "0.1.0-SNAPSHOT"
  ...
  :plugins [[lein-jflex "0.1.0"]]
  :java-source-paths ["target/jflex"]
  :jflex-source-paths ["src/jflex"]
  :prep-tasks ["jflex" "javac"]
  ...)
```

You may specify the Jflex command to use with `:jflex-command` and any other
options to pass to Jflex with `:jflex-options`.

## License
Copyright © 20120 Bombin Valentin

Distributed under the Eclipse Public License, the same as Clojure.
