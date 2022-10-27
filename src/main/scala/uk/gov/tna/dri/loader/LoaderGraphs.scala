package uk.gov.tna.dri.loader

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.alpakka.file.scaladsl.{Archive, Directory}
import akka.stream.alpakka.jms.JmsConsumerSettings
import akka.stream.alpakka.jms.scaladsl.{JmsConsumer, JmsConsumerControl}
import akka.stream.scaladsl.{Compression, FileIO, Flow, Sink, Source}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import org.apache.activemq.ActiveMQConnectionFactory
import uk.gov.tna.dri.loader.model.ModelCaseClasses.DRISIPDownloadedMessage

import java.io.File
import java.nio.file.{Path, Paths}
import javax.jms.Message
import scala.collection.immutable
import scala.concurrent.Future

object LoaderGraphs {



  def jmsSource: Source[Message, JmsConsumerControl] = {
    val config = ConfigFactory.parseFile(new File("./conf/application.conf")).withFallback(ConfigFactory.load())
    val connectionFactory = new ActiveMQConnectionFactory(config.getString("activeMQURL"))
    JmsConsumer(JmsConsumerSettings(config, connectionFactory).withQueue(config.getString("jmsQueue")))
  }

  def batchFlow = Flow.fromFunction(evaluateBatchAndSeries)
  def evaluateBatchAndSeries(fileNames :(Future[immutable.Iterable[Path]],DRISIPDownloadedMessage,Path)) = {
    implicit val system: ActorSystem = ActorSystem("SqsApp")
    implicit val dis = system.dispatcher
     val res = fileNames._1.map(list => {
      println(list)
      val filtered: Seq[Path] = list.filter(path => !path.toString.endsWith("Header")).toList
      val small: Path = filtered.fold(filtered.head) {
        (path, smallPath) =>
          if (path.toString.length < smallPath.toString.length) path
          else
            smallPath
      }

      small
    })
    res
  }

    val tarFlow: Flow[DRISIPDownloadedMessage, (Future[immutable.Iterable[Path]], DRISIPDownloadedMessage, Path), NotUsed] = Flow.fromFunction(unTar)

    def unTar(path: DRISIPDownloadedMessage) = {
      implicit val system: ActorSystem = ActorSystem("SqsApp")
      implicit val dis = system.dispatcher
      val bytesSource: Source[ByteString, Future[IOResult]] = FileIO.fromPath(Paths.get("/home/ihoyle/Downloads/testBag"))
      val target: Path = Paths.get("/home/ihoyle/accrual")
      val filePaths: Future[immutable.Iterable[Path]] = {
        bytesSource
          .via(Compression.gunzip(10000))
          .via(Archive.tarReader())
          .mapAsyncUnordered(10) {
            case (metadata, source) =>
              val targetFile = target.resolve(metadata.filePath)
              if (metadata.isDirectory) {
                Source
                  .single(targetFile)
                  .via(Directory.mkdirs())
                  .map(_ => targetFile)
                  .runWith(Sink.head)
              } else {
                Source
                  .single(targetFile.getParent)
                  .via(Directory.mkdirs())
                  .runWith(Sink.ignore)
                  .map { _ =>
                    if (!targetFile.toString.endsWith("@PaxHeader")) {
                      FileIO.toPath(targetFile)
                      source.runWith(FileIO.toPath(targetFile))
                    }
                  }
              }
              Future(targetFile)
          }
          .runWith(Sink.collection)
      }
      (filePaths,path,target)
    }
  }
