package models

import org.joda.time.{Period, DateTime}
import com.google.gdata.client.spreadsheet.SpreadsheetService
import com.google.gdata.data.spreadsheet._
import java.net.URL
import scala.collection.JavaConversions._
import org.joda.time.format.DateTimeFormat

case class Work(startTime: DateTime, endTime: DateTime, description: String)


object Work {

  val SpreadsheetFeedUrl = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full")

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


  def save(work: Work) {
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
            val dateFormatter = DateTimeFormat.forPattern("dd.MM.yyyy");
            val period = new Period(work.startTime, work.endTime)
            val formattedPeriod = "%02d:%02d".format(period.getHours(), period.getMinutes())
            val cellEntry0 = new CellEntry(27, 2, "=TEXT(C27;\"dddd\")");
            service.insert(worksheet.getCellFeedUrl, cellEntry0);
            val cellEntry1 = new CellEntry(27, 3, dateFormatter.print(work.startTime));
            service.insert(worksheet.getCellFeedUrl, cellEntry1);
            val cellEntry2 = new CellEntry(27, 4, formattedPeriod);
            service.insert(worksheet.getCellFeedUrl, cellEntry2);
            val cellEntry3 = new CellEntry(27, 5, work.description);
            service.insert(worksheet.getCellFeedUrl, cellEntry3);
          }
        )
      }
    )
  }

}
