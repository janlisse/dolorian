package controllers

import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import anorm.NotAssigned
import models.{ InvoiceTemplate, Project }
import utils.FormFieldImplicits
import play.api.i18n.Messages

object InvoiceTemplates extends Controller with Secured {

  val invoiceTemplateForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: anorm.Pk[Long]),
      "templateFileKey" -> optional(nonEmptyText),
      "projectId" -> longNumber,
      "hourlyRate" -> of(FormFieldImplicits.bigDecimalFormat))(InvoiceTemplate.apply)(InvoiceTemplate.unapply))

  def index = withAuth {
    username =>
      implicit request =>
        Ok(views.html.invoiceTemplateCreate(invoiceTemplateForm, Project.getAll))
  }

  def list = withAuth {
    username =>
      implicit request =>
        Ok(views.html.invoiceTemplateList(InvoiceTemplate.getAll))
  }

  def delete(id: Long) = withAuth {
    username =>
      implicit request =>
        InvoiceTemplate.delete(id)
        Redirect(routes.InvoiceTemplates.list).flashing("success" -> Messages("template.delete.success"))
  }

  def submit = withAuth(parse.multipartFormData) {
    username =>
      implicit request =>
        request.body.file("templateFile").map {
          file =>
            invoiceTemplateForm.bindFromRequest.fold(
              errors => {
                BadRequest(views.html.invoiceTemplateCreate(errors, Project.getAll))
              },
              template => {
                InvoiceTemplate.save(template, file.ref.file, file.filename)
                Redirect(routes.InvoiceTemplates.list).flashing("success" -> Messages("template.create.success"))
              })
        }.getOrElse(Redirect(routes.InvoiceTemplates.index()).flashing("error" -> Messages("template.missing.file")))
  }
}
