package controllers

import play.api.mvc.{ Action, Controller }
import models.Project
import play.api.data.Form
import play.api.data.Forms._
import anorm.NotAssigned
import play.api.i18n.Messages
import utils.FormFieldImplicits

object Projects extends Controller with Secured {

  val projectForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: anorm.Pk[Long]),
      "number" -> nonEmptyText,
      "description" -> nonEmptyText,
      "customerId" -> longNumber,
      "invoiceTemplateId" -> longNumber,
      "reportTemplateId" -> longNumber,
      "hourlyRate" -> of(FormFieldImplicits.bigDecimalFormat))(Project.apply)(Project.unapply))

  def delete(id: Long) = withAuth {
    username =>
      implicit request =>
        Project.delete(id)
        Redirect(routes.Projects.list).flashing("success" -> Messages("project.delete.success"))
  }

  def add = withAuth {
    username =>
      implicit request =>
        Ok(views.html.projectCreate(projectForm))
  }

  def list = withAuth {
    username =>
      implicit request =>
        Ok(views.html.projectList(Project.getAll))
  }

  def submit = withAuth {
    username =>
      implicit request =>
        projectForm.bindFromRequest.fold(
          errors => {
            BadRequest(views.html.projectCreate(errors))
          },
          project => {
            Project.save(project)
            Redirect(routes.Projects.list).flashing("success" -> Messages("project.create.success", project.number))
          })
  }
  
  def edit(id: Long) = withAuth {
    username =>
      implicit request =>
        Project.findById(id).map { project =>
          Ok(views.html.projectEdit(id, projectForm.fill(project)))
        }.getOrElse(NotFound)
  }

  def update(id: Long) = withAuth {
    username =>
      implicit request =>
        projectForm.bindFromRequest.fold(
          errors => BadRequest(views.html.projectEdit(id, errors)),
          customer => {
            Project.update(id, customer)
            Redirect(routes.Projects.list).flashing("success" -> Messages("project.edit.success"))
          })
  }

}
