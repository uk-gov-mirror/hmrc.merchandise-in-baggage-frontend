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
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntries, ImportGoodsEntry}
import uk.gov.hmrc.merchandiseinbaggage.views.html.{PurchaseDetailsExportView, PurchaseDetailsImportView}
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class PurchaseDetailsControllerSpec extends DeclarationJourneyControllerSpec with MockFactory {

  private val importView = app.injector.instanceOf[PurchaseDetailsImportView]
  private val exportView = app.injector.instanceOf[PurchaseDetailsExportView]
  private val mockNavigator = mock[Navigator]

  def controller(declarationJourney: DeclarationJourney) =
    new PurchaseDetailsController(
      controllerComponents,
      stubProvider(declarationJourney),
      stubRepo(declarationJourney),
      mockNavigator,
      importView,
      exportView)

  declarationTypes.foreach { importOrExport =>
    val journey: DeclarationJourney = DeclarationJourney(
      aSessionId,
      importOrExport,
      goodsEntries = GoodsEntries(Seq(ImportGoodsEntry(maybeCategoryQuantityOfGoods = Some(CategoryQuantityOfGoods("clothes", "1")))))
    )

    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {

        val request = buildGet(PurchaseDetailsController.onPageLoad(1).url, aSessionId)
        val eventualResult = controller(journey).onPageLoad(1)(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messages("purchaseDetails.title", "clothes"))
        result must include(messages("purchaseDetails.heading", "clothes"))

        if (importOrExport == Import) {
          result must include(messages("purchaseDetails.price.hint"))
          result must include(messages("purchaseDetails.p.1"))
          result must include(messages("purchaseDetails.p.2"))
          result must include(messages("purchaseDetails.p.a.text"))
          result must include(messages("purchaseDetails.p.a.href"))
        }
      }
    }

    "onSubmit" should {
      s"redirect to next page after successful form submit for $importOrExport" in {
        val request = buildPost(SearchGoodsCountryController.onSubmit(1).url, aSessionId)
          .withFormUrlEncodedBody("price" -> "20", "currency" -> "EUR")

        (mockNavigator
          .nextPage(_: PurchaseDetailsRequest)(_: ExecutionContext))
          .expects(*, *)
          .returning(Future.successful(ReviewGoodsController.onPageLoad()))

        val eventualResult = controller(journey).onSubmit(1)(request)
        status(eventualResult) mustBe 303
      }

      s"return 400 with any form errors for $importOrExport" in {
        val request = buildPost(SearchGoodsCountryController.onSubmit(1).url, aSessionId)
          .withFormUrlEncodedBody("abcd" -> "in valid")

        val eventualResult = controller(journey).onSubmit(1)(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 400
        result must include(messageApi("error.summary.title"))
        result must include(messages("purchaseDetails.title", "clothes"))
        result must include(messages("purchaseDetails.heading", "clothes"))
      }
    }
  }
}
