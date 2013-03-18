package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

import scala.collection.mutable.HashMap
import org.postgresql.util.PSQLException

case class Movie(var id: Long = -1,
                 title:  String,
                 year:   Int,
                 poster: String,
                 genres: List[String] = Nil) {

  def save() = DB.withConnection { implicit c =>
    try {
      id = SQL(
      """
        INSERT INTO movie (title, year, poster) VALUES ({title}, {year}, {poster})
      """
      ).onParams(title, year, poster)
       .executeInsert(scalar[Long].single)
      genres.foreach { genre => SQL("INSERT INTO genre VALUES ({id}, {genre})").onParams(id, genre).execute() }
    } catch {
      case db: PSQLException => {
        val query = SQL("SELECT id FROM movie WHERE title = {title} AND year = {year}").onParams(title, year)
        id = (query().head)[Long]("id")
        SQL("UPDATE movie SET poster={poster} WHERE id={id}").onParams(poster, id).execute()
        SQL("DELETE FROM genre WHERE movie_id = {id}").onParams(id).execute()
        genres.foreach { genre => SQL("INSERT INTO genre VALUES ({id}, {genre})").onParams(id, genre).execute() }
      }
    }
  }
}

object Movie {
  val dataParser = {
    get[Long]("id") ~
    get[String]("title") ~
    get[Int]("year") ~ 
    get[String]("poster") ~
    get[String]("genre") map {
      case i~t~y~p~g => (i,t,y,p,g)
    }
  }

  def apply(title: String, year: Int, poster: String, genres: List[String]): Movie = {
    Movie(-1, title, year, poster, genres)
  }

  def all(): List[Movie] = DB.withConnection { implicit c =>
    val lookup: HashMap[(Long, String, Int, String), List[String]] = HashMap()
    val rows = SQL("""
      SELECT m.id, m.title, m.year, m.poster, g.genre
      FROM movie m, genre g 
      WHERE m.id = g.movie_id
      """).as(dataParser *)

    rows.foreach { t =>
      val genre = t._5
      val movieData = (t._1, t._2, t._3, t._4)
      val genres = lookup getOrElseUpdate (movieData, List())
      lookup.update(movieData, genres :+ genre)
    }
    
    val data = lookup.map(x => Movie(x._1._1, x._1._2, x._1._3, x._1._4, x._2)).toList
    data.sortBy(titleSorter(_))
  }

  private def titleSorter(movie: Movie): String = {
    val title = movie.title.toLowerCase.filter(_ != ' ')
    if (title.startsWith("the")) {
      return title.drop(3)
    }
    return title
  }
}
