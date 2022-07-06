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

class ValidateEndpointIntegrationSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneServerPerSuite
    with Injecting {

  private val wsClient = inject[WSClient]
  private val baseUrl = s"http://localhost:$port"

  "GET /validate" should {
    "load the validate page" in {
      val response =
        wsClient
          .url(s"$baseUrl/phone-number/validate")
          .get()
          .futureValue

      response.status shouldBe 200

      val document = Jsoup.parse(response.body)
      document.title() shouldBe "Telephone validation service"
    }
  }

  "POST /validate" should {
    "redirect to landing page when form is valid" in {
      val response =
        wsClient
          .url(s"$baseUrl/phone-number/validate")
          .withFollowRedirects(false)
          .post(Map("phoneNumber" -> "01234 567890"))
          .futureValue

      response.status shouldBe 303
      response.header("Location") shouldBe Some("/phone-number?validated=true")
    }

    "return 400 when form is invalid" in {
      val response =
        wsClient
          .url(s"$baseUrl/phone-number/validate")
          .post(Map("phoneNumber" -> "invalid"))
          .futureValue

      response.status shouldBe 400
      val document = Jsoup.parse(response.body)
      document.title() shouldBe "Telephone validation service"
    }
  }
}
