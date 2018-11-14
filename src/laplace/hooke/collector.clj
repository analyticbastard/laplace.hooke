(ns laplace.hooke.collector
  (require [laplace.hooke.core :as lhc]
           [clojure.string :as string]))

(defrecord Logger []
  lhc/Collector
  (collect [this project version ns* name* params bench-info]
    (println (string/join "." [project version]) (string/join "." [ns* name*]) ":" params)
    (println bench-info)))

(defrecord Store [store]
  lhc/Collector
  (collect [this project version ns* name* params bench-info]
    (swap! store update (str ns* "/" name*) (fnil conj []) (:elapsed bench-info))))
