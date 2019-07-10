(ns ross.core-test
  (:require [clojure.test :refer :all]
            [ross.core :refer [normalize-date-key]]))

(deftest normalize-date-key-test
  (testing "normalize-date-key"
    (is (= (normalize-date-key {:published-date nil
                                :updated-date (java.util.Date. 2019 1 1)})
           {:date (java.util.Date. 2019 1 1)}))
    (is (= (normalize-date-key {:published-date (java.util.Date. 2019 1 1)
                                :updated-date nil})
           {:date (java.util.Date. 2019 1 1)}))))
