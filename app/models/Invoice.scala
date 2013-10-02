package models

import org.joda.time.DateTime
import java.math.BigDecimal
import play.api.db.DB
import anorm._
import anorm.SqlParser._
import anorm.~
import play.api.Play.current

case class Invoice(id: anorm.Pk[Long], templateId: Long, invoiceDate: DateTime, workingHoursTotal: BigDecimal)


object Invoice {

  val invoiceParser = {
    get[Pk[Long]]("id") ~
      get[String]("name") ~
      get[Option[String]]("number") map {
      case (id ~ name ~ number) => {
        Project(id, name, number)
      }
    }
  }

  def getAll: List[Project] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("Select * from project")
        select.as(invoiceParser *)
    }
  }


  def save(template: Invoice) = {



  }



}
