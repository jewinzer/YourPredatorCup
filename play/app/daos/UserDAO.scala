package daos

import model.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
              (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val Users = TableQuery[UserTable]

  def exists(user: User): Future[Boolean] = {
    db.run(Users.filter(userRow => userRow.name ===user.name && userRow.password === user.password).result).map(userRows => userRows.nonEmpty)
  }

  def all(): Future[Seq[User]] = db.run(Users.result)

  def insert(user: User): Future[Unit] = db.run(Users.map(u => (u.name, u.password)) += (user.name, user.password)).map { _ => () }

  def delete(id: Int): Future[Unit] = db.run(Users.filter( u => u.id === id).delete).map { _ => ()}

  def update(user: User): Future[Unit] = db.run(Users.insertOrUpdate(user)).map { _ => ()}

  private class UserTable(tag: Tag) extends Table[User](tag, "USER") {
    def id = column[Int]("ID", O.PrimaryKey)
    def name = column[String]("NAME")
    def password = column[String]("PASSWORD")
    def * = (id, name, password) <> (User.tupled, User.unapply)
  }
}