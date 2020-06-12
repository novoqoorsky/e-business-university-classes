package models.client

import play.api.libs.json.{Json, OFormat}

case class Client(id: Long, name: String, lastName: String, email: String, address: Long, var cart: Long)

object Client {
  implicit val clientFormat: OFormat[Client] = Json.format[Client]
}
