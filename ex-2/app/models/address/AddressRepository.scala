package models.address

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class AddressTable(tag: Tag) extends Table[Address](tag, "address") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def city = column[String]("city")
    def streetName = column[String]("street_name")
    def houseNumber = column[Int]("house_number")
    def postalCode = column[String]("postal_code")

    def * = (id, city, streetName, houseNumber, postalCode) <> ((Address.apply _).tupled, Address.unapply)
  }

  private val addresses = TableQuery[AddressTable]

  def create(city: String, streetName: String, houseNumber: Int, postalCode: String): Future[Address] = db.run {
    (addresses.map(a => (a.city, a.streetName, a.houseNumber, a.postalCode))
      returning addresses.map(_.id)
      into { case ((city, streetName, houseNumber, postalCode), id) => Address(id, city, streetName, houseNumber, postalCode) }
      ) += (city, streetName, houseNumber, postalCode)
  }

  def list(): Future[Seq[Address]] = db.run {
    addresses.result
  }

  def getById(id: Long): Future[Address] = db.run {
    addresses.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Address]] = db.run {
    addresses.filter(_.id === id).result.headOption
  }

  def delete(id: Long): Future[Unit] = db.run(addresses.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newAddress: Address): Future[Unit] = {
    val addressToUpdate: Address = newAddress.copy(id)
    db.run(addresses.filter(_.id === id).update(addressToUpdate)).map(_ => ())
  }
}
