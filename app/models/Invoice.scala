package models

import org.joda.time.DateTime


case class Invoice(templateId: Long, invoiceDate: DateTime, workingHoursTotal: Double)


object Invoice {



  def create(template: InvoiceTemplate) = {



  }



}
