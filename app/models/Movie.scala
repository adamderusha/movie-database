package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class Movie(id:     Long,
                 title:  String,
                 year:   Int,
                 poster: String,
                 genres: List[String])

object Movie {
  val multiparser = {
    get[Long]("id") ~
    get[String]("title") ~
    get[Int]("year") ~
    get[String]("poster") ~ 
    get[String]("genre") map {
      case id~title~year~poster~genre => (id, title, year, poster, genre)
    }
  }

  def all(): List[Movie] = DB.withConnection { implicit c =>
    val rows = SQL("""
                   SELECT m.id, m.title, m.year, m.poster, g.genre
                   FROM movie m, genre g 
                   WHERE m.id = g.movie_id
                   ORDER BY title ASC
                   """).as(multiparser *)
    val data = rows.map(r => (r._2,r._5)).groupBy(_._1).mapValues { l => l.map(_._2) }
    rows.map(r => Movie(r._1, r._2, r._3, r._4, data(r._2))).distinct
  }
}


