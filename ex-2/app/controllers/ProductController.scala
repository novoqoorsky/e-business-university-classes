package controllers

import javax.inject.{Inject, Singleton}
import models.category.{Category, CategoryRepository}
import models.producer.{Producer, ProducerRepository}
import models.product.{Product, ProductRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ProductController @Inject()(productRepository: ProductRepository,
                                  categoryRepository: CategoryRepository,
                                  producerRepository: ProducerRepository,
                                  messagesControllerComponents: MessagesControllerComponents)(implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(messagesControllerComponents) {

  var categories: Seq[Category] = Seq[Category]()
  var producers: Seq[Producer] = Seq[Producer]()

  val productForm: Form[CreateProductForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "category" -> longNumber,
      "producer" -> longNumber,
      "price" -> number
    )(CreateProductForm.apply)(CreateProductForm.unapply)
  }

  val updateProductForm: Form[UpdateProductForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "category" -> longNumber,
      "producer" -> longNumber,
      "price" -> number
    )(UpdateProductForm.apply)(UpdateProductForm.unapply)
  }

  def addProduct(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val fCategories = categoryRepository.list()
    val fProducers = producerRepository.list()

    for {
      categories <- fCategories
      producers <- fProducers
    } yield Ok(views.html.productadd(productForm, categories, producers))
  }

  def updateProduct(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    retrieveForeignProperties()

    productRepository.getById(id).map(product => {
      val filledUpdateForm = updateProductForm.fill(UpdateProductForm(product.id, product.name, product.description, product.category, product.producer, product.price))
      Ok(views.html.productupdate(filledUpdateForm, categories, producers))
    })

  }

  def deleteProduct(id: Long): Action[AnyContent] = Action {
    productRepository.delete(id)
    Redirect("/display-products")
  }

  def displayProduct(id: Long): Action[AnyContent] = Action.async { implicit request =>
    productRepository.getByIdOption(id).map {
      case Some(p) => Ok("Product " + p.name)
      case None => Redirect(routes.HomeController.index())
    }
  }

  def displayProducts(): Action[AnyContent] = Action.async { implicit request =>
    productRepository.list().map(products => Ok(views.html.productsdisplay(products)))
  }

  def saveAddedProduct(): Action[AnyContent] = Action.async { implicit request =>
    retrieveForeignProperties()

    productForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productadd(errorForm, categories, producers))
        )
      },
      product => {
        productRepository.create(product.name, product.description, product.category, product.producer, product.price).map { _ =>
          routes.ProductController.addProduct()
          Redirect("/display-products")
        }
      }
    )
  }

  def saveUpdatedProduct(): Action[AnyContent] = Action.async { implicit request =>
    retrieveForeignProperties()

    updateProductForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productupdate(errorForm, categories, producers))
        )
      },
      product => {
        productRepository.update(product.id, Product(product.id, product.name, product.description, product.category, product.producer, product.price)).map { _ =>
          routes.ProductController.updateProduct(product.id)
          Redirect("/display-products")
        }
      }
    )
  }

  def retrieveForeignProperties(): Unit = {
    categoryRepository.list().onComplete {
      case Success(c) => categories = c
      case Failure(_) => print("Failure on retrieving categories")
    }
    producerRepository.list().onComplete{
      case Success(p) => producers = p
      case Failure(_) => print("Failure on retrieving products")
    }
  }

  // REACT

  def product(): Action[AnyContent] = Action { implicit request =>
    Ok("2137")
  }

  def products(): Action[AnyContent] = Action.async { implicit request =>
    productRepository.list().map(products => Ok(Json.toJson(products)))
  }
}

case class CreateProductForm(name: String, description: String, category: Long, producer: Long, price: Int)
case class UpdateProductForm(id: Long, name: String, description: String, category: Long, producer: Long, price: Int)