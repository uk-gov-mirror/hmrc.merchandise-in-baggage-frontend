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
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.merchandiseinbaggage.config.AmendFlagConf
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.givenFindByDeclarationReturnStatus
import uk.gov.hmrc.merchandiseinbaggage.views.html.RetrieveDeclarationView
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport
import uk.gov.hmrc.merchandiseinbaggage.navigation._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class RetrieveDeclarationControllerSpec
    extends DeclarationJourneyControllerSpec with WireMockSupport with MockFactory with PropertyBaseTables {

  val view = injector.instanceOf[RetrieveDeclarationView]
  val connector = injector.instanceOf[MibConnector]
  val mockNavigator = mock[Navigator]

  def controller(declarationJourney: DeclarationJourney, amendFlag: Boolean = true) =
    new RetrieveDeclarationController(
      controllerComponents,
      stubProvider(declarationJourney),
      stubRepo(declarationJourney),
      connector,
      mockNavigator,
      view) {
      override lazy val amendFlagConf: AmendFlagConf = AmendFlagConf(amendFlag)
    }

  val journey: DeclarationJourney = DeclarationJourney(aSessionId, Import)

  //TODO create UI test for content
  forAll(declarationTypesTable) { importOrExport: DeclarationType =>
    val journey: DeclarationJourney = DeclarationJourney(aSessionId, importOrExport)
    "onPageLoad" should {
      s"return 200 with expected content for $importOrExport" in {

        val request = buildGet(RetrieveDeclarationController.onPageLoad.url, aSessionId)
        val eventualResult = controller(journey).onPageLoad(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messageApi(s"retrieveDeclaration.title"))
        result must include(messageApi(s"retrieveDeclaration.heading"))
        result must include(messageApi(s"retrieveDeclaration.p"))

        result must include(messageApi(s"retrieveDeclaration.mibReference.label"))
        result must include(messageApi(s"retrieveDeclaration.mibReference.hint"))

        result must include(messageApi(s"retrieveDeclaration.eori.label"))
        result must include(messageApi(s"retrieveDeclaration.eori.hint"))
      }

      s"redirect to ${CannotAccessPageController.onPageLoad().url} if flag is false for $importOrExport" in {
        val request = buildGet(NewOrExistingController.onPageLoad.url, aSessionId)
        val eventualResult = controller(journey, false).onPageLoad(request)

        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(CannotAccessPageController.onPageLoad().url)
      }
    }
  }

  "onSubmit" should {
    s"redirect by delegating to Navigator" in {
      givenFindByDeclarationReturnStatus(mibReference, eori, 404)
      val request = buildPost(RetrieveDeclarationController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("mibReference" -> mibReference.value, "eori" -> eori.value)

      (mockNavigator
        .nextPage(_: RetrieveDeclarationRequest)(_: ExecutionContext))
        .expects(*, *)
        .returning(Future.successful(DeclarationNotFoundController.onPageLoad()))

      controller(journey).onSubmit(request).futureValue
    }

    s"redirect to /internal-server-error after successful form submit but some unexpected error is thrown from the BE" in {
      givenFindByDeclarationReturnStatus(mibReference, eori, 500)
      val request = buildPost(RetrieveDeclarationController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("mibReference" -> mibReference.value, "eori" -> eori.value)

      val eventualResult = controller(journey).onSubmit(request)
      status(eventualResult) mustBe 500
    }

    "return 400 for invalid form data" in {
      val request = buildPost(RetrieveDeclarationController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("mibReference" -> "XAMB0000010", "eori" -> "GB12345")
      val eventualResult = controller(journey).onSubmit(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi(s"retrieveDeclaration.mibReference.error.invalid"))
      result must include(messageApi(s"retrieveDeclaration.eori.error.invalid"))
    }
  }
}
