(ns leiningen.immutant.deploy
  (:require [clojure.java.io               :as io]
            [leiningen.immutant.shim       :as shim]
            [leiningen.immutant.archive    :as local-archive]
            [leiningen.immutant.common     :as common]
            [immutant.deploy-tools.archive :as archive]
            [immutant.deploy-tools.deploy  :as deploy]
            [immutant.deploy-tools.util    :as util]))

(defn deploy 
  "Deploys a project to the Immutant specified by ~/.lein/immutant/current or $IMMUTANT_HOME"
  [project root & args] 
  (let [jboss-home (common/get-jboss-home)
        deployed-file (if (some #{"--archive"} args)
                        (binding [archive/*dependency-resolver* (shim/copy-dependencies-fn)]
                          (deploy/deploy-archive jboss-home project root))
                        (deploy/deploy-dir jboss-home project root))]
    (println "Deployed" (util/app-name project root) "to" (.getAbsolutePath deployed-file))))

(defn undeploy
  "Undeploys a project from the Immutant specified by ~/.lein/immutant/current or $IMMUTANT_HOME"
  [project root]
  (let [app-name (util/app-name project root)
        jboss-home (common/get-jboss-home)
        deploy-path (.getAbsolutePath (util/deployment-dir jboss-home))]
    (if (deploy/undeploy jboss-home project root)
      (println "Undeployed" app-name "from" deploy-path)
      (println "No action taken:" app-name "is not deployed to" deploy-path))))

