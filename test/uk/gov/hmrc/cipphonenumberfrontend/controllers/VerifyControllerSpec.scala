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

import org.mockito.ArgumentMatchersSugar.{*, any}
import org.mockito.IdiomaticMockito
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import play.twirl.api.Html
import uk.gov.hmrc.cipphonenumberfrontend.connectors.VerifyConnector
import uk.gov.hmrc.cipphonenumberfrontend.models.PhoneNumber
import uk.gov.hmrc.cipphonenumberfrontend.views.html.VerifyPage
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VerifyControllerSpec
    extends AnyWordSpec
    with Matchers
    with IdiomaticMockito {

  "verifyForm" should {
    "return 200" in new SetUp {
      val result = controller.verifyForm()(fakeRequest)
      status(result) shouldBe Status.OK

      mockVerifyPage.apply(PhoneNumber.form)(*, *) was called
    }

    "return HTML" in new SetUp {
      val result = controller.verifyForm()(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")

      mockVerifyPage.apply(PhoneNumber.form)(*, *) was called
    }

    "load empty form by default" in new SetUp {
      controller.verifyForm()(fakeRequest)
      mockVerifyPage.apply(PhoneNumber.form)(*, *) was called
    }

    "load form with phone number when supplied" in new SetUp {
      val phoneNumber = "test"
      controller.verifyForm(Some(phoneNumber))(fakeRequest)
      mockVerifyPage
        .apply(PhoneNumber.form.fill(PhoneNumber(phoneNumber)))(*, *) was called
    }
  }

  "verify" should {
    "redirect to verify passcode when request is valid" in new SetUp {
      val phoneNumber = "test"
      val request =
        fakeRequest.withFormUrlEncodedBody("phoneNumber" -> phoneNumber)
      mockVerifyConnector
        .verify(PhoneNumber(phoneNumber))(any[HeaderCarrier])
        .returns(
          Future.successful(
            Right(
              HttpResponse(
                Status.OK,
                "",
                Map("Location" -> Seq("notificationId"))
              )
            )
          )
        )
      val result = controller.verify(request)
      status(result) shouldBe Status.SEE_OTHER
      header("Location", result) shouldBe Some(
        s"/phone-number-example-frontend/verify/passcode?phoneNumber=$phoneNumber"
      )

      mockVerifyConnector.verify(PhoneNumber(phoneNumber))(
        any[HeaderCarrier]
      ) was called
    }

    "return bad request when form is invalid" in new SetUp {
      val result = controller.verify(fakeRequest)
      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) shouldBe "some html content"

      mockVerifyPage.apply(
        PhoneNumber.form.withError("phoneNumber", "error.required")
      )(*, *) was called
    }

    "return bad request when request is invalid" in new SetUp {
      val phoneNumber = "test"
      val request =
        fakeRequest.withFormUrlEncodedBody("phoneNumber" -> phoneNumber)
      mockVerifyConnector
        .verify(PhoneNumber(phoneNumber))(any[HeaderCarrier])
        .returns(
          Future.successful(Left(UpstreamErrorResponse("", Status.BAD_REQUEST)))
        )
      val result = controller.verify(request)
      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) shouldBe "some html content"

      mockVerifyConnector.verify(PhoneNumber(phoneNumber))(
        any[HeaderCarrier]
      ) was called
      mockVerifyPage.apply(
        PhoneNumber.form.withError("phoneNumber", "verifyPage.error")
      )(*, *) was called
    }

    "return bad request when request is for a non-mobile number with appropriate message" in new SetUp {
      val phoneNumber = "test"
      val indeterminateStatus =
        """
          |{"status":"Indeterminate", "message":"Only mobile numbers can be verified"}
          |""".stripMargin
      val request =
        fakeRequest.withFormUrlEncodedBody("phoneNumber" -> phoneNumber)
      mockVerifyConnector
        .verify(PhoneNumber(phoneNumber))(any[HeaderCarrier])
        .returns(
          Future.successful(Right(HttpResponse(Status.OK, indeterminateStatus)))
        )
      val result = controller.verify(request)
      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) shouldBe "some html content"

      mockVerifyConnector.verify(PhoneNumber(phoneNumber))(
        any[HeaderCarrier]
      ) was called
      mockVerifyPage.apply(
        PhoneNumber.form.withError("phoneNumber", "verifyPage.mobileonly")
      )(*, *) was called
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

    mockVerifyPage
      .apply(*)(*, *)
      .returns(Html("some html content"))
  }
}
