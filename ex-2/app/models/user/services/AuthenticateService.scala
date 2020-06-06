package models.user.services

import java.time.Instant
import java.util.UUID

import akka.util.Timeout
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.{AuthInfo, LoginInfo}
import com.mohiva.play.silhouette.impl.exceptions.{IdentityNotFoundException, InvalidPasswordException}
import com.mohiva.play.silhouette.impl.providers._
import javax.inject.Inject
import models.user.User
import models.user.daos.LoginInfoDAO

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class AuthenticateService @Inject()(credentialsProvider: CredentialsProvider,
                                    userService: UserService,
                                    authInfoRepository: AuthInfoRepository,
                                    loginInfoDAO: LoginInfoDAO,
                                   )(implicit ec: ExecutionContext) {
  implicit val timeout: Timeout = 5.seconds

  def credentials(email: String, password: String): Future[AuthenticateResult] = {
    val credentials = Credentials(email, password)
    credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
      userService.retrieve(loginInfo).map {
        case Some(user) if !user.activated =>
          NonActivatedUserEmail
        case Some(user) =>
          Success(user)
        case None =>
          UserNotFound
      }
    }.recoverWith {
      case _: InvalidPasswordException =>
        Future.successful(InvalidPassword(20))
      case _: IdentityNotFoundException =>
        Future.successful(UserNotFound)
      case e =>
        Future.failed(e)
    }
  }

  def addAuthenticateMethod[T <: AuthInfo](userId: UUID, loginInfo: LoginInfo, authInfo: T): Future[Unit] = {
    for {
      _ <- loginInfoDAO.saveUserLoginInfo(userId, loginInfo)
      _ <- authInfoRepository.add(loginInfo, authInfo)
    } yield ()
  }

  def userHasAuthenticationMethod(userId: UUID, providerId: String): Future[Boolean] = {
    loginInfoDAO.find(userId, providerId).map(_.nonEmpty)
  }

  def getAuthenticationProviders(email: String): Future[Seq[String]] = loginInfoDAO.getAuthenticationProviders(email)
}

sealed trait AuthenticateResult
case class Success(user: User) extends AuthenticateResult
case class InvalidPassword(attemptsAllowed: Int) extends AuthenticateResult
object NonActivatedUserEmail extends AuthenticateResult
object UserNotFound extends AuthenticateResult
case class ToManyAuthenticateRequests(nextAllowedAttemptTime: Instant) extends AuthenticateResult

sealed trait UserForSocialAccountResult
case object NoEmailProvided extends UserForSocialAccountResult
case class EmailIsBeingUsed(providers: Seq[String]) extends UserForSocialAccountResult
case class AccountBound(user: User) extends UserForSocialAccountResult
