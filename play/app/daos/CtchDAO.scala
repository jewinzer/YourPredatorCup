package daos

import model.Ctch
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CtchDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                       (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Ctches = TableQuery[CtchTable]

  def all(): Future[Seq[Ctch]] = db.run(Ctches.result)

  def insert(ctch: Ctch): Future[Unit] = db.run(Ctches.map(c => (c.name, c.species, c.length)) += (ctch.name, ctch.species, ctch.length)).map { _ => () }

  def delete(id: Int): Future[Unit] = db.run(Ctches.filter( c => c.id === id).delete).map { _ => ()}

  def update(ctch: Ctch): Future[Unit] = db.run(Ctches.insertOrUpdate(ctch)).map { _ => ()}

  private class CtchTable(tag: Tag) extends Table[Ctch](tag, "CTCH") {

    def id = column[Int]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def species = column[String]("SPECIES")
    def length = column[Int]("LENGTH")

    def * = (id, name, species, length) <> (Ctch.tupled, Ctch.unapply)
  }
}