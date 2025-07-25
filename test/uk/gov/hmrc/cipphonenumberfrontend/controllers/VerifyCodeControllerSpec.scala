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

package uk.gov.hmrc.cipphonenumberfrontend.controllers

import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.{eq => meq, _}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import play.twirl.api.Html
import uk.gov.hmrc.cipphonenumberfrontend.connectors.VerificationConnector
import uk.gov.hmrc.cipphonenumberfrontend.models.PhoneNumberAndPasscode
import uk.gov.hmrc.cipphonenumberfrontend.views.html.VerifyCodePage
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VerifyCodeControllerSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar {

  "verifyForm" should {
    "return 200" in new SetUp {
      val result = controller.verifyCodeForm(Some(""))(fakeRequest)
      status(result) shouldBe OK

      verify(mockVerifyPasscodePage, atLeastOnce())
        .apply(
          meq(PhoneNumberAndPasscode.form.fill(PhoneNumberAndPasscode("", "")))
        )(any(), any())
    }

    "return HTML" in new SetUp {
      val result = controller.verifyCodeForm(Some(""))(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")

      verify(mockVerifyPasscodePage, atLeastOnce())
        .apply(
          meq(PhoneNumberAndPasscode.form.fill(PhoneNumberAndPasscode("", "")))
        )(any(), any())
    }

    "pass phone number to form" in new SetUp {
      val phoneNumber = "test"
      controller.verifyCodeForm(Some(phoneNumber))(fakeRequest)
      verify(mockVerifyPasscodePage, atLeastOnce())
        .apply(
          meq(
            PhoneNumberAndPasscode.form.fill(
              PhoneNumberAndPasscode(phoneNumber, "")
            )
          )
        )(any(), any())
    }

    "redirect to landing page when phone number is absent" in new SetUp {
      val result = controller.verifyCodeForm(None)(fakeRequest)
      status(result) shouldBe SEE_OTHER
      headers(result).apply(
        "Location"
      ) shouldBe "/phone-number-example-frontend"

      verify(mockVerifyPasscodePage, never()).apply(any())(any(), any())
    }
  }

  "verify" should {
    "redirect to landing page when phone number passes verification" in new SetUp {
      val phoneNumber = "test"
      val passcode = "test"
      val request = fakeRequest.withFormUrlEncodedBody(
        "phoneNumber" -> phoneNumber,
        "verificationCode" -> passcode
      )
      when(
        mockVerifyConnector
          .verifyCode(meq(PhoneNumberAndPasscode(phoneNumber, passcode)))(
            any[HeaderCarrier]
          )
      )
        .thenReturn(
          Future.successful(
            Right(
              HttpResponse(
                OK,
                """
          {
            "status": "CODE_VERIFIED",
            "message":"Passcode successfully verified"
          }
          """.stripMargin
              )
            )
          )
        )
      val result = controller.verifyCode(request)
      status(result) shouldBe Status.SEE_OTHER
      header("Location", result) shouldBe Some(
        "/phone-number-example-frontend?verified=true"
      )

      verify(mockVerifyConnector, atLeastOnce())
        .verifyCode(meq(PhoneNumberAndPasscode(phoneNumber, passcode)))(
          any[HeaderCarrier]
        )
    }

    "return BAD_REQUEST when phone number fails verification" in new SetUp {
      val phoneNumber = "test"
      val passcode = "test"
      val request = fakeRequest.withFormUrlEncodedBody(
        "phoneNumber" -> phoneNumber,
        "verificationCode" -> passcode
      )
      when(
        mockVerifyConnector
          .verifyCode(meq(PhoneNumberAndPasscode(phoneNumber, passcode)))(
            any[HeaderCarrier]
          )
      )
        .thenReturn(
          Future.successful(
            Right(
              HttpResponse(
                OK,
                """
          {
            "status": "Not verified"
          }
          """.stripMargin
              )
            )
          )
        )
      val result = controller.verifyCode(request)
      status(result) shouldBe BAD_REQUEST

      verify(mockVerifyConnector, atLeastOnce())
        .verifyCode(meq(PhoneNumberAndPasscode(phoneNumber, passcode)))(
          any[HeaderCarrier]
        )
      verify(mockVerifyPasscodePage, atLeastOnce())
        .apply(
          meq(
            PhoneNumberAndPasscode.form
              .withError("verificationCode", "verifyCodePage.incorrectPasscode")
              .fill(PhoneNumberAndPasscode(phoneNumber, ""))
          )
        )(any(), any())
    }

    "return BAD_REQUEST when passcode has expired" in new SetUp {
      val phoneNumber = "test"
      val passcode = "test"
      val request = fakeRequest.withFormUrlEncodedBody(
        "phoneNumber" -> phoneNumber,
        "verificationCode" -> passcode
      )
      when(
        mockVerifyConnector
          .verifyCode(meq(PhoneNumberAndPasscode(phoneNumber, passcode)))(
            any[HeaderCarrier]
          )
      )
        .thenReturn(
          Future.successful(
            Right(
              HttpResponse(
                OK,
                """
          {
            "code": "1003",
            "message": "The passcode has expired. Request a new passcode"
          }
          """.stripMargin
              )
            )
          )
        )
      val result = controller.verifyCode(request)
      status(result) shouldBe BAD_REQUEST

      verify(mockVerifyConnector, atLeastOnce())
        .verifyCode(meq(PhoneNumberAndPasscode(phoneNumber, passcode)))(
          any[HeaderCarrier]
        )
      verify(mockVerifyPasscodePage, atLeastOnce())
        .apply(
          meq(
            PhoneNumberAndPasscode.form
              .withError("verificationCode", "verifyCodePage.passcodeExpired")
              .fill(PhoneNumberAndPasscode(phoneNumber, ""))
          )
        )(any(), any())
    }

    "return bad request when form is invalid" in new SetUp {
      val phoneNumber = "test"
      val request =
        fakeRequest.withFormUrlEncodedBody("phoneNumber" -> phoneNumber)
      val result = controller.verifyCode(request)
      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) shouldBe "some html content"

      verify(mockVerifyPasscodePage, atLeastOnce())
        .apply(
          meq(
            PhoneNumberAndPasscode.form.bind(Map("phoneNumber" -> phoneNumber))
          )
        )(any(), any())
    }

    "return bad request when request is invalid" in new SetUp {
      val phoneNumber = "test"
      val passcode = "test"
      val request = fakeRequest.withFormUrlEncodedBody(
        "phoneNumber" -> phoneNumber,
        "verificationCode" -> passcode
      )
      when(
        mockVerifyConnector
          .verifyCode(meq(PhoneNumberAndPasscode(phoneNumber, passcode)))(
            any[HeaderCarrier]
          )
      )
        .thenReturn(
          Future.successful(Left(UpstreamErrorResponse("", BAD_REQUEST)))
        )
      val result = controller.verifyCode(request)
      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) shouldBe "some html content"

      verify(mockVerifyConnector, atLeastOnce())
        .verifyCode(meq(PhoneNumberAndPasscode(phoneNumber, passcode)))(
          any[HeaderCarrier]
        )
      verify(mockVerifyPasscodePage, atLeastOnce())
        .apply(
          meq(
            PhoneNumberAndPasscode.form
              .withError("verificationCode", "verifyCodePage.error")
              .fill(PhoneNumberAndPasscode(phoneNumber, ""))
          )
        )(any(), any())
    }
  }

  trait SetUp {
    protected val fakeRequest = FakeRequest(POST, "")
    protected val mockVerifyPasscodePage = mock[VerifyCodePage]
    protected val mockVerifyConnector = mock[VerificationConnector]

    protected val controller = new VerifyCodeController(
      Helpers.stubMessagesControllerComponents(),
      mockVerifyPasscodePage,
      mockVerifyConnector
    )

    when(
      mockVerifyPasscodePage
        .apply(any())(any(), any())
    )
      .thenReturn(Html("some html content"))
  }
}
