(ns leiningen.immutant
  (:require [leiningen.immutant.deploy  :as deploy]
            [leiningen.immutant.env     :as env]
            [leiningen.immutant.test    :as test]
            [leiningen.immutant.init    :as init]
            [leiningen.immutant.archive :as archive]
            [leiningen.immutant.install :as install]
            [leiningen.immutant.run     :as run]
            [leiningen.immutant.common  :as common]
            [clojure.tools.cli          :as cli]))

(def cli-options
  {"deploy"    deploy/deploy-options
   "undeploy"  deploy/undeploy-options
   "archive"   archive/archive-options
   "test"      test/test-options})

(defn- subtask-with-resolved-project [subtask project-or-nil root-dir options]
  (apply subtask
         (conj (common/resolve-project project-or-nil root-dir) options)))

(defn immutant
  "Manage the deployment lifecycle of an Immutant application."
  {:no-project-needed true
   :subtasks [#'init/new
              #'install/install
              #'install/overlay
              #'install/version
              #'env/env
              #'init/init
              #'archive/archive
              #'deploy/deploy
              #'deploy/undeploy
              #'run/run
              #'test/test]}
  ([subtask]
     (common/print-help))
  ([project-or-nil subtask & args]
     (if (= "run" subtask)
       ;; run currently handles its own options
       (apply run/run project-or-nil args)
       (let [[options other-args banner] (apply cli/cli args (cli-options subtask))
             root-dir (common/get-application-root other-args)]
         (case subtask
           "install"      (apply install/install other-args)
           "overlay"      (apply install/overlay other-args)
           "version"      (install/version)
           "env"          (apply env/env other-args)
           "new"          (init/new (first other-args))
           "init"         (init/init project-or-nil)
           "archive"      (subtask-with-resolved-project
                            archive/archive project-or-nil root-dir options)
           "deploy"       (subtask-with-resolved-project
                            deploy/deploy project-or-nil root-dir options)
           "undeploy"     (subtask-with-resolved-project
                            deploy/undeploy project-or-nil root-dir options)
           "test"         (subtask-with-resolved-project
                            test/test project-or-nil root-dir options)
           (common/unknown-subtask subtask))))
     (shutdown-agents)))
