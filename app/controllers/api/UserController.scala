package controllers.api

import play.api.mvc._
import play.api.libs.json._
import models._
import controllers.api.Actions._

import com.mongodb.casbah.WriteConcern
import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import play.api.Play.current
import com.typesafe.plugin._
import service._


object UserController extends Controller {

  def index() = Action {
    val users = User.findAll().toList
    Ok(Json.toJson(users))
  }

  def create = JsonAction[User] { user =>
    User.save(user, WriteConcern.Safe)
    TxEventBus.publish(NewUserEvent(user.id))
    // val mail = use[MailerPlugin].email
    // mail.setSubject("Signup confirmation")
    // mail.addRecipient("brian.xiang@gmail.com")
    // mail.addFrom("someone@gmail.com")
    // mail.sendHtml("<html>Please click <a href=\"http://localh
    //   ost:9000/#/confirm/" + user.id + "\">here</a> to confirm your email.</html>")
    Ok(Json.toJson(user))
  }

  def view(id: ObjectId) = Action {
    User.findOneById(id).map { user =>
      Ok(Json.toJson(user))
    } getOrElse {
      NotFound
    }
  }

  def update(id: ObjectId) = JsonAction[User] { requestUser =>
    val user = requestUser.copy(id)
    User.save(user, WriteConcern.Safe)
    Ok(Json.toJson(user))
  }

  def delete(id: ObjectId) = Action {
    User.removeById(id)
    Ok("")
  }
}
