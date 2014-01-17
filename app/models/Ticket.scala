package models

import play.api.Play.current
import java.util.Date
import java.io.File
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs._
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import pkgContext._

case class Ticket(
  id: ObjectId = new ObjectId,
  number: Long,
  summary: String,
  detail: String,
  status: String,
  ticketType: String,
  priority: String = "Normal",
  severity: String = "Normal",
  progress: Int = 0,
  estimate: Option[String] = None,
  created: Date = new Date(),
  updated: Option[Date] = None,
  reporter: Option[User] = None,
  assignee: Option[User] = None,
  project: Option[Project] = None,
  files: List[Attachment] = Nil,
  histories: List[History] = Nil)

case class History(
  id: ObjectId = new ObjectId,
  log: String,
  user: String,
  created: Date = new Date())

case class Attachment(id: ObjectId, filename: String)

object Ticket extends ModelCompanion[Ticket, ObjectId] {
  def collection = mongoCollection("tickets")
  val fileCollection = gridFS("ticketAttachments")
  val dao = new SalatDAO[Ticket, ObjectId](collection) {}

  // Indexes
  collection.ensureIndex(DBObject("number" -> 1), "ticket_idx", unique = true)

  // Queries
  def findByNumber(number: String): Option[Ticket] = dao.findOne(MongoDBObject("number" -> number))
  def findByStatus(status: String) = dao.find(MongoDBObject("status" -> status))

  def getNextNumber(): Long = {
    Counter.getNextSequence("Ticket")
  }

  def addFile(id: ObjectId, f: File) = {
    val gridFSFile = fileCollection.createFile(f)
    gridFSFile.save()
    findOneById(id).map {
      ticket => ticket.copy(files = ticket.files :+ new Attachment(gridFSFile._id.get, f.getName()))
    }.map {
      ticket => save(ticket, WriteConcern.Safe)
    }
  }

  def deleteFile(id: ObjectId, f: ObjectId) = {
    findOneById(id).map {
      ticket => ticket.copy(files = ticket.files.filterNot(x => x.id == f))
    }.map {
      ticket => save(ticket, WriteConcern.Safe)
    }
    fileCollection.remove(f)
  }

  def getFile(file: ObjectId): Option[GridFSDBFile] = {
    fileCollection.findOne(file)
  }

}

/**
 * Trait used to convert to and from json
 */

trait TicketJson {

  implicit val ticketJsonWrite = (
    (__ \ "id").write[ObjectId] ~
    (__ \ "number").write[Long] ~
    (__ \ "summary").write[String] ~
    (__ \ "detail").write[String] ~
    (__ \ "status").write[String] ~
    (__ \ "ticketType").write[String] ~
    (__ \ "priority").write[String] ~
    (__ \ "severity").write[String] ~
    (__ \ "progress").write[Int] ~
    (__ \ "estimate").write[Option[String]] ~
    (__ \ "created").write[Date] ~
    (__ \ "updated").writeNullable[Date] ~
    (__ \ "reporter").writeNullable[User] ~
    (__ \ "assignee").writeNullable[User] ~
    (__ \ "project").writeNullable[Project] ~
    (__ \ "files").lazyWrite(Writes.traversableWrites[Attachment](attachmentJsonWrite)) ~
    (__ \ "histories").lazyWrite(Writes.traversableWrites[History](historyJsonWrite)))(unlift(Ticket.unapply))

  implicit val ticketJsonRead = (
    (__ \ "id").read[ObjectId].orElse(Reads.pure(new ObjectId)) ~
    (__ \ "number").read[Long].orElse(Reads.pure(0)) ~
    (__ \ "summary").read[String] ~
    (__ \ "detail").read[String] ~
    (__ \ "status").read[String] ~
    (__ \ "ticketType").read[String] ~
    (__ \ "priority").read[String] ~
    (__ \ "severity").read[String] ~
    (__ \ "progress").read[Int] ~
    (__ \ "estimate").readNullable[String] ~
    (__ \ "created").read[Date].orElse(Reads.pure(new Date())) ~
    (__ \ "updated").readNullable[Date] ~
    (__ \ "reporter").readNullable[User] ~
    (__ \ "assignee").readNullable[User] ~
    (__ \ "project").readNullable[Project] ~
    (__ \ "files").lazyRead(list[Attachment](attachmentJsonRead)) ~
    (__ \ "histories").lazyRead(list[History](historyJsonRead)))(Ticket.apply _)

  implicit val historyJsonWrite: Writes[History] = (
    (__ \ "id").write[ObjectId] ~
    (__ \ "log").write[String] ~
    (__ \ "user").write[String] ~
    (__ \ "created").write[Date])(unlift(History.unapply))

  implicit val historyJsonRead: Reads[History] = (
    (__ \ "id").read[ObjectId].orElse(Reads.pure(new ObjectId)) ~
    (__ \ "log").read[String] ~
    (__ \ "user").read[String] ~
    (__ \ "created").read[Date].orElse(Reads.pure(new Date())))(History.apply _)

  implicit val attachmentJsonWrite: Writes[Attachment] = (
    (__ \ "id").write[ObjectId] ~
    (__ \ "filename").write[String])(unlift(Attachment.unapply))

  implicit val attachmentJsonRead: Reads[Attachment] = (
    (__ \ "id").read[ObjectId] ~
    (__ \ "filename").read[String])(Attachment.apply _)

}

trait TicketSlimJson {

  implicit val ticketJsonWrite = new Writes[Ticket] {
    def writes(t: Ticket): JsValue = {
      Json.obj(
        "id" -> t.id,
        "number" -> t.number,
        "summary" -> t.summary,
        "status" -> t.status,
        "ticketType" -> t.ticketType,
        "reporter" -> t.reporter,
        "assignee" -> t.assignee)
    }
  }

}

