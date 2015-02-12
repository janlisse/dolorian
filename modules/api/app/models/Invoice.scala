package models

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

import anorm.SqlParser._
import anorm.{~, _}
import org.joda.time.{DateTime, LocalDate}
import play.api.Play.current
import play.api.db.DB

object InvoiceStatus extends Enumeration {
  type InvoiceStatus = Value
  val Created, Sent, Paid = Value
}

case class Invoice(id: Option[Long], projectId: Long, invoiceDate: LocalDate, workingHoursTotal: BigDecimal, status: InvoiceStatus.Value, invoiceNumber: Option[String]) {

  lazy val project: Project = DB.withConnection { implicit connection =>
    SQL("SELECT * FROM project p WHERE p.id = {id}").on(
      'id -> projectId).as(Project.projectParser.single)
  }

  def hourlyRateFormatted = Invoice.formatMoney(project.hourlyRate.doubleValue)
  def projectDescription = project.description
  def amountFormatted = Invoice.formatMoney(amount)
  def amount = project.hourlyRate.doubleValue * workingHoursTotal.doubleValue
  def amountTaxesFormatted = Invoice.formatMoney(amountTaxes)
  def amountTaxes = amount * 0.19
  def amountTotal = amount + amountTaxes
  def amountTotalFormatted = Invoice.formatMoney(amountTotal)
  def invoiceDateFormatted = invoiceDate.toString("dd.MM.yyyy")
  def invoiceMonth = invoiceDate.toString("MMMM", Locale.GERMAN)
  def invoiceYear = invoiceDate.toString("yyyy")
  def generateInvoiceNumber = {
    invoiceNumber.getOrElse({
      val customer = project.customer
      val seqNumber = customer.invoiceSequence.getOrElse(1)
      val shortName = customer.shortName
      s"$shortName-$invoiceYear-$seqNumber"
    })
  }
  def incrementInvoiceSequence = project.customer.incrementSequence

}

object Invoice {


  import models.AnormExtension._
  import play.api.libs.json._

  val MONEY_FORMAT = NumberFormat.getCurrencyInstance(Locale.GERMANY)

  implicit val invoiceWrites = new Writes[Invoice] {
    def writes(invoice: Invoice): JsValue = {
      Json.obj(
        "id" -> invoice.id.get,
        "projectId" -> invoice.projectId,
        "number" -> invoice.invoiceNumber,
        "invoiceDate" -> invoice.invoiceDate,
        "totalHours" -> invoice.workingHoursTotal.doubleValue,
        "totalAmount" -> invoice.amountTotal,
        "status" -> invoice.status.toString)
    }
  }

  val invoiceParser = {
      get[Long]("id") ~
      get[Long]("project_id") ~
      get[LocalDate]("invoice_date") ~
      get[BigDecimal]("total_hours") ~
      get[String]("invoice_status") ~
      get[String]("invoice_number") map {
      case (id ~ project_id ~ invoice_date ~ total_hours ~ status ~ invoice_number) => Invoice(Some(id), project_id, invoice_date, total_hours, InvoiceStatus.withName(status), Some(invoice_number))
    }
  }

  def formatMoney(value: Double): String = MONEY_FORMAT.format(value)

  def defaultDate: String = {
    val today = new DateTime
    val lastMonth = today.minusMonths(1)
    lastMonth.dayOfMonth().withMaximumValue().toString("dd/MM/yyyy")
  }

  def save(invoice: Invoice): Option[Long] = {
    DB.withConnection {
      implicit c =>
        SQL( """insert into invoice(project_id, invoice_number, invoice_date, total_hours, invoice_status) values({project_id}, {invoice_number}, {invoice_date}, {total_hours}, {invoice_status})""")
          .on("project_id" -> invoice.projectId,
            "invoice_number" -> invoice.invoiceNumber,
            "invoice_date" -> invoice.invoiceDate,
            "total_hours" -> invoice.workingHoursTotal,
            "invoice_status" -> invoice.status.toString,
            "invoice_number" -> invoice.invoiceNumber).executeInsert()
    }
  }

  def updateStatus(id: Long, status: InvoiceStatus.InvoiceStatus) = {
    DB.withConnection { implicit connection =>
      SQL(
        "update invoice set invoice_status = {status} where id = {id}")
        .on("id" -> id, "status" -> status.toString).executeUpdate()
    }
  }

  def getAll: List[Invoice] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("Select * from invoice order by invoice_date DESC")
        select.as(invoiceParser *)
    }
  }

  def findById(id: Long): Option[Invoice] = {
    DB.withConnection {
      implicit c =>
        SQL("Select * from invoice i where i.id = {id}").on("id" -> id) as invoiceParser.singleOpt
    }
  }

  def delete(id: Long) {
    DB.withConnection {
      implicit connection =>
        SQL("DELETE FROM invoice where id = {id}").on('id -> id).executeUpdate
    }
  }

}