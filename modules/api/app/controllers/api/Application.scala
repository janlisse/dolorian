package controllers.api

import models._
import play.api._
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {

	def index = Action { implicit request =>

		Ok("hello")
	}

}