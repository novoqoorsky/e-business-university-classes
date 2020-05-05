package models.address

import play.api.libs.json.{Json, OFormat}

case class Address(id: Long, city: String, streetName: String, houseNumber: Int, postalCode: String)

object Address {
  implicit val addressFormat: OFormat[Address] = Json.format[Address]
}
