package com.mairbek.contacts

import com.twitter.finatra.test.SpecHelper

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.twitter.util.Future

@RunWith(classOf[JUnitRunner])
class AppSpec extends SpecHelper with MockitoSugar {
  val repo = mock[ContactRepository]

  when(repo.allContacts()).thenReturn(Future.value(
    List(Contact("Mairbek", "mkhadikov@gmail.com", "123"))
  ))
  val unit: Future[Unit] = Future.value(Unit)
  when(repo.addContact(any[String], any[String], any[String])).thenReturn(unit)

  def app = new App.ContactController(repo)

  "GET /contacts.json" should "respond 200" in {
    get("/contacts.json")
    response.code should equal(200)
  }

  "POST /contact" should "respond 200" in {
    post("/contact", params = Map("name" -> "mairbek", "email" -> "mkhadikov@gmail.com", "phone" -> "123"))
    response.code should equal(200)
  }

}
