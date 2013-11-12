package models

import anorm._
import play.api.db.DB
import models.AnormExtension._
import anorm.SqlParser._
import play.api.Play.current
import java.math.BigDecimal
import java.io.File
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile

case class Project(id: anorm.Pk[Long], number: String, description: String, customerId: Long,
  invoiceTemplateId: Long, reportTemplateId: Long, hourlyRate: BigDecimal) {

  lazy val customer: Customer = DB.withConnection { implicit connection =>
    SQL("SELECT * FROM customer c WHERE c.id = {id}").on(
      'id -> customerId).as(Customer.customerParser.single)
  }
}

object Project {

  val projectParser = {
    get[Pk[Long]]("id") ~
      get[String]("number") ~
      get[String]("description") ~
      get[Long]("customer_id") ~
      get[Long]("invoice_template_id") ~
      get[Long]("report_template_id") ~
      get[BigDecimal]("hourly_rate") map {
        case (id ~ name ~ number ~ customerId ~ invoiceTemplateId ~ reportTemplateId ~ hourlyRate) => {
          Project(id, name, number, customerId, invoiceTemplateId, reportTemplateId, hourlyRate)
        }
      }
  }

  def getAll: List[Project] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("Select * from project")
        select.as(projectParser *)
    }
  }

  def save(project: Project): Option[Long] = {
    DB.withConnection {
      implicit c =>
        SQL("""insert into project(number, description, customer_id, invoice_template_id, 
            report_template_id, hourly_rate) 
            values ({number},{description}, {customerId}, {invoiceTemplateId}, {reportTemplateId}, {hourlyRate})""")
          .on("description" -> project.description,
            "number" -> project.number,
            "customerId" -> project.customerId,
            "invoiceTemplateId" -> project.invoiceTemplateId,
            "reportTemplateId" -> project.reportTemplateId,
            "hourlyRate" -> project.hourlyRate).executeInsert()
    }
  }

  def delete(id: Long) {
    DB.withConnection {
      implicit connection =>
        SQL("DELETE FROM project where id = {id}").on('id -> id).executeUpdate
    }
  }

  def findById(id: Long): Option[Project] = {
    DB.withConnection {
      implicit c =>
        SQL("Select * from project p where p.id = {id}").on("id" -> id) as projectParser.singleOpt
    }
  }

  def options: Seq[(String, String)] = {
    getAll map {
      c => c.id.toString -> c.number
    }
  }

  def update(id: Long, project: Project) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update project
          set number = {number}, description = {description}, customer_id = {customerId}, invoice_template_id = {invoiceTemplateId}, 
          report_template_id = {reportTemplateId}, hourly_rate = {hourlyRate}
          where id = {id}
        """).on(
          'id -> id,
          "number" -> project.number,
          "description" -> project.description,
          "customerId" -> project.customerId,
          "invoiceTemplateId" -> project.invoiceTemplateId,
          "reportTemplateId" -> project.reportTemplateId,
          "hourlyRate" -> project.hourlyRate).executeUpdate()
    }
  }

}