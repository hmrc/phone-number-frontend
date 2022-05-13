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

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, Ignore}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContent, Request}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.cipphonenumberfrontend.models.PhoneNumber
import uk.gov.hmrc.cipphonenumberfrontend.validators.Validator
import uk.gov.hmrc.cipphonenumberfrontend.views.html.ValidatePage

import scala.concurrent.Future

class ValidateControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with Injecting with BeforeAndAfterEach {

  private implicit val messages: Messages = MessagesImpl(Lang("en"), inject[MessagesApi])

  val mockValidator: Validator = mock[Validator]

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(
//        TODO: find out what is causing the binding error, fix and re-enable tests below
//        bind[Validator].toInstance(mockValidator)
      )
      .configure(
        "metrics.jvm" -> false,
        "metrics.enabled" -> false
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

  "validate" should {
    "redirect to landing page when form is valid" ignore {
      val validForm = PhoneNumber("valid")
      when(mockValidator.validate(ArgumentMatchers.eq(validForm))(any())).thenReturn(Future.successful(true))

      val validFormMap = PhoneNumber.form.mapping.unbind(validForm)
      implicit val validRequest: Request[AnyContent] =
        fakeRequest.withFormUrlEncodedBody(validFormMap.toSeq: _*)

      val response = controller.validate().apply(validRequest)
      status(response) shouldBe 303
      header("Location", response) shouldBe Some("/phone-number?validated=false")
    }

    "return bad request when form is invalid" ignore {
      val invalidForm = PhoneNumber("invalid")
      when(mockValidator.validate(ArgumentMatchers.eq(invalidForm))(any())).thenReturn(Future.successful(false))

      val invalidFormMap = PhoneNumber.form.mapping.unbind(invalidForm)
      implicit val invalidRequest: Request[AnyContent] =
        fakeRequest.withFormUrlEncodedBody(invalidFormMap.toSeq: _*)

      val response = controller.validate().apply(invalidRequest)
      status(response) shouldBe BAD_REQUEST
      contentAsString(response) shouldBe inject[ValidatePage].apply(PhoneNumber.form.withError("phoneNumber", "validatePage.error")).toString()
    }
  }
}
