package models.order

import play.api.libs.json.{Json, OFormat}

case class Order(id: Long, reference: String)

object Order {
  implicit val orderFormat: OFormat[Order] = Json.format[Order]
}
