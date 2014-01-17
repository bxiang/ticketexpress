package service

import akka.actor.Actor
import java.net.URL
import models._
import play.api.Configuration
import play.api.i18n.Messages
import play.api.Play.current
import com.typesafe.plugin._
import play.api._


class EmailService(configuration: Configuration) extends Actor {

  case class EmailResult(
    subject: String,
    recipient: String,
    body: String
  )

  val maybeHost     = configuration.getString("smtp.host")
  val maybePort     = configuration.getString("smtp.port")
  val maybeSsl      = configuration.getBoolean("smtp.ssl")
  val maybeUser     = configuration.getString("smtp.user")
  val maybePassword = configuration.getString("smtp.password")
  val maybeFrom     = configuration.getString("smtp.from")

  def receive = {
    case event: TxEvent => sendEmail(event)
  }

  def sendEmail(event: TxEvent) = {

    maybeHost.map { host =>
      val maybeResult: Option[EmailResult] = event match {
        case nue: NewUserEvent => {
          User.findOneById(nue.userId).map { user => EmailResult(
              subject = Messages("email.subject"),
              recipient = user.email,
              body = "<html>Please click <a href=\"http://localhost:9000/#/confirm/" + user.id + "\">here</a> to confirm your email.</html>"
            )
          }
        }
        // Unknown event, just do nothing
        case _ => None
      }
      maybeResult.map { result =>
        Logger.debug("Sending email to " + result.recipient) 
        val mail = use[MailerPlugin].email
        mail.setSubject(result.subject)
        mail.addRecipient(result.recipient)
        mail.addFrom(maybeFrom.getOrElse("brian@gmail.com"))
        mail.sendHtml(result.body)
      }
    }
  }
}

object EmailService {
  def triggerEvents = List("user/created", "ticket/created")
}