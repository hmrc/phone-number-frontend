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

package uk.gov.hmrc.cipphonenumberfrontend

import org.jsoup.Jsoup
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status.OK
import play.api.libs.ws.ahc.AhcCurlRequestLogger
import uk.gov.hmrc.cipphonenumberfrontend.utils.DataSteps

class VerifyPasscodeEndpointIntegrationSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneServerPerSuite
    with DataSteps {

  "GET /verify/passcode" should {
    "load the verify passcode page" in {
      val response =
        wsClient
          .url(s"$baseUrl/phone-number-example-frontend/verify/passcode?phoneNumber=07123456789")
          .withRequestFilter(AhcCurlRequestLogger())
          .get()
          .futureValue

      response.status shouldBe 200

      val document = Jsoup.parse(response.body)
      document.title() shouldBe "Enter passcode"
    }
  }

  "POST /verify/passcode" should {
    "redirect to landing page when phone number is verified" in {
      //generate passcode
      verify("07811123456").futureValue

      //retrieve passcode
      val maybePhoneNumberAndPasscode = retrievePasscode("+447811123456").futureValue

      //verify passcode (sut)
      val response =
        wsClient
          .url(s"$baseUrl/phone-number-example-frontend/verify/passcode")
          .withRequestFilter(AhcCurlRequestLogger())
          .withFollowRedirects(false)
          .post(Map("phoneNumber" -> "07811123456", "passcode" -> s"${maybePhoneNumberAndPasscode.get.passcode}"))
          .futureValue

      response.status shouldBe 303
      response.header("Location") shouldBe Some("/phone-number-example-frontend?verified=true")
    }

    "return OK when phone number is not verified" in {
      val response =
        wsClient
          .url(s"$baseUrl/phone-number-example-frontend/verify/passcode")
          .withRequestFilter(AhcCurlRequestLogger())
          .withFollowRedirects(false)
          .post(Map("phoneNumber" -> "07811123456", "passcode" -> "123456"))
          .futureValue

      response.status shouldBe OK
      val document = Jsoup.parse(response.body)
      document.title() shouldBe "Enter passcode"
    }

    "return 400 when form is invalid" in {
      val response =
        wsClient
          .url(s"$baseUrl/phone-number-example-frontend/verify/passcode")
          .withRequestFilter(AhcCurlRequestLogger())
          .post(Map("phoneNumber" -> "invalid"))
          .futureValue

      response.status shouldBe 400
      val document = Jsoup.parse(response.body)
      document.title() shouldBe "Enter passcode"
    }
  }
}
