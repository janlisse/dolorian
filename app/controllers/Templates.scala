package controllers

import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import anorm.NotAssigned
import models.{ Template, Project }
import utils.FormFieldImplicits
import play.api.i18n.Messages

object Templates extends Controller with Secured {

  val templateForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: anorm.Pk[Long]),
      "name" -> nonEmptyText,
      "key" -> ignored("")
      )(Template.apply)(Template.unapply))

  def add = withAuth {
    username =>
      implicit request =>
        Ok(views.html.templateCreate(templateForm))
  }

  def list = withAuth {
    username =>
      implicit request =>
        Ok(views.html.templateList(Template.getAll))
  }

  def delete(id: Long) = withAuth {
    username =>
      implicit request =>
        Template.delete(id)
        Redirect(routes.Templates.list).flashing("success" -> Messages("template.delete.success"))
  }

  def submit = withAuth(parse.multipartFormData) {
    username =>
      implicit request =>
        request.body.file("templateFile").map {
          file =>
            templateForm.bindFromRequest.fold(
              errors => {
                BadRequest(views.html.templateCreate(errors))
              },
              template => {
                Template.save(template, file.ref.file, file.filename)
                Redirect(routes.Templates.list).flashing("success" -> Messages("template.create.success"))
              })
        }.getOrElse(Redirect(routes.Templates.add()).flashing("error" -> Messages("template.missing.file")))
  }
}
