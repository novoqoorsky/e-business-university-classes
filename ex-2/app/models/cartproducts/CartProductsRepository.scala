package models.cartproducts

import javax.inject.{Inject, Singleton}
import models.cart.CartRepository
import models.product.ProductRepository
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CartProductsRepository @Inject()(dbConfigProvider: DatabaseConfigProvider,
                                       val cartRepository: CartRepository,
                                       val productRepository: ProductRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class CartProductsTable(tag: Tag) extends Table[CartProducts](tag, "cart_products") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def cart = column[Long]("cart")
    def product = column[Long]("product")

    def cart_fk = foreignKey("category_fk", cart, carts)(_.id)
    def producer_fk = foreignKey("producer_fk", product, products)(_.id)

    def * = (id, cart, product) <> ((CartProducts.apply _).tupled, CartProducts.unapply)
  }

  import cartRepository.CartTable
  import productRepository.ProductTable

  private val cartProducts = TableQuery[CartProductsTable]
  private val carts = TableQuery[CartTable]
  private val products = TableQuery[ProductTable]

  def create(cart: Long, product: Long): Future[CartProducts] = db.run {
    (cartProducts.map(cp => (cp.cart, cp.product))
      returning cartProducts.map(_.id)
      into { case ((cart, product), id) => CartProducts(id, cart, product) }
      ) += (cart, product)
  }

  def list(): Future[Seq[CartProducts]] = db.run {
    cartProducts.result
  }

  def getById(id: Long): Future[CartProducts] = db.run {
    cartProducts.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[CartProducts]] = db.run {
    cartProducts.filter(_.id === id).result.headOption
  }

  def getByCart(cart_id: Long): Future[Seq[CartProducts]] = db.run {
    cartProducts.filter(_.cart === cart_id).result
  }

  def delete(id: Long): Future[Unit] = db.run(cartProducts.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newCartProducts: CartProducts): Future[Unit] = {
    val cartProductsToUpdate: CartProducts = newCartProducts.copy(id)
    db.run(cartProducts.filter(_.id === id).update(cartProductsToUpdate)).map(_ => ())
  }
}

