package com.mairbek.contacts.test

import com.twitter.finatra.test.SpecHelper
import com.mairbek.contacts.{Contact, ContactRepository, App}

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.twitter.util.Future

@RunWith(classOf[JUnitRunner])
class AppSpec extends SpecHelper {

  def repo = new ContactRepository {
    def allContacts() = Future.value(List(Contact("Mairbek", "mkhadikov", "123 45 67")))
  }

  def app = new App.ContactController(repo)

  "GET /contacts.json" should "respond 200" in {
    get("/contacts.json")
    response.code should equal(200)
  }

}
