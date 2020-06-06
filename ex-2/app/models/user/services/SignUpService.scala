package models.user.services

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import models.user.User

import scala.concurrent.{ExecutionContext, Future}

class SignUpService @Inject()(
                               avatarService: AvatarService,
                               authTokenService: AuthTokenService,
                               authenticateService: AuthenticateService,
                               passwordHasherRegistry: PasswordHasherRegistry,
                               userService: UserService)(implicit ex: ExecutionContext) {

  def signUpByCredentials(data: CredentialsSingUpData, userIdAddress: String, activationUrlProvider: UUID => String): Future[SignUpResult] = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
    userService.retrieve(loginInfo).flatMap {
      case Some(user) =>
        Future.successful(UserAlreadyExists)
      case None =>
        val authInfo = passwordHasherRegistry.current.hash(data.password)
        for {
          avatar <- avatarService.retrieveURL(data.email)
          user <- userService.createOrUpdate(loginInfo, data.email, Some(data.firstName), Some(data.lastName), avatar)
          _ <- authenticateService.addAuthenticateMethod(user.userID, loginInfo, authInfo)
          authToken <- authTokenService.create(user.userID)
        } yield {
          val activationUrl = activationUrlProvider(authToken.id)

          println(activationUrl)
          UserCreated(user)
        }
    }
  }
}

sealed trait SignUpResult
case object UserAlreadyExists extends SignUpResult
case class UserCreated(user: User) extends SignUpResult
case object InvalidRecaptchaCode extends SignUpResult

case class CredentialsSingUpData(firstName: String,
                                 lastName: String,
                                 email: String,
                                 password: String)
