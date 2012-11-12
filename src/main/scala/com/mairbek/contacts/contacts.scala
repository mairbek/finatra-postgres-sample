package com.mairbek.contacts

import com.twitter.finatra.{Controller, FinatraServer}
import com.twitter.util.Future
import com.twitter.finagle.postgres.Client
import com.twitter.logging.config.{LoggerConfig, ConsoleHandlerConfig}
import com.twitter.logging.Logger
import Heroku._

case class Contact(name: String, email: String, phone: String)

object App {

  class ContactController(repo: ContactRepository) extends Controller {

    get("/contacts.json") {
      request => for {
        contacts <- repo.allContacts()
      } yield (render.json(contacts))
    }

    post("/contact") {
      request =>
        val param = for {
          name <- request.params.get("name")
          email <- request.params.get("email")
          phone <- request.params.get("phone")
        } yield ((name, email, phone))

        val result = param match {
          case Some((name, email, phone)) => repo.addContact(name, email, phone)
          case None => Future.exception(new IllegalArgumentException("Unknown contact"))
        }

        result.map(_ => render.plain("").status(200))
    }

  }

  def main(args: Array[String]) {
    //    val repo = new InMemoryContactRepository


    val config = new LoggerConfig {
      node = ""
      level = Logger.DEBUG
      handlers = new ConsoleHandlerConfig {
      }
    }
    config()


    val uri: Option[String] = sys.env.get("DATABASE_URL")

    val client = uri match {
      case Some(url) => createClient(url)
      case None => Client("localhost:5432", "mkhadikov", Some("pass"), "contacts")
    }

    val repo = new PgContactRepository(client)
    val app = new ContactController(repo)

    FinatraServer.register(app)
    FinatraServer.start()
  }

}