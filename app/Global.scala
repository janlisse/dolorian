import anorm.Id
import models.{ WorkItem, Project, User }
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

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    S3Configuration
    TestData.insert()
    Logger.info("Application has started...");
  }
}

/**
 * Initialize test data
 */
object TestData {

  def insert() = {
    if (Play.isDev && User.findAll.isEmpty) {
      Customer.save(Customer(Id(1l), "big company", "BIG", Address("mainstreet", "45", "12345", "denver"), Some(1)))
      User.create(User("Joshi", "jan@test.de", "secret"))
      Project.save(Project(Id(1l), "xyz-123", "Something useful", 1l, 1l, 1l, new BigDecimal(50.00)))
      Seq(
        WorkItem(Id(1l), 1l, new DateTime(2013, 7, 12, 10, 0), new DateTime(2013, 7, 12, 20, 20), 30, "something"),
        WorkItem(Id(2l), 1l, new DateTime(2013, 7, 14, 10, 15), new DateTime(2013, 7, 14, 18, 0), 30, "anything")).foreach(WorkItem.save)
    }
  }

}




