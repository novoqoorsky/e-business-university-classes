package controllers

import javax.inject.{Inject, Singleton}
import models.product.ProductRepository
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class ProductController @Inject()(productRepository: ProductRepository, messagesControllerComponents: MessagesControllerComponents)(implicit executionContext: ExecutionContext)
  extends MessagesAbstractController(messagesControllerComponents) {

  def addProduct(): Action[AnyContent] = Action {
    Ok("Add a product")
  }

  def saveAddedProduct(): Action[AnyContent] = Action {
    Created("Product saved")
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

  def displayProduct(id: Long): Action[AnyContent] = Action.async {
    productRepository.getByIdOption(id).map {
      case Some(p) => Ok("Product " + p.name)
      case None => Redirect(routes.HomeController.index())
    }
  }

  def displayProducts(): Action[AnyContent] = Action {
    Ok("All products")
  }
}

