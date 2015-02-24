package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import org.joda.time.{ DateTime, Period }
import anorm.NotAssigned
import java.io.ByteArrayOutputStream
import org.joda.time.Minutes
import play.api.i18n.Messages
import net.sf.jooreports.templates.DocumentTemplateFactory
import scala.collection.JavaConverters._
import java.util.Arrays
import org.joda.time.format.DateTimeFormat
import org.joda.time.Duration
import org.joda.time.format.PeriodFormatterBuilder
import org.joda.time.PeriodType
import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.data.validation.ValidationError
import models.WorkItem._
import com.google.inject._

@Singleton
class WorkItems @Inject()(templateStorage: TemplateStorage) extends Controller with Secured {

  def currentMonthStart = new DateTime().dayOfMonth().withMinimumValue()
  def currentMonthEnd = new DateTime().plusMonths(1).dayOfMonth().withMinimumValue()

  val dateStringFormat = DateTimeFormat.forPattern("yyyy-MM-dd")

  // Define serialisation for JSON validation error messages.
  implicit val JsPathWrites = Writes[JsPath](p => JsString(p.toString))

  implicit val ValidationErrorWrites =
    Writes[ValidationError](e => {
      JsString(Messages(e.message, e.args: _*))
    })

  implicit val jsonValidateErrorWrites = (
    (JsPath \ "path").write[JsPath] and
      (JsPath \ "errors").write[Seq[ValidationError]] tupled)

  val timeForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: anorm.Pk[Long]),
      "projectId" -> longNumber,
      "startTime" -> jodaDate("dd/MM/yyyy:HH:mm"),
      "endTime" -> jodaDate("dd/MM/yyyy:HH:mm"),
      "breakTime" -> optional(number),
      "date" -> jodaLocalDate("dd/MM/yyyy"),
      "description" -> nonEmptyText)(DetailedWorkItem.apply)(DetailedWorkItem.unapply).verifying(Messages("workitem.validation.time"), {
        result => result.duration.toStandardMinutes.isGreaterThan(Minutes.ZERO)
      }))

  def add = withAuth {
    username =>
      implicit request =>
        Ok(views.html.workItemCreate(timeForm))
  }

  def quickTrack = withAuth {
    username =>
      implicit request =>
        Ok(views.html.workItemQuickTrack())
  }

  def delete(id: Long) = withAuth {
    username =>
      implicit request =>
        WorkItem.delete(id)
        Redirect(routes.WorkItems.list()).flashing("success" -> Messages("workitem.delete.success"))
  }

  def list(startOption: Option[String], endOption: Option[String]) = withAuth {
    username =>
      implicit request =>
        //TODO handle bad input
        val groupedByProject = WorkItem.getByRange(mapRange(startOption, endOption)).
          groupBy(_.projectId).map(item => (Project.findById(item._1).get, item._2))
        Ok(views.html.workItemList(groupedByProject))
  }

  def submit = withAuth {
    username =>
      implicit request =>
        timeForm.bindFromRequest.fold(
          errors => {
            BadRequest(views.html.workItemCreate(errors))
          },
          work => {
            WorkItem.save(work)
            Redirect(routes.WorkItems.list()).flashing("success" -> Messages("workitem.create.success"))
          })
  }

  def export(startOption: Option[String], endOption: Option[String], projectId: Long) = withUser {
    user =>
      implicit request =>
        val range = mapRange(startOption, endOption)
        val workItems = WorkItem.getByRange(range).filter(_.projectId == projectId)
        val project = Project.findById(projectId).get
        val baos = new ByteArrayOutputStream
        val documentTemplateFactory = new DocumentTemplateFactory
        val template = Template.findById(project.reportTemplateId)
        val jodTemplate = documentTemplateFactory.getTemplate(templateStorage.load(template.key))

        /** necessary conversion because jodreports/freemarker doesn't work with raw Scala types **/
        val javaWorkItems = workItems.map {
          case item:DetailedWorkItem => new WorkItemJavaWrapper(item.description, item.dateFormatted, item.durationFormatted, item.startTimeFormatted, item.endTimeFormatted, item.breakTimeFormatted)
          case item:SimpleWorkItem => new WorkItemJavaWrapper(item.description, item.dateFormatted, item.durationFormatted, null, null, null)
        }.asJava

        val dataMap = Map(
          "month" -> range._1.toString("MM/yyyy"),
          "contractor" -> user.name,
          "projectNumber" -> project.number,
          "workItems" -> javaWorkItems,
          "totalHours" -> WorkItem.totalHours(workItems))

        jodTemplate.createDocument(dataMap.toMap.asJava, baos)
        Ok(baos.toByteArray).as(Template.MIME_TYPE).
          withHeaders("Content-Disposition" -> "attachment; filename=report.odt")
  }

  def submitSimpleWorkItem = withAuth(parse.json) {
    username =>
      implicit request =>
        val workItemJson = request.body
        workItemJson.validate[SimpleWorkItem].fold(
          valid = { simpleWorkItem =>
            WorkItem.save(simpleWorkItem)
            Ok("Saved")
          },
          invalid = { errors => BadRequest(Json.toJson(errors))
          })
  }
  
  def edit(id: Long) = withAuth {
    username =>
      implicit request =>
        WorkItem.findById(id).map { workItem =>
          Ok(views.html.workItemEdit(id, timeForm.fill(workItem)))
        }.getOrElse(NotFound)
  }

  def update(id: Long) = withAuth {
    username =>
      implicit request =>
        timeForm.bindFromRequest.fold(
          errors => BadRequest(views.html.workItemEdit(id, errors)),
          workItem => {
            WorkItem.update(id, workItem)
            Redirect(routes.WorkItems.list(None,None)).flashing("success" -> Messages("workitem.edit.success"))
          })
  }
  

  def mapRange(startOption: Option[String], endOption: Option[String]) = {
    val startDate = startOption.map(start => dateStringFormat.parseDateTime(start)).getOrElse(currentMonthStart)
    val endDate = endOption.map(start => dateStringFormat.parseDateTime(start)).getOrElse(currentMonthEnd)
    (startDate, endDate)
  }
}