package uk.gov.tna.dri.loader

import akka.actor.ActorSystem
import akka.stream.alpakka.jms.JmsTextMessage
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.testkit.TestKit
import com.spotify.docker.client.messages.PortBinding
import com.whisk.docker.testkit.scalatest.DockerTestKitForAll
import com.whisk.docker.testkit.{ContainerSpec, DockerReadyChecker, ManagedContainers}
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.tna.dri.loader.LoaderGraphs.{jmsSink, jmsSource, messageDecodeFlow}
import uk.gov.tna.dri.loader.model.ModelCaseClasses.{DRISIPDownloadedMessage, SIPDowloadAvailableParams, SIPDownloadAvailable}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class JMSListenerIntegrationSpec extends TestKit(ActorSystem("TestSystem"))
  with AnyWordSpecLike
  with MockitoSugar
  with Matchers
  with BeforeAndAfterAll
  with DockerTestKitForAll {

  private val activeMqPort = 61616
  private val activeMqConsole = 8161
  private lazy val activemqContainer = ContainerSpec("symptoma/activemq:latest")
    .withPortBindings(activeMqPort -> PortBinding.of("0.0.0.0", activeMqPort), activeMqConsole -> PortBinding.of("0.0.0.0", activeMqConsole))
    .withReadyChecker(DockerReadyChecker.LogLineContains("ActiveMQ WebConsole available at http://0.0.0.0:8161/"))

  override val managedContainers: ManagedContainers = activemqContainer.toManagedContainer


  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }

  val sipLocation = "sip-location"

  "Jms listener"  must {

    "consume and decode message" in {
      sendMessageToQueue

      val messageDecoded: Future[DRISIPDownloadedMessage] = jmsSource.via(messageDecodeFlow).runWith(Sink.head)
      val location : DRISIPDownloadedMessage = Await.result(messageDecoded, 5 seconds)
      location.parameters.`sip-download-available`.`xip-zip-file-location` shouldBe sipLocation

    }
  }

  private def sendMessageToQueue = {
    val sipParams = SIPDowloadAvailableParams(sipLocation)
    val downloadAvailable = SIPDownloadAvailable(sipParams)
    val driMessageContent = DRISIPDownloadedMessage(downloadAvailable)

    val messageText = driMessageContent.asJson
    val jmsTextMessage = JmsTextMessage(messageText.toString())

    val messageSource = Source.single(jmsTextMessage)
    val jmsMessageSent = messageSource.toMat(jmsSink)(Keep.right).run

    Await.result(jmsMessageSent, 5 seconds)
  }


}
