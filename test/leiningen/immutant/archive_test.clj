(ns leiningen.immutant.archive-test
  (:use midje.sweet
        leiningen.immutant.test-helper)
  (:require [clojure.java.io :as io]))

(setup-test-env)

(def base-contents #{"project.clj"
                     "resources/resource.txt"
                     "src/test_project/core.clj"})

(def base-project-contents
  (conj base-contents
        "target/classes/Foo.class"
        "target/native/foo.so"))

(println "\n==> Testing archive")

(facts "archive"
  (facts "in a project"
    (fact (str "with no args should work")
      (with-tmp-dir
        (let [project-dir (copy-resource-to-tmp "test-project")
              archive (io/file project-dir "test-project.ima")]
          (run-lein "immutant" "archive"
                    :dir project-dir
                    :env base-lein-env)          => 0
          (.exists archive)                      => true
          (verify-archive archive
                          base-project-contents) => true)))

    (fact (str "with no args and non-default project settings should work")
      (with-tmp-dir
        (let [project-dir (copy-resource-to-tmp "non-defaults-project")
              archive (io/file project-dir "test-project.ima")]
          (run-lein "immutant" "archive"
                    :dir project-dir
                    :env base-lein-env)          => 0
          (.exists archive)                      => true
          (verify-archive
           archive
           (conj base-contents
                 "schmative/foo.so"
                 "prod-resources/resource2.txt"
                 "schmasses/Foo.class"
                 "schmrc/test_project/ham.clj")) => true)))

    (fact (str "with no args and no project.clj should work")
      (with-tmp-dir
        (let [project-dir (copy-resource-to-tmp "projectless-project")
              archive (io/file project-dir "projectless-project.ima")]
          (run-lein "immutant" "archive"
                    :dir project-dir
                    :env base-lein-env)    => 0
          (.exists archive)                => true
          (verify-archive
           archive
           (-> base-project-contents
               (disj "project.clj")
               (conj
                "native/bar.so"
                "classes/Bar.class")))   => true)))
        
    (fact (str "with path arg should print a warning")
      (with-tmp-dir
        (let [project-dir (copy-resource-to-tmp "test-project")
              archive (io/file project-dir "test-project.ima")
              result 
              (run-lein "immutant" "archive" "yarg"
                        :dir project-dir
                        :env base-lein-env
                        :return-result? true)]
          (re-find #"specified a root path of 'yarg'" (:err result)) =not=> nil
          (:exit result)                   => 0
          (.exists archive)                => true
          (verify-archive archive
                          base-project-contents)   => true)))
    
    (fact (str "with a --name arg should work")
      (with-tmp-dir
        (let [project-dir (copy-resource-to-tmp "test-project")
              archive (io/file project-dir "blarg.ima")]
          (run-lein "immutant" "archive" "--name" "blarg"
                    :dir project-dir
                    :env base-lein-env)  => 0
          (.exists archive)              => true
          (verify-archive archive
                          base-project-contents) => true)))


    (fact (str "with options should add an internal descriptor")
      (with-tmp-dir
        (let [project-dir (copy-resource-to-tmp "test-project")
              archive (io/file project-dir "test-project.ima")]
          (run-lein "immutant" "archive" "--context-path" "/" "--virtual-host" "ham"
                    :dir project-dir
                    :env base-lein-env)  => 0
          (.exists archive)              => true
          (verify-archive
           archive
           (conj base-project-contents
                 ".immutant.clj"))       => true
           (read-string
            (slurp (file-from-archive archive ".immutant.clj"))) => {:context-path "/"
                                                                     :virtual-host "ham"})))

    (fact "with profiles should add an internal descriptor"
      (with-tmp-dir
        (let [project-dir (copy-resource-to-tmp "test-project")
              archive (io/file project-dir "test-project.ima")]
          (run-lein "with-profile" "ham" "immutant" "archive"
                    :dir project-dir
                    :env base-lein-env)  => 0
          (.exists archive)              => true
          (verify-archive
           archive
           (conj base-project-contents
                 ".immutant.clj"))       => true
           (read-string
            (slurp (file-from-archive archive ".immutant.clj"))) => {:lein-profiles [:ham]})))

    (fact "with profiles and options should add an internal descriptor"
      (with-tmp-dir
        (let [project-dir (copy-resource-to-tmp "test-project")
              archive (io/file project-dir "test-project.ima")]
          (run-lein "with-profile" "ham" "immutant" "archive" "--context-path" "biscuit"
                    :dir project-dir
                    :env base-lein-env)  => 0
          (.exists archive)              => true
          (verify-archive
           archive
           (conj base-project-contents
                 ".immutant.clj"))       => true
           (read-string
            (slurp (file-from-archive archive ".immutant.clj"))) => {:lein-profiles [:ham]
                                                                     :context-path "biscuit"})))
        
    (fact (str "with a --include-dependencies arg should work")
      (with-tmp-dir
        (let [project-dir (copy-resource-to-tmp "test-project")
              archive (io/file project-dir "test-project.ima")]
          (run-lein "immutant" "archive" "--include-dependencies"
                    :dir project-dir
                    :env base-lein-env)    => 0
          (.exists archive)                => true
          (verify-archive
           archive
           (conj base-project-contents
                 "lib/clojure-1.4.0.jar")) => true))))

  
  (facts "not in a project"
    (fact (str "with a path arg should work")
      (with-tmp-dir
        (let [project-dir (copy-resource-to-tmp "test-project")
              archive (io/file *tmp-dir* "test-project.ima")]
          (run-lein "immutant" "archive" "test-project"
                    :dir *tmp-dir*
                    :env base-lein-env)    => 0
          (.exists archive)                => true
          (verify-archive archive
                          base-project-contents)   => true)))

    (fact (str "with a path and non-default project settings should work")
      (with-tmp-dir
        (let [project-dir (copy-resource-to-tmp "non-defaults-project")
              archive (io/file *tmp-dir* "test-project.ima")]
          (run-lein "immutant" "archive" "non-defaults-project"
                    :dir *tmp-dir*
                    :env base-lein-env)              => 0
          (.exists archive)                => true
          (verify-archive
           archive
           (conj base-contents
                 "schmative/foo.so"
                 "prod-resources/resource2.txt"
                 "schmasses/Foo.class"
                 "schmrc/test_project/ham.clj"))   => true)))

    (fact (str "with a path and no project.clj should work")
      (with-tmp-dir
        (let [project-dir (copy-resource-to-tmp "projectless-project")
              archive (io/file *tmp-dir* "projectless-project.ima")]
          (run-lein "immutant" "archive" "projectless-project"
                    :dir *tmp-dir*
                    :env base-lein-env)              => 0
          (.exists archive)                => true
          (verify-archive
           archive
           (-> base-project-contents
               (disj "project.clj")
               (conj
                "native/bar.so"
                "classes/Bar.class")))   => true)))
    
    (fact (str "with a non-existent path arg should work")
      (let [{:keys [err exit]}
            (run-lein "immutant" "archive" "/tmp/hAmBisCuit"
                      :dir "/tmp"
                      :env base-lein-env
                      :return-result? true)]
        exit                                                     => 1
        (re-find #"Error: '/tmp/hAmBisCuit' does not exist" err) =not=> nil))
    
    (fact (str "with a --name arg and a path arg should work")
      (with-tmp-dir
        (copy-resource-to-tmp "test-project")
        (let [archive (io/file *tmp-dir* "blarg.ima")]
          (run-lein "immutant" "archive" "--name" "blarg" "test-project"
                    :dir *tmp-dir*
                    :env base-lein-env)  => 0
          (.exists archive)              => true
          (verify-archive archive
                          base-project-contents) => true)))

    (fact (str "with a --name arg, --include-dependencies, and a path arg should work")
      (with-tmp-dir
        (copy-resource-to-tmp "test-project")
        (let [archive (io/file *tmp-dir* "blarg.ima")]
          (run-lein "immutant" "archive" "--name" "blarg" "--include-dependencies" "test-project"
                    :dir *tmp-dir*
                    :env base-lein-env)  => 0
          (.exists archive)              => true
          (verify-archive
           archive
           (conj base-project-contents
                 "lib/clojure-1.4.0.jar")) => true)))

    (future-fact "with a path, profiles, and options should add an internal descriptor"
      (with-tmp-dir
        (let [project-dir (copy-resource-to-tmp "test-project")
              archive (io/file *tmp-dir* "test-project.ima")]
          (run-lein "with-profile" "ham" "immutant" "archive" "--context-path" "biscuit" "test-project"
                    :dir *tmp-dir*
                    :env) base-lein-env  => 0
          (.exists archive)              => true
          (verify-archive
           archive
           (conj base-project-contents
                 ".immutant.clj"))       => true
           (read-string
            (slurp (file-from-archive archive ".immutant.clj"))) => {:lein-profiles [:ham]
                                                                     :context-path "biscuit"})))))

