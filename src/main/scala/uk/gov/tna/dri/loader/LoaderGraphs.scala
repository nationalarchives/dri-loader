package uk.gov.tna.dri.loader

import akka.stream.alpakka.jms.JmsConsumerSettings
import akka.stream.alpakka.jms.scaladsl.{JmsConsumer, JmsConsumerControl}
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import org.apache.activemq.ActiveMQConnectionFactory

import java.io.File

object LoaderGraphs {

  def jmsSource: Source[jms.Message, JmsConsumerControl] = {
    val config = ConfigFactory.parseFile(new File("./conf/application.conf")).withFallback(ConfigFactory.load())
    val connectionFactory = new ActiveMQConnectionFactory(config.getString("activeMQURL"))
    JmsConsumer(JmsConsumerSettings(config, connectionFactory).withQueue(config.getString("jmsQueue")))
  }

}
