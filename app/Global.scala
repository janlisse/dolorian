import anorm.Id
import models.{ WorkItem, DetailedWorkItem, SimpleWorkItem, Project, User }
import org.joda.time.DateTime
import play.api.{ Play, GlobalSettings, Application }
import play.api.Play.current
import models.Customer
import models.Address
import java.math.BigDecimal
import play.api.Logger
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{ GetObjectRequest, ObjectMetadata, CannedAccessControlList, PutObjectRequest }
import java.util.UUID
import java.io.InputStream
import java.io.File
import models.S3Configuration
import org.joda.time.LocalDate
import models.Template
import models.ClasspathTemplate
import java.io.FileReader

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    S3Configuration
    InitialData.insert()
  }
}

object InitialData {

  def insert() = {

    if (Template.getAll.isEmpty) {
      val invoiceKey = "/public/templates/dis_template.odt"
      val file = Play.application.getFile(invoiceKey)
      Template.saveToDB(ClasspathTemplate(Id(1l), "defaultInvoiceTemplate", "key"))
    }

    if (Play.isDev && User.findAll.isEmpty) {
      User.create(User("Joshi", "jan@test.de", "secret"))
      Customer.save(Customer(Id(1l),"DIS AG","DIS", Address("","","","")))
      Project.save(Project(Id(1l), "xyz-123", "Something useful", 1l, 1l, 1l, new BigDecimal(50.00)))
      Seq(
        DetailedWorkItem(Id(1l), 1l, new DateTime(2013, 7, 12, 10, 0), new DateTime(2013, 7, 12, 20, 20), Some(30), new LocalDate(),"something"),
        DetailedWorkItem(Id(2l), 1l, new DateTime(2013, 7, 14, 10, 15), new DateTime(2013, 7, 14, 18, 0), None, new LocalDate(),"anything")).foreach(WorkItem.save)
    }
  }

}




