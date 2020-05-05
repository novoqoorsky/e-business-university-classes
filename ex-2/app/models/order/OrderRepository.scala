package models.order

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class OrderTable(tag: Tag) extends Table[Order](tag, "order") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def reference = column[String]("reference")

    def * = (id, reference) <> ((Order.apply _).tupled, Order.unapply)
  }

  private val orders = TableQuery[OrderTable]

  def create(reference: String): Future[Order] = db.run {
    (orders.map(o => (o.reference))
      returning orders.map(_.id)
      into ((reference, id) => Order(id, reference))
      ) += (reference)
  }

  def list(): Future[Seq[Order]] = db.run {
    orders.result
  }

  def getById(id: Long): Future[Order] = db.run {
    orders.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Order]] = db.run {
    orders.filter(_.id === id).result.headOption
  }

  def delete(id: Long): Future[Unit] = db.run(orders.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newOrder: Order): Future[Unit] = {
    val orderToUpdate: Order = newOrder.copy(id)
    db.run(orders.filter(_.id === id).update(orderToUpdate)).map(_ => ())
  }
}

