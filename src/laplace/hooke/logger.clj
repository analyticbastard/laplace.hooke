(ns laplace.hooke.logger
  (require [laplace.hooke.core :as lhc]
           [clojure.string :as string]))

(defrecord Logger []
  lhc/Collector
  (collect [this project version ns* name* params bench-info]
    (println (string/join "." [project version]) (string/join "." [ns* name*]) ":" params)
    (println bench-info)))

(defn add-logger []
  (intern 'laplace.hooke.core '*collector* (laplace.hooke.logger/->Logger)))