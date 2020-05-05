package models.clientorders

import play.api.libs.json.{Json, OFormat}

case class ClientOrders(id: Long, client: Long, order: Long)

object ClientOrders {
  implicit val clientOrdersFormat: OFormat[ClientOrders] = Json.format[ClientOrders]
}
