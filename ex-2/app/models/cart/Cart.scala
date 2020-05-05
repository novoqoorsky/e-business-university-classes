package models.cart

import play.api.libs.json.{Json, OFormat}

case class Cart(id: Long, value: Double)

object Cart {
  implicit val cartFormat: OFormat[Cart] = Json.format[Cart]
}