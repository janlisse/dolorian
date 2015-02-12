package models

import anorm.SqlParser._
import anorm._
import play.api.Play.current
import play.api.db.DB

case class Address(street: String, streetNumber: String, zipCode: String, city: String) {

  override def toString: String = s"$street $streetNumber, $zipCode $city"
}

case class Customer(id: Option[Long] = None, name: String, shortName: String, address: Address, invoiceSequence: Option[Int] = None) {

  def incrementSequence = {
    invoiceSequence map { seq =>
      val update = copy(invoiceSequence = Some(seq + 1))
      Customer.update(id.get, update)
    }
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
      addressParser ~
      get[Option[Int]]("invoice_sequence") map {
        case (id ~ name ~ shortName ~ address ~ invoiceSequence) => Customer(Some(id), name, shortName, address, invoiceSequence)
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
        SQL("insert into customer(name, short_name, street, street_number, zip_code, city, invoice_sequence) values ({name}, {short_name}, {street}, {street_number}, {zip_code}, {city}, {invoice_sequence})")
          .on("name" -> customer.name,
            "short_name" -> customer.shortName,
            "street" -> customer.address.street,
            "street_number" -> customer.address.streetNumber,
            "zip_code" -> customer.address.zipCode,
            "city" -> customer.address.city,
            "invoice_sequence" -> customer.invoiceSequence.getOrElse(1)).executeInsert()
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
          set invoice_sequence = {invoice_sequence}, name = {name}, short_name = {short_name}, 
          street = {street}, street_number = {street_number}, city = {city}, zip_code = {zip_code}
          where id = {id}
        """).on(
          'id -> id,
          'invoice_sequence -> customer.invoiceSequence.getOrElse(1),
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

}