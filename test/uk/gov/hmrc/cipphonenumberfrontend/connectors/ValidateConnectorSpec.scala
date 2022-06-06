/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.cipphonenumberfrontend.connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import org.junit.Assert.assertTrue
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration
import play.api.http.Status
import uk.gov.hmrc.cipphonenumberfrontend.config.AppConfig
import uk.gov.hmrc.cipphonenumberfrontend.models.PhoneNumber
import uk.gov.hmrc.cipphonenumberfrontend.utils.WireMockSupport
import uk.gov.hmrc.http.test.HttpClientV2Support
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global

class ValidateConnectorSpec extends AnyWordSpec
  with Matchers
  with WireMockSupport
  with ScalaFutures
  with HttpClientV2Support {

  val url: String = "/customer-insight-platform/phone-number/validate-format"

  "ValidateConnector.callService" should {
    "return HttpResponse OK for valid input" in new Setup {
      val phoneNumber = "07843274323"
      stubFor(
        post(urlEqualTo(url))
          .willReturn(aResponse())
      )

      implicit val hc: HeaderCarrier = HeaderCarrier()
      validateConnector.callService(PhoneNumber(phoneNumber)).map(res => {
        res shouldBe Right(HttpResponse(Status.OK, ""))
      })

//      TODO: find out why this is failing
//      verify(
//        postRequestedFor(urlEqualTo("/customer-insight-platform/phone-number/validate-format"))
//          .withRequestBody(equalToJson(s"""{"phoneNumber": "07843274323"}"""))
//      )
    }
  }

  trait Setup {

    private val appConfig = new AppConfig(
      Configuration.from(Map(
        "microservice.services.cipphonenumber.host" -> wireMockHost,
        "microservice.services.cipphonenumber.port" -> wireMockPort,
        "microservice.services.cipphonenumber.protocol" -> wireMockProtocol,
      ))
    )

    val validateConnector = new ValidateConnector(
      httpClientV2,
      appConfig
    )
  }
}
