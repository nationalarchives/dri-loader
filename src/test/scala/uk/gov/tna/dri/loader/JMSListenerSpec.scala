package uk.gov.tna.dri.loader

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink}
import akka.testkit.TestKit
import io.circe.generic.auto._
import io.circe.parser
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.tna.dri.loader.LoaderGraphs.{jmsSource, tarFlow}
import uk.gov.tna.dri.loader.model.ModelCaseClasses.DRISIPDownloadedMessage

import scala.language.postfixOps

class JMSListenerSpec extends TestKit(ActorSystem("TestSystem"))
  with AnyWordSpecLike
  with MockitoSugar
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "Loader App " must {
    "retrieve a message" in {
      val matValue = jmsSource.map {
        case t: javax.jms.TextMessage => {
        t.getText
        }
        case other => sys.error(s"unexpected message type ${other.getClass}")
      }
        .map(message =>  parser.decode[DRISIPDownloadedMessage](message))
        .filter(decodeEither => decodeEither.isRight)
        .map(x => x match {
          case Right(value) => value
        })
        .via(tarFlow)
        .toMat(Sink.head)(Keep.right).run()

      import system.dispatcher

     val d = matValue.flatten
      d map {x => {
        println(x)
        assert(true)
      }}
    }
  }
   Thread.sleep(1000)

  //  control.shutdown()

}
