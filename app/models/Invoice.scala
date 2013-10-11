package models

import org.joda.time.DateTime
import java.math.BigDecimal
import play.api.db.DB
import anorm._
import anorm.SqlParser._
import anorm.~
import play.api.Play.current
import net.sf.jooreports.templates.DocumentTemplateFactory
import org.apache.commons.io.output.ByteArrayOutputStream
import java.text.NumberFormat
import java.util.Locale

case class Invoice(id: anorm.Pk[Long], template: InvoiceTemplate, invoiceDate: DateTime, workingHoursTotal: BigDecimal) {

  def hourlyRateFormatted = Invoice.formatMoney(template.hourlyRate.doubleValue)
  def projectNumber = Project.findById(template.projectId).number
  def projectDescription = Project.findById(template.projectId).description
  def amountFormatted = Invoice.formatMoney(amount)
  def amount = template.hourlyRate.doubleValue * workingHoursTotal.doubleValue
  def amountTaxesFormatted = Invoice.formatMoney(amountTaxes)
  def amountTaxes =  amount * 0.19
  def amountTotal = amount + amountTaxes
  def amountTotalFormatted = Invoice.formatMoney(amountTotal)
  def invoiceDateFormatted = invoiceDate.toString("dd.MM.yyyy")
  def invoiceMonth = invoiceDate.toString("MMMM")
  def invoiceYear = invoiceDate.toString("yyyy")
  def invoiceNumber = {
    val customer = template.project.customer
    val seqNumber = customer.getAndIncrementSequence.getOrElse(1)
    val shortName = customer.shortName
    s"$shortName-$invoiceYear-$seqNumber"
  }
}


object Invoice {

  import collection.JavaConverters._

  val MONEY_FORMAT = NumberFormat.getCurrencyInstance(Locale.GERMANY)

  def formatMoney(value: Double): String = {
     return MONEY_FORMAT.format(value)
  }

  def create(invoice: Invoice) = {
    val baos = new ByteArrayOutputStream
    val documentTemplateFactory = new DocumentTemplateFactory
    val jodTemplate = documentTemplateFactory.getTemplate(invoice.template.inputStream)
    val dataMap = Map(
      "hours" -> invoice.workingHoursTotal,
      "hourlyRate" -> invoice.hourlyRateFormatted,
      "projectNumber" -> invoice.projectNumber,
      "invoiceDate"     -> invoice.invoiceDateFormatted,
      "invoiceMonth"   -> invoice.invoiceMonth,
      "invoiceYear"    -> invoice.invoiceYear,
      "invoiceNumber"   -> invoice.invoiceNumber,
      "description" -> invoice.projectDescription,
      "amount" -> invoice.amountFormatted,
      "amountTaxes" -> invoice.amountTaxesFormatted,
      "amountTotal" -> invoice.amountTotalFormatted
    )
    jodTemplate.createDocument(dataMap.toMap.asJava, baos)
    baos.toByteArray
  }
}
