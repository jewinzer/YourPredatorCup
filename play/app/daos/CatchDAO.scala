package daos

import model.Catch
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CatchDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                        (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Catches = TableQuery[CatchTable]

  def all(): Future[Seq[Catch]] = db.run(Catches.result)

  def insert(c: Catch): Future[Unit] = db.run(Catches.map(ca => (ca.name, ca.species, ca.length)) += (c.name, c.species, c.length)).map { _ => () }

  def delete(id: Int): Future[Unit] = db.run(Catches.filter( c => c.id === id).delete).map { _ => ()}

  def update(c: Catch): Future[Unit] = db.run(Catches.insertOrUpdate(c)).map { _ => ()}

  private class CatchTable(tag: Tag) extends Table[Catch](tag, "CATCH") {

    def id = column[Int]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def species = column[String]("SPECIES")
    def length = column[Int]("LENGTH")

    def * = (id, name, species, length) <> (Catch.tupled, Catch.unapply)
  }
}