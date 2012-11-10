package com.mairbek.contacts

import com.twitter.finatra.{Controller, FinatraServer, View}
import com.twitter.util.Future
import com.twitter.finagle.postgres.Client

case class Contact(name: String, email: String, phone: String)

object App {

  class ContactController(repo: ContactRepository) extends Controller {

    get("/contacts.json") {
      request => for {
        contacts <- repo.allContacts()
      } yield (render.json(contacts))
    }

  }

  def main(args: Array[String]) {
    //    val repo = new InMemoryContactRepository
    val repo = new PgContactRepository(Client("localhost:5432", "mkhadikov", Some("pass"), "contacts"))
    val app = new ContactController(repo)

    FinatraServer.register(app)
    FinatraServer.start()
  }

}


trait ContactRepository {
  def allContacts(): Future[Seq[Contact]]
}


class InMemoryContactRepository extends ContactRepository {
  def allContacts() = Future.value(List(Contact("Mairbek", "mkhadikov@gmail.com", "+38 123 345 67 89")))
}

class PgContactRepository(client: Client) extends ContactRepository {
  def allContacts() = client.select("select * from contacts") {
    row =>
      Contact(row.getString("name"), row.getString("email"), row.getString("phone"))
  }
}

/*
CREATE TABLE contacts (
    id    SERIAL,
	name   varchar(40) NOT NULL,
    email   varchar(40) NOT NULL,
    phone varchar(40) NOT NULL
);

 */


