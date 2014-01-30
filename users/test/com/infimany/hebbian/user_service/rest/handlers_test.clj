(ns com.infimany.hebbian.user-service.rest.handlers-test
  (:require [clojure.test :refer :all]
            [clojure.core]
            [com.infimany.hebbian.user-service.rest.handlers :as handlers]
            [com.infimany.hebbian.user-service.db.users :as users]
            [ring.mock.request :refer :all]
            [cheshire.core :refer :all]
            [clojure.java.io :refer [resource]]
            ))


; test data

(def test-user (parse-string (slurp (resource "./valid_user.json")) true))

; fixtures

(defn delete-test-user [f]
  (users/delete-user (:identity-id test-user))
  (f)
  )


(use-fixtures :each delete-test-user)

; tests

(deftest handler-GET
    ; setup
    (users/insert-user test-user)
    ; test
    (is (= (handlers/app (request :get (str "/user/" (:identity-id test-user))) )
           {:status 200
            :headers {"Content-Type" "application/json; charset=utf-8" }
            :body (generate-string test-user)}))
)


(deftest handler-POST
  (is (empty? (users/get-user (:identity-id test-user))) )
  (is (= (handlers/app (body (content-type (request :post "/user") "application/json") (generate-string test-user)) )
         {:status 200
          :headers {}
          :body ""}))

  (is (= test-user(users/get-user (:identity-id test-user))))

  )

