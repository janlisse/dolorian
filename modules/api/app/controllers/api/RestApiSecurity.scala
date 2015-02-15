package controllers.api

import models.User
import play.api.cache.Cache
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import play.api.Play.current

import scala.concurrent.Future


class AuthenticatedRequest[A](val user: User, request: Request[A]) extends WrappedRequest[A](request)


object Authenticated extends ActionBuilder[AuthenticatedRequest] {

  val AuthTokenHeader = "X-AUTH-TOKEN"
  val AuthTokenCookieKey = "X-AUTH-TOKEN"

  def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {
    val maybeToken = request.headers.get(AuthTokenHeader)
    maybeToken.fold(Future.successful(Unauthorized("Not logged in.")))(
      token => {
        val mayBeUser = Cache.getAs[User](token)
        mayBeUser.fold(Future.successful(Unauthorized("Not logged in.")))(user =>
          block(new AuthenticatedRequest(user, request)))
      })
  }
}

case class Login(email: String, password: String)

object Login {
  implicit val writes = Json.format[Login]
}


object RestApi extends Controller {

  import Authenticated._
  import scala.concurrent.duration._

  implicit class ResultWithToken(result: Result) {
    def withToken(token: (String, User)): Result = {
      Cache.set(token._1, token._2, Duration(30, MINUTES))
      result.withCookies(Cookie(AuthTokenCookieKey, token._1, None, httpOnly = false))
    }

    def discardingToken(token: String): Result = {
      Cache.remove(token)
      result.discardingCookies(DiscardingCookie(name = AuthTokenCookieKey))
    }
  }

  def login = Action(parse.json) {
    request => request.body.asOpt[Login].fold(BadRequest(Json.obj("err" -> "No credentials")))(login => {
      val res = User.authenticate(login.email, login.password) map {
        user =>
          val token = java.util.UUID.randomUUID().toString
          Ok(Json.obj(
            "authToken" -> token,
            "userId" -> user.email
          )).withToken(token, user)
      }
      res.getOrElse(NotFound(Json.obj("err" -> "User Not Found or Password Invalid")))
    })
  }

  def logout = Authenticated { request =>
    request.headers.get(AuthTokenHeader) map {
      token =>
        Ok("Success").discardingToken(token)
    } getOrElse BadRequest(Json.obj("err" -> "No Token"))
  }

  def ping = Authenticated { request => {
    Ok(Json.obj("userId" -> request.user.email))
  }
  }
}
