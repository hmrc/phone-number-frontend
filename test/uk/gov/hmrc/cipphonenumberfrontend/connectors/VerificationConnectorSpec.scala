/*
 * Copyright 2023 HM Revenue & Customs
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
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration
import play.api.http.Status.OK
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.cipphonenumberfrontend.config.AppConfig
import uk.gov.hmrc.cipphonenumberfrontend.models.{
  PhoneNumberAndPasscode,
  PhoneNumber
}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}

import scala.concurrent.ExecutionContext.Implicits.global

class VerificationConnectorSpec
    extends AnyWordSpec
    with Matchers
    with WireMockSupport
    with ScalaFutures
    with HttpClientV2Support {

  "verify" should {
    "delegate to http client" in new Setup {
      stubFor(
        post(urlEqualTo(verifyUrl))
          .willReturn(aResponse())
      )

      val result = verifyConnector.sendCode(PhoneNumber("test"))

      await(result).toOption.get.status shouldBe OK

      verify(
        postRequestedFor(
          urlEqualTo("/phone-number-verification/send-code")
        )
          .withRequestBody(equalToJson(s"""{"phoneNumber": "test"}"""))
      )
    }
  }

  "verifyPasscode" should {
    "delegate to http client" in new Setup {
      stubFor(
        post(urlEqualTo(verifyPasscodeUrl))
          .willReturn(aResponse())
      )

      val result =
        verifyConnector.verifyCode(PhoneNumberAndPasscode("test", "test"))

      await(result).toOption.get.status shouldBe OK

      verify(
        postRequestedFor(
          urlEqualTo("/phone-number-verification/verify-code")
        )
          .withRequestBody(
            equalToJson(
              s"""{"phoneNumber": "test", "verificationCode": "test"}"""
            )
          )
      )
    }
  }

  trait Setup {

    protected val verifyUrl: String =
      "/phone-number-verification/send-code"
    protected val verifyPasscodeUrl: String =
      "/phone-number-verification/verify-code"

    private val appConfig = new AppConfig(
      Configuration.from(
        Map(
          "microservice.services.cipphonenumber.host" -> wireMockHost,
          "microservice.services.cipphonenumber.port" -> wireMockPort,
          "microservice.services.cipphonenumber.protocol" -> "http",
          "microservice.services.cipphonenumber.auth-token" -> "fake-token"
        )
      )
    )

    implicit protected val hc: HeaderCarrier = HeaderCarrier()

    protected val verifyConnector = new VerificationConnector(
      httpClientV2,
      appConfig
    )
  }
}
