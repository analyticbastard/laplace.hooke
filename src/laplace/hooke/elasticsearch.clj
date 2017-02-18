(ns laplace.hooke.elasticsearch
  (require [clojure.string :as string]
           [clojurewerkz.elastisch.native  :as es]
           [clojurewerkz.elastisch.native.index :as esi]
           [clojurewerkz.elastisch.native.document :as esnd]
           [clojurewerkz.elastisch.native.response :as esnr]
           [clojurewerkz.elastisch.query :as q]
           [laplace.hooke.core :as lhc]))

(defn- create-connection [host tcp-port cluster-name]
  (es/connect [[host tcp-port]] {"cluster.name" cluster-name}))

(defn- create-document [conn project version ns* name* params bench]
  (let [index (string/join "_" [(if (empty project) project "laplacehook")  version])
        type  (string/join "/" [ns* name*])
        doc   (merge bench (apply hash-map params))]
    (esnd/create conn index type doc)))

(defrecord Elasticsearch [^String host ^Integer tcp-port ^String cluster-name]
  lhc/Collector
  (start [this]
    (assoc this :es (create-connection host tcp-port cluster-name)))
  (stop [this]
    (dissoc this :es))
  (collect [this project version ns* name* params bench]
    (create-document (:es this) project version ns* name* params bench)))

(defn add-local-elasticsearch [host tcp-port cluster-name]
  (let [es (->Elasticsearch host tcp-port cluster-name)]
    (intern 'laplace.hooke.core '*collector* (.start es))))