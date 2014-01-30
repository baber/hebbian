(ns com.infimany.hebbian.user-service.rest.handlers-test
  (:require [clojure.test :refer :all]
            [clojure.core]
            [com.infimany.hebbian.user-service.rest.handlers :as handlers]
            [com.infimany.hebbian.user-service.db.users :as users]
            [ring.mock.request :refer :all]
            [cheshire.core :refer :all]
            [clojure.java.io :refer [resource]]
            ))



; tests

(def test-user (parse-string (slurp (resource "./valid_user.json")) true))

(deftest handler-test
  ; setup
  (users/insert-user test-user)
  ; test
  (is (= (handlers/app (request :get (str "/user/" (:identity-id test-user))) )
         {:status 200
          :headers {"Content-Type" "application/json; charset=utf-8" }
          :body (generate-string test-user)})))

