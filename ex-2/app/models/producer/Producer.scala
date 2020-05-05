package models.producer

import play.api.libs.json.{Json, OFormat}

case class Producer(id: Long, name: String, address: Long)

object Producer {
  implicit val producerFormat: OFormat[Producer] = Json.format[Producer]
}
