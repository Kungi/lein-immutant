(ns leiningen.immutant.deploy-test
  (:use midje.sweet
        leiningen.immutant.test-helper)
  (:require [clojure.java.io :as io]))

;; TODO: deploy --archive

(setup-test-env)

(println "\n==> Testing deploy/undeploy")

(let [project-dir (io/file (io/resource "test-project"))]

  (future-fact "test --archive")
  
  (facts "deploy"
    (facts "in a project"
      (fact "with no args should work"
        (with-tmp-jboss-home
          (let [env (assoc base-lein-env "JBOSS_HOME" *tmp-jboss-home*)
                dd (io/file *tmp-deployments-dir* "test-project.clj")]
            (run-lein "immutant" "deploy"
                      :dir project-dir
                      :env env)              => 0
                      (.exists dd)                     => true
                      (:root (read-string (slurp dd))) => (.getAbsolutePath project-dir))))

      (fact "with path arg should print a warning"
        (with-tmp-jboss-home
          (let [env (assoc base-lein-env "JBOSS_HOME" *tmp-jboss-home*)
                dd (io/file *tmp-deployments-dir* "test-project.clj")
                result 
                (run-lein "immutant" "deploy" "yarg"
                          :dir project-dir
                          :env env
                          :return-result? true)]
            (re-find #"specified a root path of 'yarg'" (:err result)) =not=> nil
            (:exit result)                   => 0
            (.exists dd)                     => true
            (:root (read-string (slurp dd))) => (.getAbsolutePath project-dir))))
      
      (fact "with a --name arg should work"
        (with-tmp-jboss-home
          (let [env (assoc base-lein-env "JBOSS_HOME"  *tmp-jboss-home*)
                dd (io/file *tmp-deployments-dir* "ham.clj")]
            (run-lein "immutant" "deploy" "--name" "ham"
                      :dir project-dir
                      :env env)              => 0
                      (.exists dd)                     => true
                      (:root (read-string (slurp dd))) => (.getAbsolutePath project-dir))))
      
      (fact "with a --context-path and --virtual-host should work"
        (with-tmp-jboss-home
          (let [env (assoc base-lein-env "JBOSS_HOME"  *tmp-jboss-home*)
                dd (io/file *tmp-deployments-dir* "test-project.clj")]
            (run-lein "immutant" "deploy" "--virtual-host" "host" "--context-path" "path"
                      :dir project-dir
                      :env env)      => 0
                      (.exists dd)             => true
                      (read-string (slurp dd)) => {:root (.getAbsolutePath project-dir)
                                                   :context-path "path"
                                                   :virtual-host "host"}))))

    (fact "profiles should be noticed and written to the dd"
      (with-tmp-jboss-home
        (let [env (assoc base-lein-env "JBOSS_HOME"  *tmp-jboss-home*)
              dd (io/file *tmp-deployments-dir* "test-project.clj")]
          (run-lein "with-profile" "foo" "immutant" "deploy"
                    :dir project-dir
                    :env env)      => 0
                    (.exists dd)             => true
                    (let [dd-data (read-string (slurp dd))]
                      dd-data                  => {:root (.getAbsolutePath project-dir)
                                                   :lein-profiles [:foo]}
                      (:lein-profiles dd-data) => vector?))))

    (fact "profiles passed via --with-profiles should be in dd, and print a dep warning"
      (with-tmp-jboss-home
        (let [env (assoc base-lein-env "JBOSS_HOME"  *tmp-jboss-home*)
              dd (io/file *tmp-deployments-dir* "test-project.clj")
              {:keys [out err exit]}
              (run-lein "immutant" "deploy" "--lein-profiles" "foo,bar"
                        :dir project-dir
                        :env env
                        :return-result? true)]
          exit => 0
          (re-find #"via --lein-profiles is deprecated" err) =not=> nil
          (re-find #"lein with-profile foo,bar immutant deploy" err) =not=> nil
          (.exists dd)             => true
          (let [dd-data (read-string (slurp dd))]
            dd-data                  => {:root (.getAbsolutePath project-dir)
                                         :lein-profiles ["foo" "bar"]}
            (:lein-profiles dd-data) => vector?))))
    
    (facts "not in a project"
      (fact "with a path arg should work"
        (with-tmp-jboss-home
          (let [env (assoc base-lein-env "JBOSS_HOME" *tmp-jboss-home*)
                dd (io/file *tmp-deployments-dir* "test-project.clj")]
            (run-lein "immutant" "deploy" (.getAbsolutePath project-dir)
                      :dir "/tmp"
                      :env env)              => 0
                      (.exists dd)                     => true
                      (:root (read-string (slurp dd))) => (.getAbsolutePath project-dir))))

      (fact "with a non-existent path arg should work"
        (with-tmp-jboss-home
          (let [env (assoc base-lein-env "JBOSS_HOME" *tmp-jboss-home*)
                {:keys [err exit]}
                (run-lein "immutant" "deploy" "/tmp/hAmBisCuit"
                          :dir "/tmp"
                          :env env
                          :return-result? true)]
            exit                                                     => 1
            (re-find #"Error: '/tmp/hAmBisCuit' does not exist" err) =not=> nil)))
      
      (fact "with a --name arg and a path arg should work"
        (with-tmp-jboss-home
          (let [env (assoc base-lein-env "JBOSS_HOME"  *tmp-jboss-home*)
                dd (io/file *tmp-deployments-dir* "ham.clj")]
            (run-lein "immutant" "deploy" "--name" "ham" (.getAbsolutePath project-dir)
                      :dir "/tmp"
                      :env env)              => 0
                      (.exists dd)                     => true
                      (:root (read-string (slurp dd))) => (.getAbsolutePath project-dir))))

      (fact "with a --context-path, --virtual-host, and an path arg should work"
        (with-tmp-jboss-home
          (let [env (assoc base-lein-env "JBOSS_HOME"  *tmp-jboss-home*)
                dd (io/file *tmp-deployments-dir* "test-project.clj")]
            (run-lein "immutant" "deploy"
                      "--virtual-host" "host"
                      "--context-path" "path"
                      (.getAbsolutePath project-dir)
                      :dir "/tmp"
                      :env env)      => 0
                      (.exists dd)             => true
                      (read-string (slurp dd)) => {:root (.getAbsolutePath project-dir)
                                                   :context-path "path"
                                                   :virtual-host "host"})))

      (future-fact "profiles should be noticed and written to the dd"
        (with-tmp-jboss-home
          (let [env (assoc base-lein-env "JBOSS_HOME"  *tmp-jboss-home*)
                dd (io/file *tmp-deployments-dir* "test-project.clj")]
          (run-lein "with-profile" "foo" "immutant" "deploy" (.getAbsolutePath project-dir)
                    :dir "/tmp"
                    :env env)      => 0
          (.exists dd)             => true
          (let [dd-data (read-string (slurp dd))]
            dd-data                  => {:root (.getAbsolutePath project-dir)
                                         :lein-profiles [:foo]}
            (:lein-profiles dd-data) => vector?))))))
  
  (facts "undeploy"
    (facts "in a project"
      (fact "with no args should work"
        (with-tmp-jboss-home
          (create-tmp-deploy "test-project")
          (let [env (assoc base-lein-env "JBOSS_HOME" *tmp-jboss-home*)]
            (run-lein "immutant" "undeploy"
                      :dir project-dir
                      :env env)              => 0
                      (tmp-deploy-removed? "test-project") => false)))

      (fact "with path arg should print a warning"
        (with-tmp-jboss-home
          (create-tmp-deploy "test-project")
          (let [env (assoc base-lein-env "JBOSS_HOME" *tmp-jboss-home*)
                {:keys [err exit]}
                (run-lein "immutant" "undeploy" "yarg"
                          :dir project-dir
                          :env env
                          :return-result? true)]
            exit                                             => 0
            (re-find #"specified a root path of 'yarg'" err) =not=> nil
            (tmp-deploy-removed? "test-project")             => false)))
      
      (fact "with a --name arg should work"
        (with-tmp-jboss-home
          (create-tmp-deploy "ham")
          (let [env (assoc base-lein-env "JBOSS_HOME"  *tmp-jboss-home*)]
            (run-lein "immutant" "undeploy" "--name" "ham"
                      :dir project-dir
                      :env env)                  => 0
                      (tmp-deploy-removed? "test-project") => false))))
    
    (facts "not in a project"
      (fact "with a path arg should work"
        (with-tmp-jboss-home
          (create-tmp-deploy "test-project")
          (let [env (assoc base-lein-env "JBOSS_HOME" *tmp-jboss-home*)]
            (run-lein "immutant" "undeploy" (.getAbsolutePath project-dir)
                      :dir "/tmp"
                      :env env)                  => 0
                      (tmp-deploy-removed? "test-project") => false)))

      (fact "with a non-existent path arg should work"
        (with-tmp-jboss-home
          (let [env (assoc base-lein-env "JBOSS_HOME" *tmp-jboss-home*)
                {:keys [exit err]}
                (run-lein "immutant" "undeploy" "/tmp/hAmBisCuit"
                          :dir "/tmp"
                          :env env
                          :return-result? true)]
            exit                                                     => 1
            (re-find #"Error: '/tmp/hAmBisCuit' does not exist" err) =not=> nil))))))

