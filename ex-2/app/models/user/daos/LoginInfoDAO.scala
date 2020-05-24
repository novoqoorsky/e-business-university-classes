package models.user.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.user.User

import scala.concurrent.Future

trait LoginInfoDAO {

  def getAuthenticationProviders(email: String): Future[Seq[String]]
  def find(userId: UUID, providerId: String): Future[Option[(User, LoginInfo)]]
  def saveUserLoginInfo(userID: UUID, loginInfo: LoginInfo): Future[Unit]
}
