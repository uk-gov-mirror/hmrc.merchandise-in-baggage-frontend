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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import org.scalamock.scalatest.MockFactory
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.model.api.{CategoryQuantityOfGoods, DeclarationType}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntries, ImportGoodsEntry}
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsVatRateView
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.merchandiseinbaggage.navigation._

import scala.concurrent.{ExecutionContext, Future}

class GoodsVatRateControllerSpec extends DeclarationJourneyControllerSpec with MockFactory {

  private val view = app.injector.instanceOf[GoodsVatRateView]
  private val mockNavigator = mock[Navigator]
  def controller(declarationJourney: DeclarationJourney) =
    new GoodsVatRateController(controllerComponents, stubProvider(declarationJourney), stubRepo(declarationJourney), view, mockNavigator)

  private val journey: DeclarationJourney = DeclarationJourney(
    aSessionId,
    DeclarationType.Import,
    goodsEntries = GoodsEntries(Seq(ImportGoodsEntry(maybeCategoryQuantityOfGoods = Some(CategoryQuantityOfGoods("clothes", "1")))))
  )

  "onPageLoad" should {
    "return 200 with radio buttons" in {
      val request = buildPost(GoodsVatRateController.onPageLoad(1).url, aSessionId)
      val eventualResult = controller(journey).onPageLoad(1)(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 200
      result must include(messages("goodsVatRate.title", "clothes"))
      result must include(messages("goodsVatRate.heading", "clothes"))
      result must include(messages("goodsVatRate.p"))
      result must include(messages("goodsVatRate.Zero"))
      result must include(messages("goodsVatRate.Five"))
      result must include(messages("goodsVatRate.Twenty"))
    }
  }

  "onSubmit" should {
    "redirect to next page after successful form submit" in {
      val request = buildPost(GoodsVatRateController.onSubmit(1).url, aSessionId)
        .withFormUrlEncodedBody("value" -> "Zero")

      (mockNavigator
        .nextPage(_: GoodsVatRateRequest)(_: ExecutionContext))
        .expects(*, *)
        .returning(Future successful SearchGoodsCountryController.onPageLoad(1))
        .once()

      controller(journey).onSubmit(1)(request).futureValue
    }

    "return 400 with any form errors" in {
      val request = buildGet(GoodsVatRateController.onSubmit(1).url, aSessionId)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(journey).onSubmit(1)(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messages("goodsVatRate.title", "clothes"))
      result must include(messages("goodsVatRate.heading", "clothes"))
    }
  }
}
