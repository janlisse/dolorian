package models

import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.Json
import play.api.test.FakeApplication
import play.api.test.Helpers._

class TemplateSpec extends PlaySpec with OneAppPerSuite {

  implicit override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = inMemoryDatabase())

  "Template should be savable" in {
    val json = """{"name":"Watership Down","location":{"lat":51.235685,"long":-1.309197},"residents":[{"name":"Fiver","age":4,"role":null},{"name":"Bigwig","age":6,"role":"Owsla"}]}"""
    val template = Template(None, "MyTempplate", Json.parse(json))
    val id = Template.save(template)
    id must equal(Some(1))
  }
}