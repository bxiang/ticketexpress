package models

import com.novus.salat.{ TypeHintFrequency, StringTypeHintStrategy, Context }
import play.api.Play
import play.api.Play.current
import play.api.libs.json.Format
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import java.util.Date

package object pkgContext {

  implicit val context = {
    val context = new Context {
      val name = "global"
      override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = "_t")
    }
    context.registerGlobalKeyOverride(remapThis = "id", toThisInstead = "_id")
    context.registerClassLoader(Play.classloader)
    context
  }


  val datePattern = "yyyy-MM-dd HH:mm:ss"
  implicit val dateFormat = Format[Date](Reads.dateReads(datePattern), Writes.dateWrites(datePattern))

  // implicit val dateJsonWrite: Writes[Date] = Writes.dateWrites(datePattern)
  // implicit val dateJsonRead: Reads[Date] = Reads.dateReads(datePattern)

}
