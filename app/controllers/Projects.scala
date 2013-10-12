package controllers

import play.api.mvc.{ Action, Controller }
import models.Project
import play.api.data.Form
import play.api.data.Forms._
import anorm.NotAssigned
import play.api.i18n.Messages

object Projects extends Controller with Secured {

  val projectForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: anorm.Pk[Long]),
      "number" -> nonEmptyText,
      "description" -> nonEmptyText,
      "customerId" -> longNumber)(Project.apply)(Project.unapply))

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

}
