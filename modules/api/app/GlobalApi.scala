import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future

object GlobalApi extends GlobalSettings {
	
	// 404 - page not found error
	override def onHandlerNotFound (request: RequestHeader) = Future.successful(
		NotFound()
	)
	
	// 500 - internal server error
	override def onError (request: RequestHeader, throwable: Throwable) = Future.successful(
		InternalServerError()
	)
	
	// called when a route is found, but it was not possible to bind the request parameters
	override def onBadRequest (request: RequestHeader, error: String) = Future.successful(
		BadRequest("Bad Request: " + error)
	)

}