package uk.gov.tna.dri.loader

import io.circe.parser
import io.circe.syntax.EncoderOps
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.tna.dri.loader.model.ModelCaseClasses.{DRISIPDownloadedMessage, TransferMetadata}


class ModelCaseClassesSpec extends AnyWordSpecLike with MockitoSugar with Matchers{

  val messageBody: String = {
    """{
      |  "parameters" : {
      |    "sip-download-available" : {
      |      "xip-zip-file-location" : "/home/ihoyle/Downloads/20221026093744.zip"
      |    }
      |  }
      |}
      | """.stripMargin
  }

  "json parser" must {
        import io.circe.generic.auto._
        import play.api.libs.json._


       "parse DRISipDownloadMessage" in {
         val dRISIPDownloadedMessage = parser.decode[DRISIPDownloadedMessage](messageBody)
         dRISIPDownloadedMessage.toOption.get.parameters.`sip-download-available`.`xip-zip-file-location` shouldBe "/home/ihoyle/Downloads/20221026093744.zip"
       }

    println(TransferMetadata("seriesTRE_2","batchMOCKY22002","transferringBodyTNA","closure.csvs","metadata.csvs").asJson.toString())

  }


}
