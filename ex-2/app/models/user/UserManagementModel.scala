package models.user

import java.time.ZonedDateTime
import java.util.UUID

import play.api.libs.json.{Json, OWrites}

case class UserManagementModel(id: UUID,
                               firstName: Option[String],
                               lastName: Option[String],
                               email: Option[String],
                               roleId: Int,
                               confirmed: Boolean,
                               signedUpAt: ZonedDateTime,
                               credentialsProvider: Boolean,
                               googleProvider: Boolean,
                               facebookProvider: Boolean,
                               twitterProvider: Boolean)

object UserManagementModel {
  implicit val w: OWrites[UserManagementModel] = Json.writes[UserManagementModel]
}