package models

import anorm.SqlParser._
import anorm._
import play.api.Play.current
import play.api.db.DB
import play.api.libs.json.Reads._
import play.api.libs.json.{Json, Writes, JsPath, Reads}
import play.api.libs.functional.syntax._

case class Address(street: String, streetNumber: String, zipCode: String, city: String)
case class Customer(name: String, shortName: String, address: Address, id: Option[Long] = None)

object Address {

  implicit val addressReads: Reads[Address] = (
    (JsPath \ "street").read[String] and
      (JsPath \ "streetNumber").read[String] and
      (JsPath \ "zipCode").read[String] and
      (JsPath \ "city").read[String]
    )(Address.apply _)

  implicit val addressWrites = new Writes[Address] {
    def writes(address: Address) = Json.obj(
      "street" -> address.street,
      "streetNumber" -> address.streetNumber,
      "zipCode" -> address.zipCode,
      "city" -> address.city)
  }
}

object Customer {

  val addressParser = {
    get[String]("street") ~
      get[String]("street_number") ~
      get[String]("city") ~
      get[String]("zip_code") map {
      case street ~ streetNumber ~ city ~ zipCode => Address(street, streetNumber, zipCode, city)
    }
  }

  val customerParser = {
    get[Long]("id") ~
      get[String]("name") ~
      get[String]("short_name") ~
      addressParser map {
      case (id ~ name ~ shortName ~ address) => Customer(name, shortName, address, Some(id))
    }
  }

  def getAll: List[Customer] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("Select * from customer")
        select.as(customerParser *)
    }
  }

  def save(customer: Customer): Option[Long] = {
    DB.withConnection {
      implicit c =>
        SQL("insert into customer(name, short_name, street, street_number, zip_code, city) values ({name}, {short_name}, {street}, {street_number}, {zip_code}, {city})")
          .on("name" -> customer.name,
            "short_name" -> customer.shortName,
            "street" -> customer.address.street,
            "street_number" -> customer.address.streetNumber,
            "zip_code" -> customer.address.zipCode,
            "city" -> customer.address.city).executeInsert()
    }
  }

  def findById(id: Long): Option[Customer] = {
    DB.withConnection {
      implicit c =>
        SQL("Select * from customer c where c.id = {id}").on("id" -> id) as customerParser.singleOpt
    }
  }

  def delete(id: Long) {
    DB.withConnection {
      implicit connection =>
        SQL("DELETE FROM customer where id = {id}").on('id -> id).executeUpdate
    }
  }

  def update(id: Long, customer: Customer) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update customer
          set name = {name}, short_name = {short_name},
          street = {street}, street_number = {street_number}, city = {city}, zip_code = {zip_code}
          where id = {id}
        """).on(
          'id -> id,
          'name -> customer.name,
          'short_name -> customer.shortName,
          'city -> customer.address.city,
          'zip_code -> customer.address.zipCode,
          'street -> customer.address.street,
          'street_number -> customer.address.streetNumber).executeUpdate()
    }
  }

  def options: Seq[(String, String)] = {
    getAll map {
      c => c.id.toString -> c.name
    }
  }

  implicit val customerReads: Reads[Customer] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "shortName").read[String] and
      (JsPath \ "address").read[Address]
    )( (name, shortName, address) => Customer(name, shortName, address))

  implicit val customerWrites = new Writes[Customer] {
    def writes(customer: Customer) = Json.obj(
      "name" -> customer.name,
      "shortName" -> customer.shortName,
      "address" -> customer.address)
  }
}