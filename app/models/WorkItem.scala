package models

import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.data.spreadsheet._
import java.net.URL
import scala.collection.JavaConversions._
import org.joda.time.format.DateTimeFormat
import org.joda.time.{Period, DateTime}


import anorm._
import play.api.db.DB
import models.AnormExtension._
import play.api.Play.current

case class Project(id: anorm.Pk[Long], name: String)


case class WorkItem(id: anorm.Pk[Long], projectId: Long, startTime: DateTime, endTime: DateTime, breakTime: Int, description: String) {

  def totalTime() : Period = {
    val endTimeMinusBreak = endTime.minusMinutes(breakTime)
    new Period(startTime, endTimeMinusBreak)
  }
}

object Project {

  def getAll(): List[Project] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("Select * from project")
        select().map(row =>
          Project(row[anorm.Pk[Long]]("id"), row[String]("name"))
        ).toList
    }
  }

  def save(name: String) : Option[Long] = {
    DB.withConnection {
      implicit c =>
        SQL("insert into project(name) values ({name})")
          .on("name" -> name).executeInsert()
    }
  }

  def delete(id: Long) {
    DB.withConnection { implicit connection =>
      SQL("""
          DELETE FROM project where id = {id}
          """).on(
        'id -> id
      ).executeUpdate
    }
  }

}

object WorkItem {

  val SpreadsheetFeedUrl = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full")


  def export() = {

  }


  def getAll(): List[WorkItem] = {
    DB.withConnection {
      implicit c =>
        val select = SQL("Select * from work")
        select().map(row =>
          WorkItem(row[anorm.Pk[Long]]("id"), row[Long]("projectId"), row[DateTime]("startTime"), row[DateTime]("endTime"), row[Int]("breakTime"), row[String]("description"))
        ).toList
    }
  }

  def delete(id: Long) {
    DB.withConnection { implicit connection =>
      SQL("""
          DELETE FROM work where id = {id}
          """).on(
        'id -> id
      ).executeUpdate
    }
  }

  def save(workItem: WorkItem) : Option[Long] = {
    DB.withConnection {
      implicit c =>
        SQL("insert into work(projectId, startTime,endTime,breakTime,description) values ({projectId},{startTime},{endTime},{breakTime},{description},)")
          .on("projectId" -> workItem.projectId,
          "startTime" -> workItem.startTime,
          "endTime" -> workItem.endTime,
          "breakTime" -> workItem.breakTime,
          "description" -> workItem.description
        ).executeInsert()
    }
  }

  def newSpreadsheetService(username: String, password: String, name: String = "Jetray"): SpreadsheetService = {
    val service = new SpreadsheetService(name)
    service.setUserCredentials(username, password)
    service
  }

  def spreadsheet(service: SpreadsheetService, name: String): Either[Exception, SpreadsheetEntry] = try {
    val serviceOption = service.getFeed(SpreadsheetFeedUrl, classOf[SpreadsheetFeed]).getEntries.find(_.getTitle.getPlainText == name)
    serviceOption.toRight(new NoSuchElementException("No spreadsheet with title '%s'" format name))
  } catch {
    case e: Exception => Left(e)
  }

  def worksheet(service: SpreadsheetService, spreadsheet: SpreadsheetEntry, name: String): Either[Exception, WorksheetEntry] = try {
    val o = service.getFeed(spreadsheet.getWorksheetFeedUrl, classOf[WorksheetFeed]).getEntries.find(_.getTitle.getPlainText == name)
    o.toRight(new NoSuchElementException("No worksheet with title '%s'" format name))
  } catch {
    case e: Exception => Left(e)
  }

  def saveToGoogleSpreadsheet(work: WorkItem) {
    val service = newSpreadsheetService("jan.lisse", "4ever1968")
    spreadsheet(service, "Zeiterfassung").fold(
      exception => {
        exception.printStackTrace()
      },
      spreadsheetEntry => {
        worksheet(service, spreadsheetEntry, "Sheet1").fold(
          error => {
            error.printStackTrace()
          },
          worksheet => {
            val dateFormatter = DateTimeFormat.forPattern("dd.MM.yyyy")
            val period = new Period(new DateTime(work.startTime), new DateTime(work.endTime))
            val formattedPeriod = "%02d:%02d".format(period.getHours(), period.getMinutes())
            val cellEntry0 = new CellEntry(27, 2, "=TEXT(C27;\"dddd\")")
            service.insert(worksheet.getCellFeedUrl, cellEntry0)
            val cellEntry1 = new CellEntry(27, 3, dateFormatter.print(new DateTime(work.startTime)))
            service.insert(worksheet.getCellFeedUrl, cellEntry1)
            val cellEntry2 = new CellEntry(27, 4, formattedPeriod)
            service.insert(worksheet.getCellFeedUrl, cellEntry2)
            val cellEntry3 = new CellEntry(27, 5, work.description)
            service.insert(worksheet.getCellFeedUrl, cellEntry3)
          }
        )
      }
    )
  }
}
