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

package uk.gov.hmrc.merchandiseinbaggage.pact

import java.io.File
import java.time.LocalDate

import com.itv.scalapact.ScalaPactForger._
import com.itv.scalapact.circe13._
import com.itv.scalapact.model.{ScalaPactDescription, ScalaPactOptions}
import org.json4s.DefaultFormats
import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResult, CalculationResults, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.CheckResponse
import uk.gov.hmrc.merchandiseinbaggage.model.api.{AmountInPence, ConversionRatePeriod, Declaration, DeclarationId}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

class MibConnectorContractSpec extends BaseSpecWithApplication with CoreTestData with MibConfiguration with WireMockSupport {

  implicit val formats: DefaultFormats.type = DefaultFormats

  val CONSUMER = "merchandise-in-baggage-frontend"
  val PROVIDER = "merchandise-in-baggage"
  val mibConnector = injector.instanceOf[MibConnector]

  val findByDeclaration: Declaration = declaration.copy(mibReference = mibReference, eori = eori)
  val today = LocalDate.now
  val period = ConversionRatePeriod(today, today, "EUR", BigDecimal(1.1))

  val pact: ScalaPactDescription = forgePact
    .between(CONSUMER)
    .and(PROVIDER)
    .addInteraction(
      interaction
        .description("Persisting a declaration")
        .given("persistDeclarationTest")
        .uponReceiving(POST, s"$declarationsUrl", None, Map("Content-Type" -> "application/json"), Json.toJson(declaration).toString)
        .willRespondWith(201, s""""\\"${declaration.declarationId.value}\\""""")
    )
    .addInteraction(
      interaction
        .description("Amending a declaration")
        .given("amendDeclarationTest")
        .uponReceiving(
          PUT,
          s"$declarationsUrl",
          None,
          Map("Content-Type" -> "application/json"),
          Json.toJson(declarationWithAmendment).toString)
        .willRespondWith(200, s""""\\"${declarationWithAmendment.declarationId.value}\\""""")
    )
    .addInteraction(
      interaction
        .description("find a declaration")
        .given(s"id1234XXX${Json.toJson(declaration.copy(declarationId = DeclarationId("56789"))).toString}")
        .uponReceiving(GET, s"$declarationsUrl/56789")
        .willRespondWith(200, Json.toJson(declaration.copy(declarationId = DeclarationId("56789"))).toString)
    )
    .addInteraction(
      interaction
        .description("calculate payments")
        .given(s"calculatePaymentsTest")
        .uponReceiving(
          POST,
          s"$calculationsUrl",
          None,
          Map("Content-Type" -> "application/json"),
          Json.toJson(List(aGoods).map(_.calculationRequest(GreatBritain))).toString)
        .willRespondWith(
          200,
          Json
            .toJson(
              CalculationResults(
                Seq(CalculationResult(aGoods, AmountInPence(18181), AmountInPence(0), AmountInPence(3636), Some(period))),
                WithinThreshold
              ))
            .toString
        )
    )
    .addInteraction(
      interaction
        .description("check EoriNumber")
        .given(s"checkEoriNumberTest")
        .uponReceiving(GET, s"${checkEoriUrl}GB123")
        .willRespondWith(200, Json.toJson(CheckResponse("GB123", true, None)).toString)
    )
    .addInteraction(
      interaction
        .description("findBy mib ref and eori")
        .given(s"findByTestXXX${Json.toJson(findByDeclaration).toString}")
        .uponReceiving(GET, s"$declarationsUrl?mibReference=${mibReference.value}&eori=${eori.value}")
        .willRespondWith(200, Json.toJson(findByDeclaration).toString)
    )

  implicit val options: ScalaPactOptions = ScalaPactOptions(true, "./pact")

  private val pactDir = new File("./pact")
  override def beforeAll(): Unit = {
    super.beforeAll()
    if (pactDir.exists()) pactDir.listFiles().map(_.delete())
  }

  "generate contract files in ./pact" in {
    if (pactDir.exists()) pactDir.listFiles().map(_.delete())
    pact.writePactsToFile

    pactDir.exists() mustBe true
  }
}
