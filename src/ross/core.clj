(ns ross.core
  (:require [remus]))

(defn parse-url-list [lines]
  (clojure.string/split lines #"\n"))


;; Some feeds are using "publishedDate", some are using "updatedDate."
;; Reconcile this by taking either as "date".
(defn normalize-date-key [feed-entry]
  (dissoc (assoc feed-entry :date (or (:published-date feed-entry)
                   (:updated-date feed-entry)))
          :published-date
          :updated-date))


(defn process-url [url]
  (let [feed (:feed (remus/parse-url url))
        all-keys (keys (first (:entries feed)))
        entries (map (fn [entry] (select-keys entry '(:published-date :updated-date :title :link)))
                     (:entries feed))]
    (clojure.pprint/pprint (map normalize-date-key entries))
    (clojure.pprint/pprint all-keys)))


(defn main [urls-list-filename]
  (map process-url ; map used for side effect. Well...
       (parse-url-list
         (slurp urls-list-filename))))
