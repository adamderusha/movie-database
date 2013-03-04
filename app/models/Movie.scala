package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class Movie(id:     Long,
                 title:  String,
                 year:   Int,
                 poster: String)

object Movie {
  val movie = {
    get[Long]("id") ~
    get[String]("title") ~
    get[Int]("year") ~
    get[String]("poster") map {
      case id~title~year~poster => Movie(id, title, year, poster)
    }
  }

  def all(): List[Movie] = DB.withConnection { implicit c =>
    SQL("SELECT * FROM movie ORDER BY title ASC").as(movie *)
  }
}


