(ns com.infimany.hebbian.user-service.db.users-test
  (:require [clojure.test :refer :all]
            [clojure.core]
            [clojure.java.io :refer [resource]]
            [com.infimany.hebbian.user-service.db.users :refer :all]
            [cheshire.core :refer :all]
            [closchema.core :as schema]
            ))


; helper functions
(defn rm-extra-prop-error [error-map]
  (filter #(not (and (= [] (:path %)) (= :additional-properties-not-allowed (:error %))) ) error-map)
  )

(defn rm-req-prop-error [error-map]
  (filter #(not (and (= [:identity-id] (:path %)) (= :required (:error %))) ) error-map)
  )


(defn remove-expected-errors [error-map]
  ((comp rm-req-prop-error rm-extra-prop-error) error-map))

; tests

(deftest test-validate-json
  (testing "json validation for valid user json"
    (is (= true
           (validate-json (parse-string (slurp (resource "./valid_user.json")) true) "user-v1.json")
           )
        ))

  (testing "json validation for invalid user json"
    (is (= ()
           (remove-expected-errors
            (schema/report-errors
             (validate-json (parse-string (slurp (resource "./invalid_user.json")) true) "user-v1.json")))
           )
        ))
  )


