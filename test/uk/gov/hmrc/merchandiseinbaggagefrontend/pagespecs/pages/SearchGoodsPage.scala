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

package uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages

import org.openqa.selenium.WebDriver
import org.scalatest.Assertion
import org.scalatestplus.selenium.WebBrowser
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.CategoryQuantityOfGoods

class SearchGoodsPage(implicit webDriver: WebDriver) extends DeclarationDataCapturePage[CategoryQuantityOfGoods] {

  import WebBrowser._

  def categoryInput: Element = find(NameQuery("category")).get

  def quantityInput: Element = find(NameQuery("quantity")).get

  override def fillOutForm(formData: CategoryQuantityOfGoods): Unit = {
    categoryInput.underlying.clear()
    categoryInput.underlying.sendKeys(formData.category)

    quantityInput.underlying.clear()
    quantityInput.underlying.sendKeys(formData.quantity)
  }

  override def previouslyEnteredValuesAreDisplayed(formData: CategoryQuantityOfGoods): Assertion = {
    categoryInput.underlying.getAttribute("value") mustEqual formData.category
    quantityInput.underlying.getAttribute("value") mustEqual formData.quantity
  }
}

object SearchGoodsPage {
  def path(idx: Int): String = s"/merchandise-in-baggage/search-goods/$idx"

  val title = "What type of goods are you bringing into the UK?"
}