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

object Invoices extends Controller with Secured {

  val invoiceForm = Form(
    tuple(
      "projectId" -> longNumber,
      "invoiceDate" -> jodaDate("dd/MM/yyyy"),
      "totalHours" -> of(FormFieldImplicits.bigDecimalFormat)))

  def index = withAuth {
    username =>
      implicit request =>
        Ok(views.html.invoiceCreate(invoiceForm, Template.getAll, defaultDate))
  }

  def defaultDate: String = {
    val today = new DateTime
    val lastMonth = today.minusMonths(1)
    lastMonth.dayOfMonth().withMaximumValue().toString("dd/MM/yyyy")
  }

  def submit = withAuth {
    username =>
      implicit request =>
        invoiceForm.bindFromRequest.fold(
          errors => {
            BadRequest(views.html.invoiceCreate(errors, Template.getAll, defaultDate))
          },
          form => {
            val project = Project.findById(form._1)
            val invoice = Invoice(NotAssigned, project, form._2, form._3)
            Ok(Invoice.create(invoice)).as(Template.MIME_TYPE).
              withHeaders("Content-Disposition" -> "attachment; filename=rechnung.odt")
          })
  }

}
