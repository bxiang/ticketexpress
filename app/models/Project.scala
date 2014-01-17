package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import pkgContext._

case class Project(
  id: ObjectId = new ObjectId,
  name: String,
  created: Date = new Date(),
  owner: Option[User] = None
)

object Project extends ProjectDAO with ProjectJson

trait ProjectDAO extends ModelCompanion[Project, ObjectId] {
  def collection = mongoCollection("projects")
  val dao = new SalatDAO[Project, ObjectId](collection) {}

  // Indexes
  collection.ensureIndex(DBObject("name" -> 1), "project_idx", unique = true)

  // Queries
  def findOneByProjectname(name: String): Option[Project] = dao.findOne(MongoDBObject("name" -> name))
  
}

/**
 * Trait used to convert to and from json
 */
trait ProjectJson {

  implicit val projectJsonWrite = (
    (__ \ "id").write[ObjectId] ~
    (__ \ "name").write[String] ~
    (__ \ "created").write[Date] ~
    (__ \ "owner").writeNullable[User]
  )(unlift(Project.unapply))

  implicit val projectJsonRead = (
    (__ \ "id").read[ObjectId] ~
    (__ \ "name").read[String] ~
    (__ \ "created").read[Date].orElse(Reads.pure(new Date())) ~
    (__ \ "owner").readNullable[User]
  )(Project.apply _)
}
