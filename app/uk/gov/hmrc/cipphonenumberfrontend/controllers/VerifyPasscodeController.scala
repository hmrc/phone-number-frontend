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

import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cipphonenumberfrontend.connectors.VerifyConnector
import uk.gov.hmrc.cipphonenumberfrontend.models.PhoneNumberAndPasscode
import uk.gov.hmrc.cipphonenumberfrontend.views.html.VerifyPasscodePage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyPasscodeController @Inject()(
                                          mcc: MessagesControllerComponents,
                                          verifyPasscodePage: VerifyPasscodePage,
                                          verifyConnector: VerifyConnector)
                                        (implicit executionContext: ExecutionContext)
  extends FrontendController(mcc)
    with Logging {

  def verifyForm(phoneNumber: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    phoneNumber match {
      case Some(value) => Future.successful(Ok(verifyPasscodePage(PhoneNumberAndPasscode.form.fill(PhoneNumberAndPasscode(value, "")))))
      case None => Future.successful(SeeOther("/phone-number-example-frontend"))
    }
  }

  def verify: Action[AnyContent] = Action.async { implicit request =>
    PhoneNumberAndPasscode.form.bindFromRequest().fold(
      invalid => {
        logger.warn(s"Failed to validate request")
        Future.successful(BadRequest(verifyPasscodePage(invalid)))
      },
      phoneNumberAndPasscode => {
        verifyConnector.verifyPasscode(phoneNumberAndPasscode) map {
          case Left(l) =>
            logger.warn(l.message)
            BadRequest(verifyPasscodePage(PhoneNumberAndPasscode.form
              .withError("passcode", "verifyPasscodePage.error")
              .fill(PhoneNumberAndPasscode(phoneNumberAndPasscode.phoneNumber, ""))))
          case Right(r) =>
            (r.json \ "status").as[String] match {
              case "Verified" => SeeOther("/phone-number-example-frontend?verified=true")
              case "Not verified" => Ok(verifyPasscodePage(PhoneNumberAndPasscode.form
                .withError("passcode", "verifyPasscodePage.incorrectPasscode")
                .fill(PhoneNumberAndPasscode(phoneNumberAndPasscode.phoneNumber, ""))))
            }
        }
      }
    )
  }
}
