/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.pagespecs

import com.softwaremill.macwire.wire
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.TimeoutDialogBoxPage._
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.TimeoutDialogBoxPage

class TimeoutDialogBoxPageSpec extends BasePageSpec[TimeoutDialogBoxPage] {
  override def page: TimeoutDialogBoxPage = wire[TimeoutDialogBoxPage]

  "the session expired page render" in {
    open(startImportPath)

    page.mustRenderBasicContentWithoutHeader(goodsDestinationPath, s"${messageApi("goodsDestination.Import.title")} - Declare commercial goods carried in accompanied baggage or small vehicles - GOV.UK")

//    page.headerText mustBe messages("timeOut.heading")
//    page.textOfElementWithId("expiredGuidanceId") mustBe messages("timeOut.guidance")
//    page.textOfElementWithId("expiredRestartId") mustBe messages("timeOut.restart.p")
//    page.textOfElementWithId("expiredUlIdOne") mustBe messages("timeOut.Import.restart")
//    page.textOfElementWithId("expiredUlIdTwo") mustBe messages("timeOut.Export.restart")
  }

}
