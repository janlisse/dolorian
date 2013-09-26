package utils

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat


object FormatUtil {

  def formatTime(date: DateTime, pattern: String = "HH:mm") = DateTimeFormat.forPattern(pattern).print(date)

  def formatDate(date: DateTime, pattern: String = "dd.MM.yyyy") = DateTimeFormat.forPattern(pattern).print(date)

}
