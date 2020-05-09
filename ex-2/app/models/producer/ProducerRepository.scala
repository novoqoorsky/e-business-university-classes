package models.producer

import javax.inject.{Inject, Singleton}
import models.address.AddressRepository
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProducerRepository @Inject() (dbConfigProvider: DatabaseConfigProvider, val addressRepository: AddressRepository)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class ProducerTable(tag: Tag) extends Table[Producer](tag, "producer") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def address = column[Long]("address")

    def address_fk = foreignKey("address_fk", address, addresses)(_.id)

    def * = (id, name, address) <> ((Producer.apply _).tupled, Producer.unapply)
  }

  import addressRepository.AddressTable

  private val producers = TableQuery[ProducerTable]
  private val addresses = TableQuery[AddressTable]

  def create(name: String, address: Long): Future[Producer] = db.run {
    (producers.map(p => (p.name, p.address))
      returning producers.map(_.id)
      into { case ((name, address), id) => Producer(id, name, address) }
      ) += (name, address)
  }

  def list(): Future[Seq[Producer]] = db.run {
    producers.result
  }

  def getById(id: Long): Future[Producer] = db.run {
    producers.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Producer]] = db.run {
    producers.filter(_.id === id).result.headOption
  }

  def delete(id: Long): Future[Unit] = db.run(producers.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newProducer: Producer): Future[Unit] = {
    val producerToUpdate: Producer = newProducer.copy(id)
    db.run(producers.filter(_.id === id).update(producerToUpdate)).map(_ => ())
  }
}
