package controllers

import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import anorm.NotAssigned
import models.{InvoiceTemplate, Project}
import utils.FormFieldImplicits


object InvoiceTemplates extends Controller with Secured {

  import utils.FormFieldImplicits._

  val invoiceTemplateForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: anorm.Pk[Long]),
      "templateFile" -> nonEmptyText,
      "projectId" -> longNumber,
      "hourlyRate" -> of(FormFieldImplicits.bigDecimalFormat),
      "invoiceNumber" -> nonEmptyText
    )(InvoiceTemplate.apply)(InvoiceTemplate.unapply)
  )

  def index = withAuth {
    username => implicit request =>
      Ok(views.html.invoiceTemplateCreate(invoiceTemplateForm,Project.getAll()))
  }

  def list = withAuth {
    username => implicit request =>
      Ok(views.html.invoiceTemplateList(InvoiceTemplate.getAll))
  }

  def submit = withAuth {
    username => implicit request =>
      invoiceTemplateForm.bindFromRequest.fold(
        errors => {
          BadRequest(views.html.invoiceTemplateCreate(errors, Project.getAll()))
        },
        template => {
          InvoiceTemplate.save(template)
          Redirect(routes.InvoiceTemplates.list)
        }
      )
  }
}
