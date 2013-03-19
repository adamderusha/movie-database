package controllers


import play.api._
import play.api.mvc._
import play.api.Play

import play.api.data._
import play.api.data.Forms._

import play.api.Logger

import play.api.libs.ws._
import scala.concurrent._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._

import models.Movie

object Application extends Controller {
  val api_key = Play.current.configuration.getString("application.rotten_api_key") match {
    case Some(key) => key
    case None => {
      Logger.warn("apikey not found!")
      ""
    }
  }

  def encode(url: String): String = java.net.URLEncoder.encode(url, "UTF-8")

  def getMovieList(title: String, page_limit: Int = 10): List[Movie] = {
    val url = "http://api.rottentomatoes.com/api/public/v1.0/movies.json"
    val parameters = "apikey=%s&q=%s&page_limit=%d".format(api_key, encode(title), page_limit)
    val finalUrl = url + "?" + parameters
    Logger.debug(finalUrl)
    val jsonData = WS.url(finalUrl).get
    val movieData: Future[List[Movie]] = jsonData.flatMap { response =>
      val json = response.json
      val moviesTemp = (json \ "movies").as[List[JsValue]].map { x => 
        val title = (x \ "title").as[String]
        val year = (x \ "year").as[Int]
        val poster = (x \ "posters" \ "original").as[String]
        val link = (x \ "links" \ "self").as[String]
        (title, year, poster, link)
      }.toList
      val result: List[Future[Movie]] = moviesTemp.map { t =>
        val url = "%s?apikey=%s".format(t._4, api_key)
        val genreData = WS.url(url).get
        genreData.map { genre =>
          val genreJson = genre.json
          val genres = (genreJson \ "genres").as[List[String]]
          t match {
            case (t, y,p, _) => Movie(t,y,p,genres)
          }
        }
      }
      Future.sequence(result)
    }
    
    Await.result(movieData, 3 second).asInstanceOf[List[Movie]]
  }

  def index = Action {
    getMovieList("Toy Story")
    Ok(views.html.index(Movie.all()))
  }

  def lookupPage = Action {
    Ok(views.html.lookup())
  }

  def lookupAction = Action { implicit request =>
    val inputForm = Form(
      "title" -> text
    )
    val title = inputForm.bindFromRequest.get
   
    Redirect(routes.Application.index)
  }

  def add = TODO
  
}
