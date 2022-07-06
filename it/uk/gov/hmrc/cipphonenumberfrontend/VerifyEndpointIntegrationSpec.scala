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

package uk.gov.hmrc.cipphonenumberfrontend

import org.jsoup.Jsoup
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.WSClient
import play.api.test.Injecting

class VerifyEndpointIntegrationSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneServerPerSuite
    with Injecting {

  private val wsClient = inject[WSClient]
  private val baseUrl = s"http://localhost:$port"

  "GET /verify" should {
    "load the verify page" in {
      val response =
        wsClient
          .url(s"$baseUrl/phone-number/verify")
          .get()
          .futureValue

      response.status shouldBe 200

      val document = Jsoup.parse(response.body)
      document.title() shouldBe "Enter telephone number"
    }
  }

  "POST /verify" should {
    "redirect to otp page when phone number is valid" in {
      val phoneNumber = "07123456789"
      val response =
        wsClient
          .url(s"$baseUrl/phone-number/verify")
          .withFollowRedirects(false)
          .post(Map("phoneNumber" -> phoneNumber))
          .futureValue

      response.status shouldBe 303
      response.header("Location") shouldBe Some(s"/phone-number/verify/otp?phoneNumber=$phoneNumber")
    }

    "return 400 when form is invalid" in {
      val response =
        wsClient
          .url(s"$baseUrl/phone-number/verify")
          .post(Map("phoneNumber" -> "invalid"))
          .futureValue

      response.status shouldBe 400
      val document = Jsoup.parse(response.body)
      document.title() shouldBe "Enter telephone number"
    }
  }
}