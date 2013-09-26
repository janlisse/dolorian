package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import org.joda.time.{DateTime, Period}
import anorm.NotAssigned


object WorkItems extends Controller with Secured {

  val timeForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: anorm.Pk[Long]),
      "projectId" -> longNumber,
      "startTime" -> jodaDate("dd/MM/yyyy:HH:mm"),
      "endTime" -> jodaDate("dd/MM/yyyy:HH:mm"),
      "breakTime" -> number,
      "description" -> nonEmptyText
    )(WorkItem.apply)(WorkItem.unapply)
  )


  def index = withAuth {
    username => implicit request =>
      Ok(views.html.workItemCreate(timeForm, Project.getAll()))
  }

  def delete(id: Long) = withAuth {
    username => implicit request =>
      WorkItem.delete(id)
      Redirect(routes.WorkItems.list).flashing("success" -> "Arbeitszeit erfolgreich gelöscht!")
  }

  def list = withAuth {
    username => implicit request =>
      Ok(views.html.workItemList(WorkItem.getAll()))
  }

  def submit = withAuth {
    username => implicit request =>
      timeForm.bindFromRequest.fold(
        errors => {
          BadRequest(views.html.workItemCreate(errors, Project.getAll()))
        },
        work => {
          WorkItem.save(work)
          Redirect(routes.WorkItems.list)
        }
      )
  }

  def export = withAuth {
    username => implicit request =>
      WorkItem.export()
      Redirect(routes.WorkItems.list).flashing("success" -> "Arbeitszeiten exportiert!")
  }

}