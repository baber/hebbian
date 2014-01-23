(ns com.infimany.hebbian.user-service.db.users-test
  (:require [clojure.test :refer :all]
            [clojure.core]
            [clojure.java.io :refer [resource]]
            [com.infimany.hebbian.user-service.db.users :refer :all]
            [cheshire.core :refer :all]
            ))

(deftest test-validate-json
  (testing "json validation for valid user json"
    (is (= true
           (validate-json (parse-string  (slurp (resource "./valid_user.json"))) "user-v1.json")
           )
        ))
  (testing "json validation for invalid user json"
    (is (= true
           (validate-json (parse-string (slurp(resource "./invalid_user.json"))) "user-v1.json")
           )
        ))
  )












