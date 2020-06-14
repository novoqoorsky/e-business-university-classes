package models.user

import java.util.UUID

import com.mohiva.play.silhouette.api.Identity
import play.api.libs.json.{JsObject, Json}

case class User(userID: UUID,
                firstName: Option[String],
                lastName: Option[String],
                email: Option[String],
                avatarURL: Option[String],
                activated: Boolean,
                role: UserRoles.UserRole) extends Identity {

  def toJson(token: String): JsObject = {
    Json.obj(
      "id" -> userID,
      "token" -> token,
      "firstName" -> firstName,
      "lastName" -> lastName,
      "role" -> role,
      "email" -> email
    )
  }
}
