package controllers

import javax.inject.{Inject, Singleton}
import models.discount.{Discount, DiscountRepository}
import models.product.{Product, ProductRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class DiscountController @Inject()(discountRepository: DiscountRepository,
                                   productRepository: ProductRepository,
                                  messagesControllerComponents: MessagesControllerComponents)(implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(messagesControllerComponents) {

  var products: Seq[Product] = Seq[Product]()

  val discountForm: Form[CreateDiscountForm] = Form {
    mapping(
      "product" -> longNumber,
      "percentage" -> number
    )(CreateDiscountForm.apply)(CreateDiscountForm.unapply)
  }

  val updateDiscountForm: Form[UpdateDiscountForm] = Form {
    mapping(
      "id" -> longNumber,
      "product" -> longNumber,
      "percentage" -> number
    )(UpdateDiscountForm.apply)(UpdateDiscountForm.unapply)
  }

  def addDiscount(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val fProducts = productRepository.list()

    for {
      products <- fProducts
    } yield Ok(views.html.discountadd(discountForm, products))
  }

  def updateDiscount(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    retrieveForeignProperties()

    discountRepository.getById(id).map(discount => {
      val filledUpdateForm = updateDiscountForm.fill(UpdateDiscountForm(discount.id, discount.product, discount.percentage))
      Ok(views.html.discountupdate(filledUpdateForm, products))
    })

  }

  def deleteDiscount(id: Long): Action[AnyContent] = Action {
    discountRepository.delete(id)
    Redirect("/display-discounts")
  }

  def displayDiscount(id: Long): Action[AnyContent] = Action.async { implicit request =>
    discountRepository.getByIdOption(id).map {
      case Some(p) => Ok("Discount " + p.id)
      case None => Redirect(routes.HomeController.index())
    }
  }

  def displayDiscounts(): Action[AnyContent] = Action.async { implicit request =>
    discountRepository.list().map(discounts => Ok(views.html.discountsdisplay(discounts)))
  }

  def saveAddedDiscount(): Action[AnyContent] = Action.async { implicit request =>
    retrieveForeignProperties()

    discountForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.discountadd(errorForm, products))
        )
      },
      discount => {
        discountRepository.create(discount.product, discount.percentage).map { _ =>
          routes.DiscountController.addDiscount()
          Redirect("/display-discounts")
        }
      }
    )
  }

  def saveUpdatedDiscount(): Action[AnyContent] = Action.async { implicit request =>
    retrieveForeignProperties()

    updateDiscountForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.discountupdate(errorForm, products))
        )
      },
      discount => {
        discountRepository.update(discount.id, Discount(discount.id, discount.product, discount.percentage)).map { _ =>
          routes.DiscountController.updateDiscount(discount.id)
          Redirect("/display-discounts")
        }
      }
    )
  }

  def retrieveForeignProperties(): Unit = {
    productRepository.list().onComplete {
      case Success(p) => products = p
      case Failure(_) => print("Failure on retrieving products for discount")
    }
  }
}

case class CreateDiscountForm(product: Long, percentage: Int)
case class UpdateDiscountForm(id: Long, product: Long, percentage: Int)