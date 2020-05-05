package models.user

import play.api.libs.json.{Json, OFormat}

case class User(id: Long, userName: String, password: String, email: String, client: Option[Long])

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
}
