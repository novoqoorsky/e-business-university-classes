package controllers

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.api.util.Clock
import models.user.User
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc._
import utils.silhouette.DefaultEnv

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

abstract class AbstractAuthController(silhouette: Silhouette[DefaultEnv],
                                      configuration: Configuration,
                                      clock: Clock)(implicit ex: ExecutionContext) extends InjectedController with I18nSupport {

  protected def authenticateUser(user: User, loginInfo: LoginInfo, rememberMe: Boolean)(implicit request: Request[_]): Future[AuthenticatorResult] = {
    val c = configuration.underlying
    silhouette.env.authenticatorService.create(loginInfo).map {
      case authenticator if rememberMe =>
        authenticator.copy(
          expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
          idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout")
        )
      case authenticator => authenticator
    }.flatMap { authenticator =>
      silhouette.env.authenticatorService.init(authenticator).flatMap { token =>
        silhouette.env.eventBus.publish(LoginEvent(user, request))
        silhouette.env.authenticatorService.embed(token, Ok(user.toJson(token)))
      }
    }
  }
}
