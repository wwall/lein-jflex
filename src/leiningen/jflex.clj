(ns leiningen.jflex
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as sh]
	    [clojure.string :as str]
            [leiningen.core.main :as main])
  (:import [java.io File]))

(defn- stale-jflex-targets
  "Returns a lazy seq of [source, compiled] tuples for every jflex source file
within `dirs` modified since it was most recently compiled."
  [dirs compile-path]
  (for [dir dirs
        ^File source (filter #(-> ^File % (.getName) (.endsWith ".jflex"))
                             (file-seq (io/file dir)))
        :let [rel-source (.substring (.getPath source) (inc (count dir)))
              rel-compiled (.substring rel-source 0 (- (count rel-source) 3))
              compiled (io/file compile-path rel-compiled)]
        :when (>= (.lastModified source) (.lastModified compiled))]
    [source compiled]))

(defn- jflex-command
  "Compile all sources of possible options and add important defaults."
  [project args source compiled]
      (concat ["jflex"]
	      ["-d"  (.getPath compiled)   (.getPath source) ]))

(defn run-jflex-target
  [project args [source compiled]]
  (.mkdirs (.getParentFile compiled))
  (let [command (jflex-command project args source compiled)
        {:keys [exit out err]} (apply sh/sh command)]
    (when-not (.isEmpty out) (print out))
    (when-not (.isEmpty err) (print err))
    (when-not (zero? exit)
      (main/abort "lein-jflex: `jflex` execution failed"
                  "with status code" exit))))

(defn- run-jflex-task
  "Run jflex to compile all source file in the project."
  [project args]
  (let [compile-path (:jflex-compile-path project)
        source-paths (:jflex-source-paths project)
        targets (stale-jflex-targets source-paths compile-path)]
    (when (seq targets)
      (main/info "Compiling" (count targets) "jflex source files to" compile-path)
      (dorun (map (partial run-jflex-target project args) targets)))))

(defn jflex-defaults
  [project]
  (let [compile-path (.getPath (io/file (:target-path project) "jflex"))]
    {:jflex-compile-path compile-path
     :jflex-java-extension "java"
     :jflex-options []
     :jflex-command "jflex"}))

(defn jflex
  "Compile jflex source files.

Add a :jflex-source-paths key to project.clj to specify where to find them.
Will compile the files to :jflex-compile-path, which defaults to 'target/jflex'.
You will probably want to add :jflex-compile-path to your :java-source-paths.
Provide :jflex-command to specify the command used to run jflex (default
'jflex') and :jflex-options for any additional arguments to pass to each jflex
command instance.

Any options passed to the task will also be passed to each jflex command
instance."
  [project & args]
  (let [project (merge (jflex-defaults project) project)]
    (run-jflex-task project args)))
