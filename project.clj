(defproject lein-immutant "0.8.0"
  :description "Leiningen plugin for managing an Immutant project."
  :url "https://github.com/immutant/lein-immutant"
  :mailing-list {:name "Immutant users list"
                 :post "immutant-users@immutant.org"
                 :subscribe "immutant-users-subscribe@immutant.org"
                 :unsubscribe "immutant-users-unsubscribe@immutant.org"}
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :eval-in-leiningen true
  :dependencies [[org.immutant/overlay      "1.2.0"]
                 [org.immutant/deploy-tools "0.7.0"]
                 [org.clojure/data.json     "0.1.1"]
                 [org.clojure/tools.cli     "0.2.1"]])
