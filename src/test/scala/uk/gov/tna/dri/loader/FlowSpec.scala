package uk.gov.tna.dri.loader

import akka.actor.ActorSystem
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.TestKit
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.apache.activemq.command.ActiveMQTextMessage
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.tna.dri.loader.LoaderGraphs.messageDecodeFlow
import uk.gov.tna.dri.loader.model.ModelCaseClasses.{DRISIPDownloadedMessage, SIPDowloadAvailableParams, SIPDownloadAvailable}

import javax.jms.TextMessage

class FlowSpec extends TestKit(ActorSystem("TestSystem")) with AnyWordSpecLike with MockitoSugar with Matchers{

  "The Loader streams" must {

    "convert a JMS text message to a DRISIPDownloadedMessage" in {
     val sipParams =  SIPDowloadAvailableParams("file-location")
     val downloadAvailable =  SIPDownloadAvailable(sipParams)
     val driMessageContent = DRISIPDownloadedMessage(downloadAvailable)

     val messageText = driMessageContent.asJson

     val jmsTextMessage = new ActiveMQTextMessage()
     jmsTextMessage.setText(messageText.toString())

    val testSource = TestSource.probe[TextMessage]
    val testSink = TestSink.probe[DRISIPDownloadedMessage]

     val (publisher, subscriber) = testSource.via(messageDecodeFlow).toMat(testSink)(Keep.both).run()

    publisher.sendNext(jmsTextMessage).sendComplete()

    subscriber.request(2)
    subscriber.expectNext(driMessageContent)
    subscriber.expectComplete()

    }
  }
}
