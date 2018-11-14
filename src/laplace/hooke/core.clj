(ns laplace.hooke.core
  (require [clojure.core.async :as a]
           [robert.hooke :as h]
           [listora.measure :as m]))

(defprotocol Collector
  (collect [this project version ns* name* params bench-info]))

(def ^:dynamic *project* "")

(def ^:dynamic *version* "")

(def ^:dynamic *ns-exclusions* #{})

(def ^:dynamic *collector*)

(defn collect-measures! []
  (a/go-loop []
    (when-let [bench (a/<! m/measurements)]
      (doseq [f* (keys bench)
              :let [meta-f (meta f*)
                    ns* (-> meta-f :ns str)
                    name* (-> meta-f :name str)]]
        (.collect *collector* *project* *version* ns* name* [] (get bench f*)))
      (recur))))

(defn manipulate-hooks [ns* p]
  (->> ns*
       ns-publics
       (sequence
         (comp
           (filter #(-> % *ns-exclusions* not))
           (filter (comp fn? var-get second))
           (map p)))))

(defn add-hook [var*]
  (let [f* (-> var* second)]
    (h/add-hook f*
                ::laplace
                (fn [f & args]
                  (m/profile f* (apply f args))))))

(defn add-hooks [ns*]
  (collect-measures!)
  (manipulate-hooks ns* add-hook))

(defn remove-hooks [ns*]
  (letfn [(remove-hook [var*]
            (h/remove-hook (-> var* second)
                           ::laplace))]
    (manipulate-hooks ns* remove-hook)))

(defn add-hooks-all [re]
  (->> (all-ns)
       (sequence
        (comp
         (map str)
         (filter #(re-find re %))
         (map symbol)))
       (map add-hooks)))

(defn set-collector [logger]
  (intern 'laplace.hooke.core '*collector* logger))
