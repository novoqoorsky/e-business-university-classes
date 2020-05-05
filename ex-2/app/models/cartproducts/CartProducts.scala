package models.cartproducts

import play.api.libs.json.{Json, OFormat}

case class CartProducts(id: Long, cart: Long, product: Long)

object CartProducts {
  implicit val cartProductsFormat: OFormat[CartProducts] = Json.format[CartProducts]
}
