package models.client

import play.api.libs.json.{Json, OFormat}

case class Client(id: Long, name: String, lastName: String, address: Long, cart: Option[Long])

object Client {
  implicit val clientFormat: OFormat[Client] = Json.format[Client]
}
