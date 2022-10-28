package uk.gov.tna.dri.loader

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import uk.gov.tna.dri.loader.LoaderGraphs.{batchFlow, jmsSource, messageDecodeFlow, tarFlow}

import scala.io.StdIn

object JMSListenerApp extends App {

  implicit val system: ActorSystem = ActorSystem("SqsApp")

  jmsSource.via(messageDecodeFlow)
    .via(tarFlow)
    .via(batchFlow)
    .runWith(Sink.foreach(x => {
       println(x)
    }))

  StdIn.readLine()

}
