package controllers

import play.api.mvc.{ Action, Controller }
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views._
import models.User
import play.api.i18n.Messages

object Authentication extends Controller {

  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text) verifying ("Invalid email or password", result => result match {
        case (email, password) => User.authenticate(email, password).isDefined
      }))

  def login = Action {
    implicit Request =>
      Ok(views.html.login(loginForm))
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.Authentication.login).withNewSession.flashing(
      "success" -> Messages("logout.success"))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => Redirect(routes.Application.index).withSession("email" -> user._1))
  }

}
