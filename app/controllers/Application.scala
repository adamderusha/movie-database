package controllers


import play.api._
import play.api.mvc._
import play.api.Play

import play.api.data._
import play.api.data.Forms._

import play.api.Logger

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
    val getter = new DataGrabber(api_key)
    val movies = getter.getMovieList("Toy Story")
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
