package com.mairbek.contacts

import com.twitter.finagle.postgres.Client

object Heroku {

  def createClient(url: String): Client = {
    val javaUri = new java.net.URI(url)

    val user = javaUri.getUserInfo.split(':')(0)
    val password = javaUri.getUserInfo.split(':')(1)
    val host = javaUri.getHost + ":" + javaUri.getPort
    val database = javaUri.getPath.substring(1)

    Client(host, user, Some(password), database)
  }

}
