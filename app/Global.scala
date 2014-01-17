import com.mongodb.casbah.Imports._
import play.api._
import libs.ws.WS
import akka.actor._
import models._
import service._
import se.radley.plugin.salat._
import play.api.Play.current

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    if (Ticket.count(DBObject(), Nil, Nil) == 0) {
      insertTicketTestData() 
    }
    if(Company.count(MongoDBObject.empty) == 0) {
      insertComputerTestData() 
    }

    val actsystem = ActorSystem("TicketExpress")

    val emailService = actsystem.actorOf(Props(new EmailService(Play.configuration)))

    EmailService.triggerEvents.foreach { event =>
      Logger.debug("Subscribed Email service to '" + event + "'")
      TxEventBus.subscribe(emailService, event)
    }

  }

  def insertTicketTestData() = {
    Logger.info("Creating new tickets since none existed in mongo")

    val userId1 = User.insert(User(
      username = "bxiang",
      password = "123",
      email = "brian@thomsonreuters.com",
      firstname = Some("Brian"),
      lastname = Some("Xiang")))

    val userId2 = User.insert(User(
      username = "osyafriza",
      password = "456",
      email = "odon@thomsonreuters.com",
      firstname = Some("Odon"),
      lastname = Some("Syafriza")))

    val projectId1 = Project.insert(Project(
      name = "CCMS",
      owner = User.findOneById(id = userId1.getOrElse(new ObjectId))))

    Counter.insert(Counter(
      name = "Ticket",
      sequence = 0))

    val log1 = History(
      log = "Changed from Open to In Process",
      user = "Brian Xiang")

    val log2 = History(
      log = "This is not a bug",
      user = "Amy Zhu")

    val log3 = History(
      log = "Changed from Completed to Tested",
      user = "Brian Xiang")

    val log4 = History(
      log = "Can not reproduce",
      user = "Amy Zhu")

    val ticketId1 = Ticket.insert(
      Ticket(
        number = Ticket.getNextNumber,
        summary = "First bug",
        detail = "First bug detail",
        status = "Closed",
        ticketType = "Defect",
        reporter = User.findOneById(id = userId1.getOrElse(new ObjectId)),
        project = Project.findOneById(id = projectId1.getOrElse(new ObjectId)),
        histories = log1 :: log2 :: Nil
      )
    )

    for( i <- 2 to 100)
      Ticket.insert(
        Ticket(
          number = Ticket.getNextNumber,
          summary = "Bug " + i,
          detail = "Detail description of bug " + i,
          status = "Open",
          ticketType = "Defect",
          reporter = User.findOneById(id = userId1.getOrElse(new ObjectId)),
          project = Project.findOneById(id = projectId1.getOrElse(new ObjectId)),
          histories = log3 :: log4 :: Nil
        )
      )

  }

  def insertComputerTestData() = {
    val c = createCompanies();
    createComputers(c)
  }

  def createCompanies() = {
    Logger.info("Creating new companies since none existed in mongo")
    val companies = play.Play.application().getFile("data/companies.csv");
    val lines = scala.io.Source.fromFile(companies).getLines
    val savedCompanies = lines.map{line => 
      val id = Company.insert(Company(name = line))
      line -> id
    }

    savedCompanies.toMap
  }

  def createComputers(company: Map[String, Option[ObjectId]]) = {
    Logger.info("Creating new computers since none existed in mongo")
    val companies = play.Play.application().getFile("data/computers.csv");
    val lines = scala.io.Source.fromFile(companies).getLines
    lines.foreach{line => 
      val elements = line.split(",") 

      val computer = Computer(
        name = elements(0),
        introduced =   if(elements(1) == "null") None else Some(dateFromString(elements(1))),
        discontinued = if(elements(2) == "null") None else Some(dateFromString(elements(2))),
        companyId =    if(elements(3) == "null") None else company(elements(3))
      )

      Computer.insert(computer)
    }
  }

  def dateFromString(str:String) = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(str)

}
