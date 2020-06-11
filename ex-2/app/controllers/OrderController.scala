package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import javax.inject.{Inject, Singleton}
import models.clientorders.ClientOrdersRepository
import models.order.{Order, OrderRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{Action, _}
import utils.silhouette.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderController @Inject()(orderRepository: OrderRepository,
                                clientOrdersRepository: ClientOrdersRepository,
                                messagesControllerComponents: MessagesControllerComponents,
                                silhouette: Silhouette[DefaultEnv])(implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(messagesControllerComponents) {

  val DISPLAY_ORDERS_URL = "/display-orders"

  val orderForm: Form[CreateOrderForm] = Form {
    mapping(
      "reference" -> nonEmptyText,
      "cart" -> longNumber
    )(CreateOrderForm.apply)(CreateOrderForm.unapply)
  }

  val updateOrderForm: Form[UpdateOrderForm] = Form {
    mapping(
      "id" -> longNumber,
      "reference" -> nonEmptyText,
      "cart" -> longNumber
    )(UpdateOrderForm.apply)(UpdateOrderForm.unapply)
  }

  def addOrder(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.orderadd(orderForm))
  }

  def updateOrder(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>

    orderRepository.getById(id).map(order => {
      val filledUpdateForm = updateOrderForm.fill(UpdateOrderForm(order.id, order.reference, order.cart))
      Ok(views.html.orderupdate(filledUpdateForm))
    })

  }

  def deleteOrder(id: Long): Action[AnyContent] = Action {
    orderRepository.delete(id)
    Redirect(DISPLAY_ORDERS_URL)
  }

  def displayOrder(id: Long): Action[AnyContent] = Action.async { implicit request =>
    orderRepository.getByIdOption(id).map {
      case Some(c) => Ok("Order " + c.reference)
      case None => Redirect(routes.HomeController.index())
    }
  }

  def displayOrders(): Action[AnyContent] = Action.async { implicit request =>
    orderRepository.list().map(orders => Ok(views.html.ordersdisplay(orders)))
  }

  def saveAddedOrder(): Action[AnyContent] = Action.async { implicit request =>

    orderForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.orderadd(errorForm))
        )
      },
      order => {
        orderRepository.create(order.reference, order.cart).map { _ =>
          routes.OrderController.addOrder()
          Redirect(DISPLAY_ORDERS_URL)
        }
      }
    )
  }

  def saveUpdatedOrder(): Action[AnyContent] = Action.async { implicit request =>
    updateOrderForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.orderupdate(errorForm))
        )
      },
      order => {
        orderRepository.update(order.id, Order(order.id, order.reference, order.cart)).map { _ =>
          routes.OrderController.updateOrder(order.id)
          Redirect(DISPLAY_ORDERS_URL)
        }
      }
    )
  }

  // REACT

  def clientsOrders(client: Long): Action[AnyContent] = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    clientOrdersRepository.getByClient(client).map(clientOrder => clientOrder.map(_.order))
      .flatMap(orderIds => orderRepository.getByIds(orderIds)).map(orders => Ok(Json.toJson(orders)))
  }

  def orders(): Action[AnyContent] = Action.async {
    orderRepository.list().map(orders => Ok(Json.toJson(orders)))
  }

  def postOrder(): Action[AnyContent] = Action { implicit request =>
    val o = request.body.asJson.get.asInstanceOf[JsObject].value
    orderRepository.create(
      o("reference").asInstanceOf[JsString].value,
      o("cart").asInstanceOf[JsString].value.toLong
    )
    Created("Order created")
  }

  def putOrder(): Action[AnyContent] = Action { implicit request =>
    val o = request.body.asJson.get.asInstanceOf[JsObject].value
    val id = o("id").asInstanceOf[JsString].value.toLong
    val order = Order(
      id,
      o("reference").asInstanceOf[JsString].value,
      o("cart").asInstanceOf[JsString].value.toLong
    )
    orderRepository.update(id, order)
    Created(Json.toJson(order))
  }

  def deleteOrderExternal(id: Long): Action[AnyContent] = Action { implicit request =>
    deleteOrder(id)
    Ok("Cart deleted")
  }
}

case class CreateOrderForm(reference: String, cart: Long)
case class UpdateOrderForm(id: Long, reference: String, cart: Long)
