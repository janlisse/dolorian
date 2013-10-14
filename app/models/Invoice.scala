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

case class Invoice(id: anorm.Pk[Long], project: Project, invoiceDate: DateTime, workingHoursTotal: BigDecimal) {

  def hourlyRateFormatted = Invoice.formatMoney(project.hourlyRate.doubleValue)
  def projectDescription =  project.description
  def amountFormatted = Invoice.formatMoney(amount)
  def amount = project.hourlyRate.doubleValue * workingHoursTotal.doubleValue
  def amountTaxesFormatted = Invoice.formatMoney(amountTaxes)
  def amountTaxes = amount * 0.19
  def amountTotal = amount + amountTaxes
  def amountTotalFormatted = Invoice.formatMoney(amountTotal)
  def invoiceDateFormatted = invoiceDate.toString("dd.MM.yyyy")
  def invoiceMonth = invoiceDate.toString("MMMM")
  def invoiceYear = invoiceDate.toString("yyyy")
  def invoiceNumber = {
    val customer = project.customer
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
    val template = Template.findById(invoice.project.invoiceTemplateId)
    val jodTemplate = documentTemplateFactory.getTemplate(template.inputStream)
    val dataMap = Map(
      "hours" -> invoice.workingHoursTotal,
      "hourlyRate" -> invoice.hourlyRateFormatted,
      "projectNumber" -> invoice.project.number,
      "invoiceDate" -> invoice.invoiceDateFormatted,
      "invoiceMonth" -> invoice.invoiceMonth,
      "invoiceYear" -> invoice.invoiceYear,
      "invoiceNumber" -> invoice.invoiceNumber,
      "description" -> invoice.projectDescription,
      "amount" -> invoice.amountFormatted,
      "amountTaxes" -> invoice.amountTaxesFormatted,
      "amountTotal" -> invoice.amountTotalFormatted)
    jodTemplate.createDocument(dataMap.toMap.asJava, baos)
    baos.toByteArray
  }
}
