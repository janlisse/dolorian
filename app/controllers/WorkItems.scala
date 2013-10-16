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

object WorkItems extends Controller with Secured {

  def currentMonthStart = new DateTime().dayOfMonth().withMinimumValue()
  def currentMonthEnd = new DateTime().plusMonths(1).dayOfMonth().withMinimumValue()

  val dateStringFormat = DateTimeFormat.forPattern("yyyy-MM-dd")
  
  val timeForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: anorm.Pk[Long]),
      "projectId" -> longNumber,
      "startTime" -> jodaDate("dd/MM/yyyy:HH:mm"),
      "endTime" -> jodaDate("dd/MM/yyyy:HH:mm"),
      "breakTime" -> number,
      "description" -> nonEmptyText)(WorkItem.apply)(WorkItem.unapply).verifying(Messages("workitem.validation.time"), {
        result => result.totalTime.toStandardMinutes.isGreaterThan(Minutes.ZERO)
      }))

  def add = withAuth {
    username =>
      implicit request =>
        Ok(views.html.workItemCreate(timeForm, Project.getAll))
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
        groupBy(_.projectId).map( item => (Project.findById(item._1), item._2))
        Ok(views.html.workItemList(groupedByProject))
  }

  def submit = withAuth {
    username =>
      implicit request =>
        timeForm.bindFromRequest.fold(
          errors => {
            BadRequest(views.html.workItemCreate(errors, Project.getAll))
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
        val workItems = WorkItem.getByRange(range)
        val project = Project.findById(projectId)
        val baos = new ByteArrayOutputStream
        val documentTemplateFactory = new DocumentTemplateFactory
        val template = Template.findById(project.reportTemplateId)
        val jodTemplate = documentTemplateFactory.getTemplate(template.inputStream)

        /** necessary conversion because jodreports/freemarker doesn't work with raw Scala types **/
        val javaWorkItems = workItems.map { item =>
          new WorkItemJavaWrapper(item.description, item.date, item.totalTimeFormatted)
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
  
  def mapRange(startOption: Option[String], endOption: Option[String]) = {
    val startDate = startOption.map( start => dateStringFormat.parseDateTime(start)).getOrElse(currentMonthStart)
    val endDate = endOption.map( start => dateStringFormat.parseDateTime(start)).getOrElse(currentMonthEnd)
    (startDate, endDate)
  }
}