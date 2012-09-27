(ns leiningen.immutant.init-test
  (:use midje.sweet)
  (:require [leiningen.immutant.test-helper :as h]
            [clojure.java.io                :as io]))

(h/setup-test-env)

(doseq [generation [1 2]]
  (println "\n==> Testing with lein generation" generation)

  (let [project-name (str "testproj-" generation)]
    
    (fact (str "new should work for lein " generation)
      (h/with-tmp-dir
        (let [project-dir (io/file h/*tmp-dir* project-name)]
          (h/run-lein generation "immutant" "new" project-name
                      :dir h/*tmp-dir*
                      :env h/base-lein-env) => 0
          (.exists (io/file project-dir)) => true
          (.exists (io/file project-dir "immutant.clj")) => true)))

    (if (= 2 generation)
      (fact "'new immutant' should work with lein 2"
      (h/with-tmp-dir
        (let [project-name (str project-name "-new")
              project-dir (io/file h/*tmp-dir* project-name)]
          (h/run-lein generation "new" "immutant" project-name
                      :dir h/*tmp-dir*
                      :env h/base-lein-env) => 0
          (.exists (io/file project-dir)) => true
          (.exists (io/file project-dir "immutant.clj")) => true))))
    
    (fact (str "init should work for lein " generation)
      (h/with-tmp-dir
        (let [project-dir (io/file h/*tmp-dir* project-name)]
          (h/run-lein generation "new" project-name
                      :dir h/*tmp-dir*
                      :env h/base-lein-env)
          (h/run-lein generation "immutant" "init"
                      :dir project-dir
                      :env h/base-lein-env) => 0
          (.exists (io/file project-dir "immutant.clj")) => true)))))

