package daos

import model.Produkt
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ProduktDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                          (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Produkte = TableQuery[ProduktTable]

  def all(): Future[Seq[Produkt]] = db.run(Produkte.result)

  def insert(produkt: Produkt): Future[Unit] = db.run(Produkte.map(p => (p.name, p.price)) += (produkt.name, produkt.price)).map { _ => () }

  def delete(id: Int): Future[Unit] = db.run(Produkte.filter( p => p.id === id).delete).map { _ => ()}

  def update(produkt: Produkt): Future[Unit] = db.run(Produkte.insertOrUpdate(produkt)).map { _ => ()}

  private class ProduktTable(tag: Tag) extends Table[Produkt](tag, "PRODUKT") {

    def id = column[Int]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def price = column[Int]("PRICE")

    def * = (id, name, price) <> (Produkt.tupled, Produkt.unapply)
  }
}
