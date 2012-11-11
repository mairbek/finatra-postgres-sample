package com.mairbek.contacts

import com.twitter.finatra.{Controller, FinatraServer}
import com.twitter.util.Future
import com.twitter.finagle.postgres.Client
import com.twitter.logging.config.{LoggerConfig, ConsoleHandlerConfig}
import com.twitter.logging.Logger

case class Contact(name: String, email: String, phone: String)

object App {

  class ContactController(repo: ContactRepository) extends Controller {

    get("/contacts.json") {
      request => for {
        contacts <- repo.allContacts()
      } yield (render.json(contacts))
    }

    //    post("/contacts") {
    //      request =>
    //        println(request.getContent())
    //        println(request.contentString)
    //        println(request.params.get("name"))
    //        val param = for {
    //          name <- request.params.get("name")
    //          email <- request.params.get("email")
    //          phone <- request.params.get("phone")
    //        } yield (Contact(name, email, phone))
    //
    //        val result = param match {
    //          case Some(Contact(name, email, phone)) => repo.addContact(name, email, phone)
    //          case None => Future.exception(new IllegalArgumentException("Unknown contact"))
    //        }
    //
    //        result.map(_ => render.plain("").status(200))
    //    }

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

  def createClient(url: String): Client = {
    val javaUri = new java.net.URI(url)

    val user = javaUri.getUserInfo.split(':')(0)
    val password = javaUri.getUserInfo.split(':')(0)
    val host = javaUri.getHost + ":" + javaUri.getPort
    val database = javaUri.getPath.substring(1)

    Client(host, user, Some(password), database)

  }

}


trait ContactRepository {
  def allContacts(): Future[Seq[Contact]]

  def addContact(name: String, email: String, phone: String): Future[Unit]
}


class InMemoryContactRepository extends ContactRepository {
  def allContacts() = Future.value(List(Contact("Mairbek", "mkhadikov@gmail.com", "+38 123 345 67 89")))

  def addContact(name: String, email: String, phone: String): Future[Unit] = Future.exception(new UnsupportedOperationException("oops"))
}

class PgContactRepository(client: Client) extends ContactRepository {
  def allContacts() = client.select("select * from contacts") {
    row =>
      Contact(row.getString("name"), row.getString("email"), row.getString("phone"))
  }

  def addContact(name: String, email: String, phone: String) =
    client.execute("insert into contacts (name, email, phone) values ('" + name + "', '" + email + "', '" + phone + "')").map(r => Unit)
}

/*
CREATE TABLE contacts (
    id    SERIAL,
	name   varchar(40) NOT NULL,
    email   varchar(40) NOT NULL,
    phone varchar(40) NOT NULL
);

 */


