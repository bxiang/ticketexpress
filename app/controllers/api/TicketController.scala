package controllers.api

import play.api.mvc._
import play.api._
import play.api.libs.json._
import play.api.libs.iteratee.Enumerator
import models._
import controllers.api.Actions._

import com.mongodb.casbah.WriteConcern
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import java.util.Date
import java.io.File
import java.text.SimpleDateFormat

object TicketController extends Controller with TicketJson {

  def create = JsonAction[Ticket] { requestTicket =>
    val ticket = requestTicket.copy(id = new ObjectId, number = Ticket.getNextNumber, created = new Date(), updated = Some(new Date()))
    Ticket.save(ticket, WriteConcern.Safe)
    Ok(Json.toJson(ticket))
  }

  def view(id: ObjectId) = Action {
    Ticket.findOneById(id).map { ticket =>
      Ok(Json.toJson(ticket))
    } getOrElse {
      NotFound
    }
  }

  def update(id: ObjectId) = JsonAction[Ticket] { requestTicket =>
    val ticket = requestTicket.copy(id, updated = Some(new Date()))
    Ticket.save(ticket, WriteConcern.Safe)
    Ok(Json.toJson(ticket))
  }

  def upload(id: ObjectId) = Action(parse.temporaryFile) { request =>
    val fileName = request.headers.get("x-file-name").get
    if (fileName.endsWith(".zip")) {
      Ok("{\"success\": false, \"reason\": \"zip file is not supported yet!\"}")
    } else {
      val file = new File("/tmp/images/" + fileName)
      request.body.moveTo(file, true)
      Ticket.addFile(id, file)
      Ok("{\"success\": true}")
    }
  }

  def getFile(file: ObjectId) = Action {
    Logger.debug("retriev file " + file)
    Ticket.getFile(file) match {
      case Some(f) => SimpleResult(
        ResponseHeader(OK, Map(
          CONTENT_LENGTH -> f.length.toString,
          CONTENT_TYPE -> f.contentType.getOrElse(BINARY),
          CONTENT_DISPOSITION -> {"attachment; filename=" + f.filename.getOrElse(file.toString)},
          DATE -> new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", java.util.Locale.US).format(f.uploadDate))),
        Enumerator.fromStream(f.inputStream))
      case None => NotFound
    }
  }

}

object TicketSlimController extends Controller with TicketSlimJson {

  /**
   * @param page Current page number (starts from 1)
   * @param orderBy Column to be sorted. first column is 1. >0 is ascending; <0 is descending
   * @param filter Filter applied on ticket summary
   */
  def index(page: Int, orderBy: Int, filter: String) = Action {
    val pageSize = 20
    val totalPages = Ticket.count() / pageSize + 1
    val tickets = Ticket.findAll().sort(MongoDBObject("number" -> 1)).skip((page - 1) * pageSize).limit(pageSize).toList
    Ok(Json.toJson(
      Map(
        "tickets" -> Json.toJson(tickets),
        "totalPages" -> Json.toJson(totalPages))))
  }

  def delete(id: ObjectId) = Action {
    Ticket.removeById(id)
    Ok("")
  }
}

