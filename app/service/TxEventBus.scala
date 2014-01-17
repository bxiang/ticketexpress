package service

import akka.event.ActorEventBus
import akka.event.LookupClassification
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import org.bson.types.ObjectId
import models._

class TxEvent(p: String) {
  val name: String = p
}

case class NewUserEvent(
  userId: ObjectId
) extends TxEvent("user/created")

case class NewTicketEvent(
  ticketId: ObjectId
) extends TxEvent("ticket/created")

object TxEventBus extends ActorEventBus with LookupClassification{
  type Event=TxEvent
  type Classifier=String

  protected def mapSize(): Int = 10

  protected def classify(event: Event): Classifier = event.name

  protected def publish(event: Event, subscriber: Subscriber): Unit = subscriber ! event
}
