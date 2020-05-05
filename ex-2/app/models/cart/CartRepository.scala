package models.cart

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CartRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class CartTable(tag: Tag) extends Table[Cart](tag, "cart") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def value = column[Double]("value")

    def * = (id, value) <> ((Cart.apply _).tupled, Cart.unapply)
  }

  private val carts = TableQuery[CartTable]

  def create(value: Double): Future[Cart] = db.run {
    (carts.map(c => (c.value))
      returning carts.map(_.id)
      into ((value, id) => Cart(id, value))
      ) += (value)
  }

  def list(): Future[Seq[Cart]] = db.run {
    carts.result
  }

  def getById(id: Long): Future[Cart] = db.run {
    carts.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Cart]] = db.run {
    carts.filter(_.id === id).result.headOption
  }

  def delete(id: Long): Future[Unit] = db.run(carts.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newCart: Cart): Future[Unit] = {
    val cartToUpdate: Cart = newCart.copy(id)
    db.run(carts.filter(_.id === id).update(cartToUpdate)).map(_ => ())
  }
}