/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.content

import org.openqa.selenium.{By, WebElement}
import org.scalatest.Assertion
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.ReviewGoodsPage

class ReviewGoodsContentSpec extends ReviewGoodsPage with CoreTestData with PropertyBaseTables {

  s"render contents" in {
    givenAJourneyWithSession()
    goToReviewGoodsPagePage(New)

    elementText(findByTagName("h2")) must include(messages("reviewGoods.h2"))

    rowTest(0, "reviewGoods.list.item", "wine", "/goods-type-quantity/1")
    rowTest(1, "reviewGoods.list.quantity", "1", "/goods-type-quantity/1")
    rowTest(2, "reviewGoods.list.vatRate", "20%", "/goods-vat-rate/1")
    rowTest(3, "reviewGoods.list.producedInEu", "Yes", "/goods-origin/1")
    rowTest(4, "reviewGoods.list.price", "99.99, Euro (EUR)", "/purchase-details/1")

    findByTagName("a").getAttribute("href") must include("review-goods#main-content")
    radioButtonTest
    elementText(findByTagName("button")) mustBe "Continue"
  }

  s"render different title&header for amending an existing declaration" in {
    givenAJourneyWithSession(Amend)
    goToReviewGoodsPagePage(Amend)
  }

  private def rowTest(rowNumber: Int, key: String, value: String, changeLink: String): Assertion = {
    val row = findById("summaryListId").findElements(By.className("govuk-summary-list__row")).get(rowNumber)
    val rowElement: String => WebElement = className => row.findElement(By.className(className))

    rowElement("govuk-summary-list__key").getText mustBe messages(key)
    rowElement("govuk-summary-list__value").getText mustBe value
    rowElement("govuk-summary-list__actions")
      .findElement(By.className("govuk-link"))
      .getAttribute("href") must include(changeLink)
  }

  private def radioButtonTest = {
    findByXPath("//div[@class='govuk-radios__item'][1]/input").getAttribute("type") mustBe "radio"
    findByXPath("//div[@class='govuk-radios__item'][2]/input").getAttribute("type") mustBe "radio"
    findByXPath("//div[@class='govuk-radios__item'][1]/label").getText mustBe "Yes"
    findByXPath("//div[@class='govuk-radios__item'][2]/label").getText mustBe "No"
  }
}
