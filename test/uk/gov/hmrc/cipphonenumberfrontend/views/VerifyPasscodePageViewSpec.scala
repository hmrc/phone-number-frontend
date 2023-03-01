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

package uk.gov.hmrc.cipphonenumberfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Injecting}
import play.twirl.api.Html
import uk.gov.hmrc.cipphonenumberfrontend.models.PhoneNumberAndPasscode
import uk.gov.hmrc.cipphonenumberfrontend.views.html.VerifyPasscodePage

class VerifyPasscodePageViewSpec
    extends AnyWordSpec
    with Matchers
    with GuiceOneAppPerSuite
    with Injecting {

  private implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest()
  private implicit val messages: Messages =
    MessagesImpl(Lang("en"), inject[MessagesApi])

  private val verifyPasscodePageView: VerifyPasscodePage =
    inject[VerifyPasscodePage]

  private def view: Html = verifyPasscodePageView(
    PhoneNumberAndPasscode.form.fill(PhoneNumberAndPasscode("abcdefghijk", ""))
  )

  private val doc: Document = Jsoup.parse(view.body)

  override def fakeApplication(): Application =
    GuiceApplicationBuilder().build()

  "Verify passcode page" should {
    "display page title" in {
      doc.title() shouldBe "Enter passcode"
    }

    "display masked telephone number" in {
      doc
        .getElementsByClass("govuk-body")
        .get(0)
        .text()
        .contains("abcxxxxxijk") shouldBe true
    }
  }
}
