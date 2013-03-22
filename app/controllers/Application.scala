package controllers


import play.api._
import play.api.mvc._
import play.api.Play

import play.api.data._
import play.api.data.Forms._

import play.api.Logger
import play.api.libs.json._

import models.Movie
import utility.DataGrabber

object Application extends Controller {
  val api_key = Play.current.configuration.getString("application.rotten_api_key") match {
    case Some(key) => key
    case None => {
      Logger.error("apikey not found!")
      //Should find a better way to handle this,
      //but this shouldn't run if the key is missing
      //since it's needed for functionality
      System.exit(1) 
      "" //To shut up the compiler...will never get here
    }
  }


  def index = Action {
    Ok(views.html.index(Movie.all()))
  }

  def lookupPage = Action {
    Ok(views.html.lookup())
  }

  def lookupMovie = Action { implicit request =>
    val params = request.queryString.map( t => t._1 -> t._2.mkString )
    val title = params.getOrElse("title", "")
    val getter = new DataGrabber(api_key)
    val movies = getter.getMovieList(title)
    Ok(Json.toJson(movies.map(Movie.makeJson(_))))
  }

  def add = TODO
  
}
