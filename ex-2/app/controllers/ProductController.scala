package controllers

import javax.inject.{Inject, Singleton}
import models.category.{Category, CategoryRepository}
import models.product.ProductRepository
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductController @Inject()(productRepository: ProductRepository,
                                  categoryRepository: CategoryRepository,
                                  messagesControllerComponents: MessagesControllerComponents)(implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(messagesControllerComponents) {

  val productForm: Form[CreateProductForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "category" -> longNumber,
      "producer" -> longNumber,
    )(CreateProductForm.apply)(CreateProductForm.unapply)
  }

  def addProduct(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val categories = categoryRepository.list()
    categories.map (cat => Ok(views.html.addproduct(productForm, cat)))
  }

  def updateProduct(id: Long): Action[AnyContent] = Action {
    Ok("Update a product")
  }

  def saveUpdatedProduct(): Action[AnyContent] = Action {
    Accepted("Updating a product...")
  }

  def deleteProduct(id: Long): Action[AnyContent] = Action {
    Ok("Delete a product")
  }

  def saveDeletedProduct(): Action[AnyContent] = Action {
    Ok("Deleted a product...")
  }

  def displayProduct(id: Long): Action[AnyContent] = Action.async { implicit request =>
    productRepository.getByIdOption(id).map {
      case Some(p) => Ok("Product " + p.name)
      case None => Redirect(routes.HomeController.index())
    }
  }

  def displayProducts(): Action[AnyContent] = Action.async { implicit request =>
    productRepository.list().map(products => Ok(views.html.displayproducts(products)))
  }

  def saveAddedProduct(): Action[AnyContent] = Action.async { implicit request =>
    val category: Seq[Category] = Seq[Category]()

    productForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.addproduct(errorForm, category))
        )
      },
      product => {
        productRepository.create(product.name, product.description, product.category, product.producer, 21.37).map { _ =>
          Redirect(routes.ProductController.addProduct()).flashing("success" -> "product.created")
        }
      }
    )

  }
}

case class CreateProductForm(name: String, description: String, category: Long, producer: Long)
