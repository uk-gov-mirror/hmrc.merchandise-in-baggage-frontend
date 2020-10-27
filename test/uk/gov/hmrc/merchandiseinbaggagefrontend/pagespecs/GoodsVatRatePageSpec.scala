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

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, GoodsVatRate, GoodsVatRates}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.GoodsVatRatePage._
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{GoodsVatRatePage, RadioButtonPage, SearchGoodsCountryPage}

class GoodsVatRatePageSpec extends DeclarationDataCapturePageSpec[GoodsVatRate, RadioButtonPage[GoodsVatRate]] {
  override lazy val page: RadioButtonPage[GoodsVatRate] = goodsVatRatePage

  "the goods vat rate page" should {
    val path = GoodsVatRatePage.path()

    behave like aPageWhichRenders(path, givenADeclarationJourney(declarationJourneyWithStartedGoodsEntry), title)
    behave like aPageWhichDisplaysPreviouslyEnteredAnswers(path)
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like
      aDataCapturePageWithSimpleRouting(
        path, givenADeclarationJourney(declarationJourneyWithStartedGoodsEntry), GoodsVatRates.values, SearchGoodsCountryPage.path())
  }

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[GoodsVatRate] =
    declarationJourney.goodsEntries.entries.head.maybeGoodsVatRate
}
