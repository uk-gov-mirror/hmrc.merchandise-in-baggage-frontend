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

package uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages

import org.openqa.selenium.WebDriver
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, WireMockSupport}

class TimeoutDialogBoxPage(implicit webDriver: WebDriver) extends BasePage

object TimeoutDialogBoxPage extends BaseSpecWithApplication {
  val startImportPath = "/declare-commercial-goods/start-import"
  val goodsDestinationPath = "/declare-commercial-goods/goods-destination"

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder().configure(Map(
      "play.http.router" -> "testOnlyDoNotUseInAppConf.Routes",
      "microservice.services.address-lookup-frontend.port" -> WireMockSupport.port,
      "microservice.services.currency-conversion.port" -> WireMockSupport.port,
      "microservice.services.payment.port" -> WireMockSupport.port,
      "microservice.services.merchandise-in-baggage.port" -> WireMockSupport.port,
      "timeout.timeout" -> 1
    )).build()
}
