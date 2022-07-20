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
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import play.twirl.api.Html
import uk.gov.hmrc.cipphonenumberfrontend.views.html.LandingPage

class LandingPageControllerSpec extends AnyWordSpec with Matchers {

  "GET /" should {
    "return 200" in new SetUp {
      val result = controller.landing()(fakeRequest)
      status(result) shouldBe Status.OK
    }

    "return HTML" in new SetUp {
      val result = controller.landing()(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }

  trait SetUp {
    protected val fakeRequest = FakeRequest()
    protected val mockLandingPage = mock[LandingPage]
    protected val controller = new LandingPageController(Helpers.stubMessagesControllerComponents(), mockLandingPage)

    mockLandingPage(any())(any(), any()) returns Html("Some")
  }
}
