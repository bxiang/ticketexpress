package models

import java.util.Date

import com.mongodb.casbah.Imports.DBObject
import com.mongodb.casbah.Imports.MongoDBObject
import com.novus.salat.dao.ModelCompanion
import com.novus.salat.dao.SalatDAO

import pkgContext._
import play.api.Play.current
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.json.__
import se.radley.plugin.salat.Binders.ObjectId
import se.radley.plugin.salat.Binders.objectIdReads
import se.radley.plugin.salat.Binders.objectIdWrites
import se.radley.plugin.salat.mongoCollection

case class User(
  id: ObjectId = new ObjectId,
  username: String,
  password: String,
  email: String,
  firstname: Option[String] = None,
  lastname: Option[String] = None,
  role: Option[String] = None,
  created: Date = new Date(),
  activated: Boolean = false
)

object User extends UserDAO with UserJson

trait UserDAO extends ModelCompanion[User, ObjectId] {
  def collection = mongoCollection("users")
  val dao = new SalatDAO[User, ObjectId](collection) {}

  // Indexes
  collection.ensureIndex(DBObject("username" -> 1), "user_idx", unique = true)

  // Queries
  def findOneByUsername(username: String): Option[User] = dao.findOne(MongoDBObject("username" -> username))
  
  def authenticate(username: String, password: String): Option[User] = findOne(DBObject("username" -> username, "password" -> password))

  def list: Seq[(String,String)] = {
    find(MongoDBObject.empty).map(it => (it.id.toString, it.username + "(" + it.firstname + " " + it.lastname + ")")).toSeq
  }

}

/**
 * Trait used to convert to and from json
 */
trait UserJson {

  implicit val userJsonWrite = new Writes[User] {
    def writes(u: User): JsValue = {
      Json.obj(
        "id" -> u.id,
        "username" -> u.username,
        "password" -> "",
        "email" -> u.email,
        "firstname" -> u.firstname,
        "lastname" -> u.lastname,
        "role" -> u.role,
        "created" -> u.created,
        "activated" -> u.activated)
    }
  }

  implicit val userJsonRead = (
    (__ \ "id").read[ObjectId].orElse(Reads.pure(new ObjectId)) ~
    (__ \ "username").read[String] ~
    (__ \ "password").read[String].orElse(Reads.pure("")) ~
    (__ \ "email").read[String] ~
    (__ \ "firstname").readNullable[String] ~
    (__ \ "lastname").readNullable[String] ~
    (__ \ "role").readNullable[String] ~
    (__ \ "created").read[Date].orElse(Reads.pure(new Date())) ~
    (__ \ "activated").read[Boolean]
  )(User.apply _)


}
