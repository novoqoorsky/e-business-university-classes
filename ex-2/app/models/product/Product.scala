package models.product

import play.api.libs.json.{Json, OFormat}

case class Product(id: Long, name: String, description: String, category: Long, producer: Long, price: Double)

object Product {
  implicit val productFormat: OFormat[Product] = Json.format[Product]
}


