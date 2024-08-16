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
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import play.twirl.api.Html
import uk.gov.hmrc.cipphonenumberfrontend.connectors.VerifyConnector
import uk.gov.hmrc.cipphonenumberfrontend.models.{
  VerificationResponse,
  PhoneNumber
}
import uk.gov.hmrc.cipphonenumberfrontend.views.html.VerifyPage
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VerifyControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  "verifyForm" should {
    "return 200" in new SetUp {
      val result = controller.verifyForm()(fakeRequest)
      status(result) shouldBe Status.OK

      verify(mockVerifyPage, atLeastOnce())
        .apply(meq(PhoneNumber.form))(any(), any())
    }

    "return HTML" in new SetUp {
      val result = controller.verifyForm()(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")

      verify(mockVerifyPage, atLeastOnce())
        .apply(meq(PhoneNumber.form))(any(), any())
    }

    "load empty form by default" in new SetUp {
      controller.verifyForm()(fakeRequest)
      verify(mockVerifyPage, atLeastOnce())
        .apply(meq(PhoneNumber.form))(any(), any())
    }

    "load form with phone number when supplied" in new SetUp {
      val phoneNumber = "test"
      controller.verifyForm(Some(phoneNumber))(fakeRequest)
      verify(mockVerifyPage, atLeastOnce())
        .apply(meq(PhoneNumber.form.fill(PhoneNumber(phoneNumber))))(
          any(),
          any()
        )
    }
  }

  "verify" should {
    "redirect to verify passcode when request is valid" in new SetUp {
      val jsValue: JsValue = Json.parse(
        """{"status" : "VERIFIED", "message":"Phone verification code successfully sent"}""".stripMargin
      )
      val phoneNumber = "test"
      val request =
        fakeRequest.withFormUrlEncodedBody("phoneNumber" -> phoneNumber)
      when(
        mockVerifyConnector
          .verify(meq(PhoneNumber(phoneNumber)))(any[HeaderCarrier])
      )
        .thenReturn(
          Future.successful(
            Right(
              HttpResponse(
                Status.OK,
                jsValue,
                Map.empty
              )
            )
          )
        )
      val result = controller.verify(request)
      status(result) shouldBe Status.SEE_OTHER

      verify(mockVerifyConnector, atLeastOnce())
        .verify(meq(PhoneNumber(phoneNumber)))(any[HeaderCarrier])
    }

    "return bad request when form is invalid" in new SetUp {
      val result = controller.verify(fakeRequest)
      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) shouldBe "some html content"

      verify(mockVerifyPage, atLeastOnce())
        .apply(
          meq(PhoneNumber.form.withError("phoneNumber", "error.required"))
        )(any(), any())
    }

    "return bad request when request is invalid" in new SetUp {
      val phoneNumber = "test"
      val request =
        fakeRequest.withFormUrlEncodedBody("phoneNumber" -> phoneNumber)
      when(
        mockVerifyConnector
          .verify(meq(PhoneNumber(phoneNumber)))(any[HeaderCarrier])
      )
        .thenReturn(
          Future.successful(Left(UpstreamErrorResponse("", Status.BAD_REQUEST)))
        )
      val result = controller.verify(request)
      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) shouldBe "some html content"

      verify(mockVerifyConnector, atLeastOnce())
        .verify(meq(PhoneNumber(phoneNumber)))(any[HeaderCarrier])
      verify(mockVerifyPage, atLeastOnce())
        .apply(
          meq(PhoneNumber.form.withError("phoneNumber", "verifyPage.error"))
        )(any(), any())
    }

    "return bad request when request is for a non-mobile number with appropriate message" in new SetUp {
      val phoneNumber = "test"
      val indeterminateStatus =
        """
          |{"status":"Indeterminate", "message":"Only mobile numbers can be verified"}
          |""".stripMargin
      val indeterminateStatus1 = VerificationResponse(
        status = "Indeterminate",
        message = "Only mobile numbers can be verified"
      )
      val request =
        fakeRequest.withFormUrlEncodedBody("phoneNumber" -> phoneNumber)
      when(
        mockVerifyConnector
          .verify(meq(PhoneNumber(phoneNumber)))(any[HeaderCarrier])
      )
        .thenReturn(
          Future.successful(Right(HttpResponse(Status.OK, indeterminateStatus)))
        )
      val result = controller.verify(request)
      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) shouldBe "some html content"

      verify(mockVerifyConnector, atLeastOnce())
        .verify(meq(PhoneNumber(phoneNumber)))(any[HeaderCarrier])
      verify(mockVerifyPage, atLeastOnce())
        .apply(
          meq(
            PhoneNumber.form.withError("phoneNumber", "verifyPage.mobileonly")
          )
        )(any(), any())
    }
  }

  trait SetUp {
    protected val fakeRequest = FakeRequest(POST, "")
    protected val mockVerifyPage = mock[VerifyPage]
    protected val mockVerifyConnector = mock[VerifyConnector]

    protected val controller = new VerifyController(
      Helpers.stubMessagesControllerComponents(),
      mockVerifyPage,
      mockVerifyConnector
    )

    when(
      mockVerifyPage
        .apply(any())(any(), any())
    )
      .thenReturn(Html("some html content"))
  }
}
