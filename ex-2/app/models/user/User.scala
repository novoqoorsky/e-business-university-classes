package models.user

import java.util.UUID

import com.mohiva.play.silhouette.api.Identity

case class User(userID: UUID,
                firstName: Option[String],
                lastName: Option[String],
                email: Option[String],
                avatarURL: Option[String],
                activated: Boolean,
                role: UserRoles.UserRole) extends Identity
