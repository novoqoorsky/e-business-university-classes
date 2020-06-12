package models.order

import javax.inject.{Inject, Singleton}
import models.cart.CartRepository
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderRepository @Inject() (dbConfigProvider: DatabaseConfigProvider,
                                 val cartRepository: CartRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class OrderTable(tag: Tag) extends Table[Order](tag, "order") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def reference = column[String]("reference")
    def cart = column[Long]("cart")

    def cartFk = foreignKey("cart_fk", cart, carts)(_.id)

    def * = (id, reference, cart) <> ((Order.apply _).tupled, Order.unapply)
  }

  import cartRepository.CartTable

  private val orders = TableQuery[OrderTable]
  private val carts = TableQuery[CartTable]

  def create(reference: String, cart: Long): Future[Order] = db.run {
    (orders.map(o => (o.reference, o.cart))
      returning orders.map(_.id)
      into { case ((reference, cart), id) => Order(id, reference, cart) }
      ) += (reference, cart)
  }

  def list(): Future[Seq[Order]] = db.run {
    orders.result
  }

  def getByIds(ids: Seq[Long]): Future[Seq[Order]] = db.run {
    orders.filter(_.id inSet ids).result
  }

  def getById(id: Long): Future[Order] = db.run {
    orders.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Order]] = db.run {
    orders.filter(_.id === id).result.headOption
  }

  def getByReference(reference: String): Future[Order] = db.run {
    orders.filter(_.reference === reference).result.head
  }

  def delete(id: Long): Future[Unit] = db.run(orders.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newOrder: Order): Future[Unit] = {
    val orderToUpdate: Order = newOrder.copy(id)
    db.run(orders.filter(_.id === id).update(orderToUpdate)).map(_ => ())
  }
}

