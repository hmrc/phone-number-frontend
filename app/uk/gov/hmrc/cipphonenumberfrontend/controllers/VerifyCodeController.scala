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

import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cipphonenumberfrontend.connectors.VerificationConnector
import uk.gov.hmrc.cipphonenumberfrontend.models.PhoneNumberAndPasscode
import uk.gov.hmrc.cipphonenumberfrontend.views.html.VerifyCodePage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VerifyCodeController @Inject() (
    mcc: MessagesControllerComponents,
    verifyPasscodePage: VerifyCodePage,
    verifyConnector: VerificationConnector
)(implicit executionContext: ExecutionContext)
    extends FrontendController(mcc)
    with Logging {

  def verifyCodeForm(phoneNumber: Option[String]): Action[AnyContent] =
    Action.async { implicit request =>
      phoneNumber match {
        case Some(value) =>
          Future.successful(
            Ok(
              verifyPasscodePage(
                PhoneNumberAndPasscode.form.fill(
                  PhoneNumberAndPasscode(value, "")
                )
              )
            )
          )
        case None =>
          Future.successful(Redirect(routes.LandingPageController.landing()))
      }
    }

  def verifyCode: Action[AnyContent] = Action.async { implicit request =>
    PhoneNumberAndPasscode.form
      .bindFromRequest()
      .fold(
        invalid => {
          logger.warn(s"Failed to validate request")
          Future.successful(BadRequest(verifyPasscodePage(invalid)))
        },
        phoneNumberAndPasscode => {
          verifyConnector.verifyCode(phoneNumberAndPasscode) map {
            case Left(l) =>
              logger.warn("Passcode verification failed")
              BadRequest(
                verifyPasscodePage(
                  PhoneNumberAndPasscode.form
                    .withError("verificationCode", "verifyCodePage.error")
                    .fill(
                      PhoneNumberAndPasscode(
                        phoneNumberAndPasscode.phoneNumber,
                        ""
                      )
                    )
                )
              )
            case Right(r) =>
              val optStatus = r.json \ "status"
              if (optStatus.isDefined) {
                optStatus.get.as[String] match {
                  case "CODE_VERIFIED" =>
                    Redirect(routes.LandingPageController.landing(Some(true)))
                  case _ =>
                    BadRequest(
                      verifyPasscodePage(
                        PhoneNumberAndPasscode.form
                          .withError(
                            "verificationCode",
                            "verifyCodePage.incorrectPasscode"
                          )
                          .fill(
                            PhoneNumberAndPasscode(
                              phoneNumberAndPasscode.phoneNumber,
                              ""
                            )
                          )
                      )
                    )
                }
              } else {
                BadRequest(
                  verifyPasscodePage(
                    PhoneNumberAndPasscode.form
                      .withError(
                        "verificationCode",
                        "verifyCodePage.passcodeExpired"
                      )
                      .fill(
                        PhoneNumberAndPasscode(
                          phoneNumberAndPasscode.phoneNumber,
                          ""
                        )
                      )
                  )
                )
              }
          }
        }
      )
  }
}
