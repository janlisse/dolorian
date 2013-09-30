package models

import anorm._
import play.api.db.DB
import java.math.BigDecimal
import anorm.SqlParser._
import anorm.~


case class InvoiceTemplate(id: Pk[Long], templateFile: String, projectId: Long, hourlyRate: BigDecimal, invoiceNumber: String)


object InvoiceTemplate {

  import play.api.Play.current

  val invoiceTemplateParser = {
    get[Pk[Long]]("id") ~
      get[String]("templateFile") ~
      get[Long]("projectId") ~
      get[BigDecimal]("hourlyRate") ~
      get[String]("invoiceNumber") map {
      case (id ~ templateFile ~ projectId ~ hourlyRate ~ invoiceNumber) => {
        InvoiceTemplate(id, templateFile, projectId, hourlyRate, invoiceNumber)
      }
    }
  }

  def save(invoiceTemplate: InvoiceTemplate): Option[Long] = {
    DB.withConnection {
      implicit c =>
        SQL("insert into invoice_template(templateFile, projectId, hourlyRate, invoiceNumber) values ({templateFile},{projectId},{hourlyRate},{invoiceNumber})")
          .on("templateFile" -> invoiceTemplate.templateFile,
          "projectId" -> invoiceTemplate.projectId,
          "hourlyRate" -> invoiceTemplate.hourlyRate,
          "invoiceNumber" -> invoiceTemplate.invoiceNumber
        ).executeInsert()
    }
  }

  def getAll(): List[InvoiceTemplate] = {
    DB.withConnection {
      implicit c =>
        SQL("Select * from invoice_template") as (invoiceTemplateParser *)
    }
  }

  def findById(id: Long): InvoiceTemplate = {
    DB.withConnection {
      implicit c =>
        SQL("Select * from invoice_template t where t.id = {id}").on("id" -> id) as (invoiceTemplateParser.single)
    }
  }
}