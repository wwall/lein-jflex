# lein-jflex

A Leiningen plugin to compile jflex specification  to Java source files.

## Usage

Put `[lein-jflex "0.1.0"]` into the `:plugins` vector of your `project.clj` and
set `:jflex` as vector oof map. Each map must have two keys :file and :output-file


Then compile any JFlex source files to Java source files by running:

    $ lein jflex


Example of project.clj

```clj
(defproject example-project "0.1.0-SNAPSHOT"
  ...
  :plugins [[lein-jflex "0.1.0"]]
  :prep-tasks ["jflex" "javac"]
  :jflex [
    {:file "src/jflex/BSLLexer.jflex"
     :output-file "src/java/riki/bsl/lexer/BSLLexer.java"}]
  ...)
```



## License
Copyright © 2025 Bombin Valentin

Distributed under the Eclipse Public License, the same as Clojure.
