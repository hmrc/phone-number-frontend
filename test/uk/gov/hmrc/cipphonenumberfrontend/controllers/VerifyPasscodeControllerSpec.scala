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

package uk.gov.hmrc.cipphonenumberfrontend.controllers

import org.mockito.ArgumentMatchersSugar.{*, any}
import org.mockito.IdiomaticMockito
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import play.twirl.api.Html
import uk.gov.hmrc.cipphonenumberfrontend.connectors.VerifyConnector
import uk.gov.hmrc.cipphonenumberfrontend.models.PhoneNumberAndPasscode
import uk.gov.hmrc.cipphonenumberfrontend.views.html.VerifyPasscodePage
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VerifyPasscodeControllerSpec extends AnyWordSpec
  with Matchers
  with IdiomaticMockito {

  "verifyForm" should {
    "return 200" in new SetUp {
      val result = controller.verifyForm(Some(""))(fakeRequest)
      status(result) shouldBe OK

      mockVerifyPasscodePage.apply(PhoneNumberAndPasscode.form.fill(PhoneNumberAndPasscode("", "")))(*, *) was called
    }

    "return HTML" in new SetUp {
      val result = controller.verifyForm(Some(""))(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")

      mockVerifyPasscodePage.apply(PhoneNumberAndPasscode.form.fill(PhoneNumberAndPasscode("", "")))(*, *) was called
    }

    "pass phone number to form" in new SetUp {
      val phoneNumber = "test"
      controller.verifyForm(Some(phoneNumber))(fakeRequest)
      mockVerifyPasscodePage.apply(PhoneNumberAndPasscode.form.fill(PhoneNumberAndPasscode(phoneNumber, "")))(*, *) was called
    }

    "redirect to landing page when phone number is absent" in new SetUp {
      val result = controller.verifyForm(None)(fakeRequest)
      status(result) shouldBe SEE_OTHER
      headers(result).apply("Location") shouldBe "/phone-number-example-frontend"

      mockVerifyPasscodePage.apply(*)(*, *) wasNever called
    }
  }

  "verify" should {
    "redirect to landing page when phone number passes verification" in new SetUp {
      val phoneNumber = "test"
      val passcode = "test"
      val request = fakeRequest.withFormUrlEncodedBody("phoneNumber" -> phoneNumber, "passcode" -> passcode)
      mockVerifyConnector.verifyPasscode(PhoneNumberAndPasscode(phoneNumber, passcode))(any[HeaderCarrier])
        .returns(Future.successful(Right(HttpResponse(OK,
          """
          {
            "status": "Verified"
          }
          """.stripMargin))))
      val result = controller.verify(request)
      status(result) shouldBe Status.SEE_OTHER
      header("Location", result) shouldBe Some("/phone-number-example-frontend?verified=true")

      mockVerifyConnector.verifyPasscode(PhoneNumberAndPasscode(phoneNumber, passcode))(any[HeaderCarrier]) was called
    }

    "return OK when phone number fails verification" in new SetUp {
      val phoneNumber = "test"
      val passcode = "test"
      val request = fakeRequest.withFormUrlEncodedBody("phoneNumber" -> phoneNumber, "passcode" -> passcode)
      mockVerifyConnector.verifyPasscode(PhoneNumberAndPasscode(phoneNumber, passcode))(any[HeaderCarrier])
        .returns(Future.successful(Right(HttpResponse(OK,
          """
          {
            "status": "Not verified"
          }
          """.stripMargin))))
      val result = controller.verify(request)
      status(result) shouldBe OK

      mockVerifyConnector.verifyPasscode(PhoneNumberAndPasscode(phoneNumber, passcode))(any[HeaderCarrier]) was called
      mockVerifyPasscodePage.apply(PhoneNumberAndPasscode.form.fill(PhoneNumberAndPasscode(phoneNumber, passcode))
        .withError("passcode", "verifyPasscodePage.incorrectPasscode"))(*, *) was called
    }

    "return bad request when form is invalid" in new SetUp {
      val phoneNumber = "test"
      val request = fakeRequest.withFormUrlEncodedBody("phoneNumber" -> phoneNumber)
      val result = controller.verify(request)
      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) shouldBe "some html content"

      mockVerifyPasscodePage.apply(PhoneNumberAndPasscode.form.bind(Map("phoneNumber" -> phoneNumber)))(*, *) was called
    }

    "return bad request when request is invalid" in new SetUp {
      val phoneNumber = "test"
      val passcode = "test"
      val request = fakeRequest.withFormUrlEncodedBody("phoneNumber" -> phoneNumber, "passcode" -> passcode)
      mockVerifyConnector.verifyPasscode(PhoneNumberAndPasscode(phoneNumber, passcode))(any[HeaderCarrier])
        .returns(Future.successful(Left(UpstreamErrorResponse("", BAD_REQUEST))))
      val result = controller.verify(request)
      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) shouldBe "some html content"

      mockVerifyConnector.verifyPasscode(PhoneNumberAndPasscode(phoneNumber, passcode))(any[HeaderCarrier]) was called
      mockVerifyPasscodePage.apply(PhoneNumberAndPasscode.form.fill(PhoneNumberAndPasscode(phoneNumber, passcode))
        .withError("passcode", "verifyPasscodePage.error"))(*, *) was called
    }
  }

  trait SetUp {
    protected val fakeRequest = FakeRequest(POST, "")
    protected val mockVerifyPasscodePage = mock[VerifyPasscodePage]
    protected val mockVerifyConnector = mock[VerifyConnector]

    protected val controller = new VerifyPasscodeController(Helpers.stubMessagesControllerComponents(), mockVerifyPasscodePage, mockVerifyConnector)

    mockVerifyPasscodePage.apply(*)(*, *)
      .returns(Html("some html content"))
  }
}