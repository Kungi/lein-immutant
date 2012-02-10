(ns leiningen.immutant
  (:use [leiningen.immutant.common :only [print-help unknown-subtask]]
        leiningen.immutant.deploy
        leiningen.immutant.env
        leiningen.immutant.init
        leiningen.immutant.archive
        leiningen.immutant.install
        leiningen.immutant.run))

(defn immutant
  "Manage the deployment lifecycle of an Immutant application."
  {:help-arglists '([subtask]
                    [new project-name]
                    [install [version [destination-dir]]
                    [overlay [feature-set [version]]]]
                    [env [key]]
                    [deploy [--archive]])
   :subtasks [#'install #'overlay #'env #'leiningen.immutant.init/new #'init #'archive #'deploy #'undeploy #'run]}
  ([]
     (print-help))
  ([subtask]
     (immutant nil subtask))
  ([project-or-nil subtask & args]
     (case subtask
       "install"      (apply install args)
       "overlay"      (apply overlay args)
       "env"          (apply env args)
       "new"          (leiningen.immutant.init/new (first args))
       "init"         (init project-or-nil)
       "archive"      (archive project-or-nil)
       "deploy"       (apply deploy project-or-nil args)
       "undeploy"     (undeploy project-or-nil)
       "run"          (apply run project-or-nil args)
       (unknown-subtask subtask))
     (shutdown-agents)))


