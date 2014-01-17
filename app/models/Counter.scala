package models

import play.api.Play.current
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
// import com.mongodb.casbah.Imports._
import com.mongodb.casbah.Imports.{ WriteConcern }
import com.mongodb.casbah.query.Imports._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import pkgContext._

case class Counter(
  id: ObjectId = new ObjectId,
  name: String,
  sequence: Long)

object Counter extends ModelCompanion[Counter, ObjectId] {
  def collection = mongoCollection("counters")
  val dao = new SalatDAO[Counter, ObjectId](collection) {}

  // Indexes
  collection.ensureIndex(DBObject("name" -> 1), "counter_idx", unique = true)

  def getNextSequence(name: String): Long = {
    val actionMDBO = MongoDBObject("name" -> name)
    val fieldsMDBO = MongoDBObject()
    val sortMDBO = MongoDBObject("sequence" -> 1)
    val newId = collection.findAndModify(actionMDBO, fieldsMDBO, sortMDBO, false, $inc("sequence" -> 1), true, true)
    newId match {
      case None => 0
      case Some(obj) => obj.as[Long]("sequence")
    }
  }
}

