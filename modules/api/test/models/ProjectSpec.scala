package models

import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.test.FakeApplication
import play.api.test.Helpers._

class ProjectSpec extends PlaySpec with OneAppPerSuite {

  implicit override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = inMemoryDatabase())

}


