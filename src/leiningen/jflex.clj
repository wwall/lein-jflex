(ns leiningen.jflex
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [clojure.string :as str]
            [leiningen.core.main :as main])
  (:import [java.io File]))

(defn- stale-jflex-targets
  "Возвращает список пар [source, compiled] для файлов, которые нужно перегенерировать."
  [jflex-config root]
  (->> jflex-config
       (filter (fn [{:keys [file output-file]}]
                 (and file output-file))) ; Шаг 1: Фильтруем элементы с file и output-file
       (map (fn [{:keys [file output-file]}]
              (let [source (io/file root file)
                    compiled (io/file output-file)]
                [source compiled]))) ; Шаг 2: Создаем пары [source compiled]
       (filter (fn [[source compiled]]
                 (and (.exists source) ; Шаг 3: Проверяем существование source
                      (>= (.lastModified source) ; и актуальность
                          (.lastModified compiled))))) ; относительно compiled
       (into []))) ; Шаг 4: Преобразуем в вектор


(defn- jflex-command
  [source compiled]
;  (println "source:" source)
;  (println "source type:" (type source))
;  (println "source path:" (.getPath source))
  (let [output-dir (.getParent (io/file compiled))]
    ["jflex" "-d" output-dir (.getPath source)]))

(defn run-jflex-target
  [project [source compiled]]
  (main/info "source   = " (.getPath source))
  (main/info "compiled = " (.getPath compiled))
  (let [output-dir (.getParent (io/file compiled))]
    (.mkdirs (io/file output-dir)))
  (let [command (jflex-command  source  compiled)]
    (println "Running command:" command)
;    (println "Types of args:" (map type command))
    (let [
        {:keys [exit out err]} (apply sh/sh command)]
    (when-not (str/blank? out) (println out))
    (when-not (str/blank? err) (println err))
    (when-not (zero? exit)
      (main/abort "lein-jflex: `jflex` execution failed"
                  "with status code" exit)))))

(defn- run-jflex-task
  [project args]
  (let [jflex-config (get project :jflex 23)
        root (:root project)
        ]
    (when (seq jflex-config)
      (main/info "Compiling" (count jflex-config) "jflex source files.")
      (let [
            kv (stale-jflex-targets jflex-config root)]
      (dorun (map (partial run-jflex-target project) kv))))))

(defn jflex
  "Compile jflex source files.

Specify files to generate in project.clj like:

:jflex [
  {:file \"src/jflex/exer.jflex\"
   :output-file \"src/java/exer/Lexer.java\"}
  {...}]
"
  [project & args]
  (run-jflex-task project args))



;(def mock-project
;  {:name "test-project"
;   :jflex [
;     {:file "src/jflex/BSLLexer.jflex"
;      :output-file "src/java/riki/bsl/lexer/BSLLexer.java"}]
;   :root "/mnt/fast/src/clojure/riki.bsl.lexer"})
