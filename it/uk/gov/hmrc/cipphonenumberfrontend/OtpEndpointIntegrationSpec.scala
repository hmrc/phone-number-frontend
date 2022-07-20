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
import uk.gov.hmrc.cipphonenumberfrontend.utils.DataSteps

class OtpEndpointIntegrationSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneServerPerSuite
    with DataSteps {

  "GET /verify/otp" should {
    "load the verify otp page" in {
      val response =
        wsClient
          .url(s"$baseUrl/phone-number/verify/otp?phoneNumber=07123456789")
          .get()
          .futureValue

      response.status shouldBe 200

      val document = Jsoup.parse(response.body)
      document.title() shouldBe "Enter passcode"
    }
  }

  "POST /verify/otp" should {
    "redirect to landing page when phone number is verified" in {
      val phoneNumber = "07811123456"
      //generate otp
      verify(phoneNumber).futureValue

      //retrieve otp
      val otp = retrieveOtp(phoneNumber).futureValue

      //verify otp (sut)
      val response =
        wsClient
          .url(s"$baseUrl/phone-number/verify/otp")
          .withFollowRedirects(false)
          .post(Map("phoneNumber" -> phoneNumber, "passcode" -> s"${otp.get.passcode}"))
          .futureValue

      response.status shouldBe 303
      response.header("Location") shouldBe Some("/phone-number?verified=true")
    }

    "redirect to landing page when phone number is not verified" in {
      val phoneNumber = "07811123456"

      val response =
        wsClient
          .url(s"$baseUrl/phone-number/verify/otp")
          .withFollowRedirects(false)
          .post(Map("phoneNumber" -> phoneNumber, "passcode" -> "123456"))
          .futureValue

      response.status shouldBe 303
      response.header("Location") shouldBe Some("/phone-number?verified=false")
    }

    "return 400 when form is invalid" in {
      val response =
        wsClient
          .url(s"$baseUrl/phone-number/verify/otp")
          .post(Map("phoneNumber" -> "invalid"))
          .futureValue

      response.status shouldBe 400
      val document = Jsoup.parse(response.body)
      document.title() shouldBe "Enter passcode"
    }
  }
}
