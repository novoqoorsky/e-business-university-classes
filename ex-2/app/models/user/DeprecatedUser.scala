package models.user

import play.api.libs.json.{Json, OFormat}

case class DeprecatedUser(id: Long, userName: String, password: String, email: String, client: Long)

object DeprecatedUser {
  implicit val userFormat: OFormat[DeprecatedUser] = Json.format[DeprecatedUser]
}
