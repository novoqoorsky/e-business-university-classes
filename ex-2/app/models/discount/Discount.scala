package models.discount

import play.api.libs.json.{Json, OFormat}

case class Discount(id: Long, product: Long, percentage: Int)

object Discount {
  implicit val discountFormat: OFormat[Discount] = Json.format[Discount]
}
