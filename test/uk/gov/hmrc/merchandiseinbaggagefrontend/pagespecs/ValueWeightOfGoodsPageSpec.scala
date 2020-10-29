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

package uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsDestinations.{GreatBritain, NorthernIreland}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, YesNo}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.ValueWeightOfGoodsPage.{greatBritainTitle, northernIrelandTitle, path}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{CannotUseServicePage, RadioButtonPage, SearchGoodsPage}

class ValueWeightOfGoodsPageSpec extends DeclarationDataCapturePageSpec[YesNo, RadioButtonPage[YesNo]] {
  override lazy val page: RadioButtonPage[YesNo] = valueWeightOfGoodsPage

  private def givenANorthernIrelandJourney(): Unit =
    givenADeclarationJourney(startedImportJourney.copy(maybeGoodsDestination = Some(NorthernIreland)))

  private def givenAGreatBritainJourney(): Unit =
    givenADeclarationJourney(startedImportJourney.copy(maybeGoodsDestination = Some(GreatBritain)))

  "the value and weight of goods page" should {
    behave like aPageWhichRenders(path, givenANorthernIrelandJourney(), northernIrelandTitle)
    behave like aPageWhichRenders(path, givenAGreatBritainJourney(), greatBritainTitle)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path)
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aDataCapturePageWithConditionalRouting(path, givenACompleteDeclarationJourney(), No, SearchGoodsPage.path(1))
    behave like aDataCapturePageWithConditionalRouting(path, givenACompleteDeclarationJourney(), Yes, CannotUseServicePage.path)
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[YesNo] =
    declarationJourney.maybeValueWeightOfGoodsExceedsThreshold
}