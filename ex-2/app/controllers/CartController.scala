package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import javax.inject.{Inject, Singleton}
import models.cart.{Cart, CartRepository}
import models.cartproducts.CartProductsRepository
import models.client.ClientRepository
import models.product.ProductRepository
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc._
import utils.silhouette.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CartController @Inject()(cartRepository: CartRepository,
                               clientRepository: ClientRepository,
                               productRepository: ProductRepository,
                               cartProductsRepository: CartProductsRepository,
                               messagesControllerComponents: MessagesControllerComponents,
                               silhouette: Silhouette[DefaultEnv])(implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(messagesControllerComponents) {

  val DISPLAY_CARTS_URL = "/display-carts"

  val cartForm: Form[CreateCartForm] = Form {
    mapping(
      "value" -> number
    )(CreateCartForm.apply)(CreateCartForm.unapply)
  }

  val updateCartForm: Form[UpdateCartForm] = Form {
    mapping(
      "id" -> longNumber,
      "value" -> number
    )(UpdateCartForm.apply)(UpdateCartForm.unapply)
  }

  def addCart(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.cartadd(cartForm))
  }

  def updateCart(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>

    cartRepository.getById(id).map(cart => {
      val filledUpdateForm = updateCartForm.fill(UpdateCartForm(cart.id, cart.value))
      Ok(views.html.cartupdate(filledUpdateForm))
    })

  }

  def deleteCart(id: Long): Action[AnyContent] = Action {
    cartRepository.delete(id)
    Redirect(DISPLAY_CARTS_URL)
  }

  def displayCart(id: Long): Action[AnyContent] = Action.async { implicit request =>
    cartRepository.getByIdOption(id).map {
      case Some(c) => Ok("Cart " + c.value)
      case None => Redirect(routes.HomeController.index())
    }
  }

  def displayCarts(): Action[AnyContent] = Action.async { implicit request =>
    cartRepository.list().map(carts => Ok(views.html.cartsdisplay(carts)))
  }

  def saveAddedCart(): Action[AnyContent] = Action.async { implicit request =>

    cartForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.cartadd(errorForm))
        )
      },
      cart => {
        cartRepository.create(cart.value).map { _ =>
          routes.CartController.addCart()
          Redirect(DISPLAY_CARTS_URL)
        }
      }
    )
  }

  def saveUpdatedCart(): Action[AnyContent] = Action.async { implicit request =>
    updateCartForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.cartupdate(errorForm))
        )
      },
      cart => {
        cartRepository.update(cart.id, Cart(cart.id, cart.value)).map { _ =>
          routes.CartController.updateCart(cart.id)
          Redirect(DISPLAY_CARTS_URL)
        }
      }
    )
  }

  // REACT

  def carts(): Action[AnyContent] = Action.async {
    cartRepository.list().map(carts => Ok(Json.toJson(carts)))
  }

  def cart(id: Long): Action[AnyContent] = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    cartRepository.getById(id).map(cart => Ok(Json.toJson(cart)))
  }

  def addProductToCart(cartUsersEmail: String, productId: Long): Action[AnyContent] = silhouette.SecuredAction {
    clientRepository.getByEmail(cartUsersEmail).map({ client =>
      cartProductsRepository.create(client.cart, productId)
      productRepository.getById(productId).map(product =>
        cartRepository.getById(client.cart).map(cart => {
            cart.value += product.price
            cartRepository.update(cart.id, cart)
          }
        )
      )
    })
    Created("Product added to cart")
  }

  def postCart(): Action[AnyContent] = Action { implicit request =>
    val c = request.body.asJson.get.asInstanceOf[JsObject].value
    cartRepository.create(
      c("value").asInstanceOf[JsString].value.toInt
    )
    Created("Cart created")
  }

  def putCart(): Action[AnyContent] = Action { implicit request =>
    val c = request.body.asJson.get.asInstanceOf[JsObject].value
    val id = c("id").asInstanceOf[JsString].value.toLong
    val cart = Cart(
      id,
      c("value").asInstanceOf[JsString].value.toInt
    )
    cartRepository.update(id, cart)
    Created(Json.toJson(cart))
  }

  def deleteCartExternal(id: Long): Action[AnyContent] = Action { implicit request =>
    deleteCart(id)
    Ok("Cart deleted")
  }
}

case class CreateCartForm(value: Int)
case class UpdateCartForm(id: Long, value: Int)
