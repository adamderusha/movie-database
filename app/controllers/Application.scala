package controllers

import play.api._
import play.api.mvc._

import models.Movie

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index(Movie.all()))
  }
  
}
