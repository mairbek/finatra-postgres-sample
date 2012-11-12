package com.mairbek.contacts

import com.twitter.util.Future
import com.twitter.finagle.postgres.Client

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
