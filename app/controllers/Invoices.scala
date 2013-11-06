package controllers

import play.api.mvc.Controller
import models.{ Invoice, Project }
import play.api.data.Form
import play.api.data.Forms._
import anorm.NotAssigned
import utils.FormFieldImplicits
import org.joda.time.DateTime
import org.apache.commons.io.output.ByteArrayOutputStream
import models.Template
import play.api.libs.json.Json
import models.InvoiceStatus
import views.html.defaultpages.badRequest
import play.api.i18n.Lang

object Invoices extends Controller with Secured {

  val invoiceForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: anorm.Pk[Long]),
      "projectId" -> longNumber,
      "invoiceDate" -> jodaLocalDate("dd/MM/yyyy"),
      "totalHours" -> of(FormFieldImplicits.bigDecimalFormat),
      "status" -> ignored(InvoiceStatus.Created),
      "invoiceNumber" -> optional(nonEmptyText))(Invoice.apply)(Invoice.unapply))

  def index = withAuth {
    username =>
      implicit request =>
        Ok(views.html.invoiceCreate(invoiceForm, Template.getAll, Invoice.defaultDate))
  }

  def survey = withAuth {
    username =>
      implicit request => Ok(views.html.invoiceSurvey())
  }

  def submit = withAuth {
    username =>
      implicit request =>
        invoiceForm.bindFromRequest.fold(
          errors => {
            BadRequest(views.html.invoiceCreate(errors, Template.getAll, Invoice.defaultDate))
          },
          invoice => {
            val invoiceWithNumber = invoice.copy(invoiceNumber = Some(invoice.generateInvoiceNumber))
            Invoice.save(invoiceWithNumber)
            Ok(Invoice.create(invoiceWithNumber)).as(Template.MIME_TYPE).
              withHeaders("Content-Disposition" -> "attachment; filename=rechnung.odt")
          })
  }

  def list = withAuth {
    username =>
      implicit request =>
        Ok(Json.toJson(Invoice.getAll))
  }

  def delete(id: Long) = withAuth {
    username =>
      implicit request =>
        Invoice.findById(id).map(invoice => {
          if (invoice.status == InvoiceStatus.Created) {
            Invoice.delete(id)
            Ok("Success")
          } else BadRequest("Only invoices with status Created can be deleted!")
        }).getOrElse(BadRequest("Invalid id"))
  }

  def updateStatus(id: Long) = withAuth(parse.json) {
    username =>
      implicit request =>
        val updateJson = request.body
        Invoice.findById(id).map(invoice => {
          val newStatus: InvoiceStatus.InvoiceStatus = InvoiceStatus.withName((updateJson \ "status").as[String])
          Invoice.updateStatus(id, newStatus)
          invoice.incrementInvoiceSequence
          Ok("Success")
        }).getOrElse(BadRequest("Invalid id"))
  }
}
