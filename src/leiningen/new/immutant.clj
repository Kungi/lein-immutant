(ns leiningen.new.immutant
  "Generate an Immutant project."
  (:require [leiningen.new.templates :as templ]))

(defn immutant
  "An Immutant application project layout."
  [name]
  (let [default-render (templ/renderer "default")
        render (templ/renderer "immutant")
        data {:raw-name name
              :name (templ/project-name name)
              :namespace (templ/sanitize-ns name)
              :nested-dirs (templ/name-to-path name)
              :year (templ/year)}]
    (println "Generating a project called" name "based on the 'immutant' template.")
    (templ/->files data
             ["project.clj" (default-render "project.clj" data)]
             ["README.md" (default-render "README.md" data)]
             ["doc/intro.md" (default-render "intro.md" data)]
             [".gitignore" (default-render "gitignore" data)]
             ["src/{{nested-dirs}}/core.clj" (default-render "core.clj" data)]
             ["test/{{nested-dirs}}/core_test.clj" (default-render "test.clj" data)]
             ["immutant.clj" (render "immutant.clj" data)])))
