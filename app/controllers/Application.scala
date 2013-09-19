package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.Work
import org.joda.time.{DateTime, Period}

object Application extends Controller {

  val timeForm = Form(
    mapping(
      "startTime" -> jodaDate("dd/MM/yyyy:HH:mm"),
      "endTime" -> jodaDate("dd/MM/yyyy:HH:mm"),
      "description" -> nonEmptyText
    )(Work.apply)(Work.unapply)
  )


  def index = Action {
    Ok(views.html.index(timeForm))
  }

  def submit = Action { implicit request =>
    timeForm.bindFromRequest.fold(
      errors => {
        println (errors)
        BadRequest(views.html.index(errors))
      },
      work => {
        val period = new Period(work.startTime, work.endTime);
        val hours = period.getHours();
        val minutes = period.getMinutes();
        println("Worked: "+hours+":"+minutes)
        Work.save(work)
        Redirect(routes.Application.index())
      }
    )
  }
  
}