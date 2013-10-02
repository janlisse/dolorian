package controllers

import play.api.mvc.Controller
import models.{InvoiceTemplate, Invoice, Project}
import play.api.data.Form
import play.api.data.Forms._
import anorm.NotAssigned
import utils.FormFieldImplicits
import org.joda.time.DateTime


object Invoices extends Controller with Secured {

  val invoiceForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: anorm.Pk[Long]),
      "templateId" -> longNumber,
      "invoiceDate" -> jodaDate("dd/MM/yyyy:HH:mm"),
      "totalHours" -> of(FormFieldImplicits.bigDecimalFormat)
    )(Invoice.apply)(Invoice.unapply)
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
        invoice => {
          Invoice.save(invoice)
          Redirect(routes.Projects.list)
        }
      )
  }


}
