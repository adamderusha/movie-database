package utility

import play.api.libs.ws._
import scala.concurrent._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._

import play.api.Logger

import models.Movie

class DataGrabber(api_key: String) {
  // Used to URL Encode a string
  private def encode(url: String): String = java.net.URLEncoder.encode(url, "UTF-8")

  // Takes a movie JSON value and extracts the relevent data from it
  private def generateMovieData(data: JsValue)
    : (String, Int, String, String) = {
    try {
      val title = (data \ "title").as[String]
      val year = (data \ "year").as[Int]
      val poster = (data \ "posters" \ "profile").as[String]
      val link = (data \ "links" \ "self").as[String]
      (title, year, poster, link)
    } catch {
      case js: Exception => { 
        Logger.warn("generateMovieData - Could not parse the following JSON: " + Json.stringify(data))
        ("", 0, "","")
      }
    }
  }

  // Builds a url like http://www.site.com/api?param1=value1&param2=value2 etc.
  private def urlBuilder(base: String, params: Map[String, String]): String = {
    val paramString = params.map(t => "%s=%s".format(t._1, t._2)).mkString("&")
    "%s?%s".format(base, paramString)
  }

  def getMovieList(title: String, page_limit: Int = 5): List[Movie] = {
    //Get movie data from rottentomatoes api
    val movieUrl = "http://api.rottentomatoes.com/api/public/v1.0/movies.json"
    val movieParams = Map(
      "apikey" -> api_key,
      "page_limit" -> page_limit.toString,
      "q" -> encode(title)
    )
    val url = urlBuilder(movieUrl, movieParams)
    val jsonData = WS.url(url).get

    // Now to go through the JSON returned.
    val movies: Future[List[Movie]] = jsonData.flatMap { response =>
      val movieJson = response.json

      // This will take the JSON, make a list of the movies JSON, then transform it into a tuple of data needed
      val rawMovieData = (movieJson \ "movies").as[List[JsValue]].map(generateMovieData(_)).toList

      // Any errors in data get filtered out (ie missing a year value).
      val filteredMovieData = rawMovieData.filter(t => !t._1.isEmpty)


      // For each movie, request from the api more info (we need the genres)
      val result: List[Future[Movie]] = filteredMovieData.map { t =>
        val moreMovieInfoUrl = urlBuilder(t._4, Map("apikey" -> api_key))
        val genreData = WS.url(moreMovieInfoUrl).get

        // Go through the JSON, extract the genres, and create the Movie class
        genreData.map { genre =>
          val genreJson = genre.json
          val genres = (genreJson \ "genres").as[List[String]]
          t match {
            case (t, y, p, _) => Movie(t, y, p, genres)
          }
        }
      }

      // Finally convert the result into a Future[List of movies]
      Future.sequence(result)
    }
   
    // Wait for the data to come in
    Await.result(movies, Duration.Inf).asInstanceOf[List[Movie]]
  }
}
