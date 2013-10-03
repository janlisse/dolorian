package controllers

import play.api.mvc.Controller
import models.{InvoiceTemplate, Invoice, Project}
import play.api.data.Form
import play.api.data.Forms._
import anorm.NotAssigned
import utils.FormFieldImplicits
import org.joda.time.DateTime
import org.apache.commons.io.output.ByteArrayOutputStream


object Invoices extends Controller with Secured {

  val invoiceForm = Form(
    tuple(
      "templateId" -> longNumber,
      "invoiceDate" -> jodaDate("dd/MM/yyyy"),
      "totalHours" -> of(FormFieldImplicits.bigDecimalFormat)
    )
  )

  def index = withAuth {
    username => implicit request =>
      Ok(views.html.invoiceCreate(invoiceForm, InvoiceTemplate.getAll,defaultDate) )
  }


  def defaultDate: String = {
    val today = new DateTime()
    val lastMonth = today.minusMonths(1)
    val defaultDate = lastMonth.dayOfMonth().withMaximumValue().toString("dd/MM/yyyy")
    defaultDate
  }

  def submit = withAuth {
    username => implicit request =>
      invoiceForm.bindFromRequest.fold(
        errors => {
          BadRequest(views.html.invoiceCreate(errors, InvoiceTemplate.getAll, defaultDate))
        },
        form => {
          val template = InvoiceTemplate.findById(form._1)
          val invoice = Invoice(NotAssigned, template, form._2, form._3)
          Ok(Invoice.create(invoice)).as(InvoiceTemplate.MIME_TYPE)
        }
      )
  }


}
