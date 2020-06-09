package controllers

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.{LogoutEvent, Silhouette}
import javax.inject.{Inject, Singleton}
import models.user.services.IndexRenderService
import play.api.Environment
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import utils.silhouette.DefaultEnv

import scala.concurrent.ExecutionContext

@Singleton
class HomeController @Inject()(messagesControllerComponents: MessagesControllerComponents,
                               silhouette: Silhouette[DefaultEnv],
                               environment: Environment,
                               ws: WSClient,
                               indexRenderService: IndexRenderService,
                               authInfoRepository: AuthInfoRepository)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(messagesControllerComponents) with I18nSupport {

  def index: Action[AnyContent] = Action {
    Ok(views.html.index("Welcome to Strong@Home store!"))
  }

  def signOut: Action[AnyContent] = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, Ok)
  }
}
