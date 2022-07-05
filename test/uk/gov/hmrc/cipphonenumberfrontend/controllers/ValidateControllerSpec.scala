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

import org.mockito.ArgumentMatchers.any
import org.mockito.IdiomaticMockito
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers, Injecting}
import play.twirl.api.Html
import uk.gov.hmrc.cipphonenumberfrontend.models.PhoneNumber
import uk.gov.hmrc.cipphonenumberfrontend.validators.Validator
import uk.gov.hmrc.cipphonenumberfrontend.views.html.ValidatePage
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ValidateControllerSpec extends AnyWordSpec with Matchers with IdiomaticMockito with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach {

  private implicit val messages: Messages = MessagesImpl(Lang("en"), inject[MessagesApi])

  val mockValidator: Validator = mock[Validator]

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(
        //        TODO: find out what is causing the binding error, fix and re-enable tests below
        //        bind[Validator].toInstance(mockValidator)
      )
      .build()

  private val controller = inject[ValidateController]

  override def afterEach(): Unit = {
    reset(mockValidator)
  }

  private val fakeRequest = FakeRequest("GET", "/")

  "validateForm" should {
    "return 200" in {
      val result = controller.validateForm(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML" in {
      val result = controller.validateForm(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  "Validate" should {
    "redirect to landing page when form is valid" in new SetUp {
      val phoneNumber = "08001111"
      val request = FakeRequest("POST", "/phone-number/validate").withFormUrlEncodedBody("phoneNumber" -> phoneNumber)
      mockValidator.validate(PhoneNumber(phoneNumber))(any[HeaderCarrier]) returns Future.successful(true)
      val result = controller.validate(request)
      status(result) shouldBe Status.SEE_OTHER
      header("Location", result) shouldBe Some("/phone-number?validated=true")
    }

    "return bad request when form is invalid" in new SetUp {
      val phoneNumber = "invalid"
      implicit val request = FakeRequest("POST", "/phone-number/validate").withFormUrlEncodedBody("phoneNumber" -> phoneNumber)
      mockValidator.validate(PhoneNumber(phoneNumber))(any[HeaderCarrier]) returns Future.successful(false)
      mockValidatePage(any())(any(), any()) returns Html("Some")
      val result = controller.validate(request)
      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) shouldBe "Some"
    }
  }

  trait SetUp {
    val mockValidatePage = mock[ValidatePage]
    val mockValidator = mock[Validator]
    val controller = new ValidateController(Helpers.stubMessagesControllerComponents(), mockValidatePage, mockValidator)
  }
}
