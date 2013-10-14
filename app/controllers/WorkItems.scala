package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import org.joda.time.{ DateTime, Period }
import anorm.NotAssigned
import com.lowagie.text._
import com.lowagie.text.pdf._
import java.io.ByteArrayOutputStream
import com.lowagie.text.pdf.draw.LineSeparator
import org.joda.time.Minutes
import play.api.i18n.Messages
import net.sf.jooreports.templates.DocumentTemplateFactory
import scala.collection.JavaConverters._
import java.util.Arrays

object WorkItems extends Controller with Secured {

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
        Redirect(routes.WorkItems.list).flashing("success" -> Messages("workitem.delete.success"))
  }

  def list = withAuth {
    username =>
      implicit request =>
        Ok(views.html.workItemList(WorkItem.getAll))
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
            Redirect(routes.WorkItems.list).flashing("success" -> Messages("workitem.create.success"))
          })
  }

  def export = withUser {
    user =>
      implicit request =>
        val currentDate = new DateTime
        val lastMonth = currentDate.minusMonths(1)
        val project = Project.getAll.head
        val workItems = WorkItem.getByProjectMonthAndYear(project.id.get, lastMonth.monthOfYear.get, lastMonth.year.get)
        val baos = new ByteArrayOutputStream
        val documentTemplateFactory = new DocumentTemplateFactory
        val template = Template.findById(project.reportTemplateId)
        val jodTemplate = documentTemplateFactory.getTemplate(template.inputStream)

        /** necessary conversion because jodreports/freemarker doesn't work with raw Scala types **/
        val javaWorkItems = workItems.map { item =>
          new WorkItemJavaWrapper(item.description, item.date, item.totalTimeFormatted)
        }.asJava

        val dataMap = Map(
          "month" -> lastMonth.toString("MM/yyyy"),
          "contractor" -> user.name,
          "projectNumber" -> project.number,
          "workItems" -> javaWorkItems,
          "totalHours" -> WorkItem.totalHours(workItems))

        jodTemplate.createDocument(dataMap.toMap.asJava, baos)
        Ok(baos.toByteArray).as(Template.MIME_TYPE).
          withHeaders("Content-Disposition" -> "attachment; filename=report.odt")
  }
}