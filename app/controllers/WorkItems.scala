package controllers

import _root_.models.Project
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import org.joda.time.{DateTime, Period}
import anorm.NotAssigned
import com.lowagie.text._
import com.lowagie.text.pdf._
import java.io.ByteArrayOutputStream
import com.lowagie.text.pdf.draw.LineSeparator
import org.joda.time.Minutes

object WorkItems extends Controller with Secured {

  val catFont = new Font(Font.TIMES_ROMAN, 18, Font.BOLD);

  val timeForm = Form(
    mapping(
      "id" -> ignored(NotAssigned: anorm.Pk[Long]),
      "projectId" -> longNumber,
      "startTime" -> jodaDate("dd/MM/yyyy:HH:mm"),
      "endTime" -> jodaDate("dd/MM/yyyy:HH:mm"),
      "breakTime" -> number,
      "description" -> nonEmptyText
    )
    (WorkItem.apply)(WorkItem.unapply).verifying("Time must be positive", {
      result => result.totalTime.toStandardMinutes.isGreaterThan(Minutes.ZERO)})
  )

  def add = withAuth {
    username => implicit request =>
      Ok(views.html.workItemCreate(timeForm, Project.getAll))
  }

  def delete(id: Long) = withAuth {
    username => implicit request =>
      WorkItem.delete(id)
      Redirect(routes.WorkItems.list).flashing("success" -> "Arbeitszeit erfolgreich gelöscht!")
  }

  def list = withAuth {
    username => implicit request =>
      Ok(views.html.workItemList(WorkItem.getAll))
  }

  def submit = withAuth {
    username => implicit request =>
      timeForm.bindFromRequest.fold(
        errors => {
          BadRequest(views.html.workItemCreate(errors, Project.getAll))
        },
        work => {
          WorkItem.save(work)
          Redirect(routes.WorkItems.list)
        }
      )
  }

  def export = withUser {
    user => implicit request =>

      val currentDate = new DateTime
      val lastMonth = currentDate.minusMonths(1)
      val project = Project.getAll.head
      val workItems = WorkItem.getByProjectMonthAndYear(project.id.get, lastMonth.monthOfYear.get, lastMonth.year.get)

      val document = new Document()
      val baos = new ByteArrayOutputStream()
      try {
        PdfWriter.getInstance(document, baos)
        document.open()

        val font = new Font(Font.HELVETICA, 16, Font.BOLD, java.awt.Color.BLACK)
        val caption = new Paragraph(new Chunk("Leistungserfassung " + lastMonth.toString("MM/yyyy"), font))
        val customerLine = new Paragraph(new Chunk("Auftragnehmer: "+user.name, defaultFont))
        val projectLine = new Paragraph(new Chunk("Projektnummer: "+project.number, defaultFont))
        document.add(caption)
        document.add(Chunk.NEWLINE)
        document.add(customerLine)
        document.add(projectLine)
        val sep = new LineSeparator(1f, 80, java.awt.Color.BLACK, Element.ALIGN_LEFT, 5f)
        document.add(new Chunk(sep))

        val table = new PdfPTable(3)
        val c1 = pdfHeaderCell("Datum")
        table.addCell(c1)
        val c2 = pdfHeaderCell("Stunden")
        table.addCell(c2)

        val c3 = pdfHeaderCell("Leistungen")
        table.addCell(c3)
        table.setHeaderRows(1)

        var hoursTotal: Int = 0
        var minutesTotal: Int = 0
        for (workItem <- workItems) {
          table.addCell(workItem.startTime.toString("dd.MM.yyyy"))
          val hours = workItem.totalTime.getHours
          hoursTotal += hours
          val minutes = workItem.totalTime.getMinutes
          minutesTotal += minutes
          val totalTime = f"$hours%02d:$minutes%02d"
          table.addCell(totalTime)
          table.addCell(workItem.description)
        }

        emptyRow(table)
        emptyRow(table)

        hoursTotal += minutesTotal / 60;
        minutesTotal %= 60;

        table.addCell(pdfHeaderCell("Stunden gesamt"))
        table.addCell(pdfHeaderCell(f"$hoursTotal%02d:$minutesTotal%02d"))
        table.addCell(pdfHeaderCell(" "))

        table.setWidths(Array(15f, 10f, 30f))
        table.setHorizontalAlignment(Element.ALIGN_LEFT)
        table.setSpacingBefore(20)
        table.setSpacingAfter(40)

        document.add(table)

        val table2 = new PdfPTable(3)
        table2.setWidths(Array(10f, 10f, 30f))
        val cell1 = new PdfPCell(new Phrase("Datum"))
        cell1.setBorder(Rectangle.TOP)
        val cell2 = emptyCell
        cell2.setBorder(Rectangle.NO_BORDER)
        val cell3 = new PdfPCell(new Phrase("Stempel / Unterschrift Kunde"))
        cell3.setBorder(Rectangle.TOP)

        table2.addCell(cell1)
        table2.addCell(cell2)
        table2.addCell(cell3)
        table2.setHorizontalAlignment(Element.ALIGN_LEFT)
        document.add(table2)

        document.close()
        Ok(baos.toByteArray()).as("application/pdf")
      }
      catch {
        case e: Exception => BadRequest(e.getMessage())
      }
  }


  def pdfHeaderCell(title: String): PdfPCell = {
    val font = new Font(Font.HELVETICA, 12, Font.BOLD, java.awt.Color.BLACK)
    val c1 = new PdfPCell(new Phrase(title, font))
    c1.setBackgroundColor(java.awt.Color.lightGray)
    c1.setHorizontalAlignment(Element.ALIGN_LEFT)
    c1
  }

  def emptyRow(table: PdfPTable) = {
    table.addCell(emptyCell)
    table.addCell(emptyCell)
    table.addCell(emptyCell)
  }

  def emptyCell(): PdfPCell = {
    new PdfPCell(new Phrase(" "))
  }

  def defaultFont(): Font = {
    new Font(Font.HELVETICA, 10, Font.BOLD, java.awt.Color.BLACK)
  }


}