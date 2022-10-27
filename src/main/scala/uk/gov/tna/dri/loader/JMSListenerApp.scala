package uk.gov.tna.dri.loader

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import io.circe.generic.auto._
import io.circe.parser
import uk.gov.tna.dri.loader.LoaderGraphs.{batchFlow, jmsSource, tarFlow}
import uk.gov.tna.dri.loader.model.ModelCaseClasses.DRISIPDownloadedMessage

import scala.io.StdIn

object JMSListenerApp extends App {

  implicit val system: ActorSystem = ActorSystem("SqsApp")

  jmsSource.map {
    case t: javax.jms.TextMessage => {
      println(t.getText)
      t.getText
    }
    case other => sys.error(s"unexpected message type ${other.getClass}")
  }.map(message => parser.decode[DRISIPDownloadedMessage](message))
    .filter(decodeEither => decodeEither.isRight)
    .map(x => x match {
      case Right(value) => value
    })
    .via(tarFlow)
    .via(batchFlow)
    .map(x => {
      import system.dispatcher
      x.onComplete(x => println(x))
      x
    })
    .runWith(Sink.foreach(x => {
      import system.dispatcher
      println(x)
    }))

  StdIn.readLine()

}
