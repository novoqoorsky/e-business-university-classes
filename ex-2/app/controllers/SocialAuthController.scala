package controllers

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.{LoginEvent, Silhouette}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfileBuilder, SocialProvider, SocialProviderRegistry}
import javax.inject.Inject
import models.user.services.{AuthTokenService, AuthenticateService, UserService}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import utils.silhouette.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

class SocialAuthController @Inject()(components: ControllerComponents,
                                     silhouette: Silhouette[DefaultEnv],
                                     userService: UserService,
                                     authTokenService: AuthTokenService,
                                     authenticateService: AuthenticateService,
                                     authInfoRepository: AuthInfoRepository,
                                     socialProviderRegistry: SocialProviderRegistry,
                                    )(implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  def authenticate(provider: String): Action[AnyContent] = Action.async { implicit request =>
    socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        var queryStringParams = Map[String, Seq[String]]()
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- userService.createOrUpdate(profile.loginInfo, profile.email.get, profile.firstName, profile.lastName, profile.avatarURL)
            _ <- authenticateService.addAuthenticateMethod(user.userID, profile.loginInfo, authInfo)
            authToken <- authTokenService.create(user.userID)
            result <- silhouette.env.authenticatorService.create(profile.loginInfo).flatMap { authenticator =>
              silhouette.env.authenticatorService.init(authenticator).flatMap { token =>
                queryStringParams += ("body" -> Seq(user.toJson(token).toString()))
                silhouette.env.authenticatorService.embed(token, Ok(user.toJson(token)))
              }
            }
          } yield {
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            Redirect("http://localhost:3000/authenticate/", queryStringParams)
          }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with provider $provider"))
    }
  }
}
