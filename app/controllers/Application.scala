package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import models.Movie

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index(Movie.all()))
  }

  def lookupPage = Action {
    Ok(views.html.lookup())
  }

  def lookupAction = Action {
    Redirect(routes.Application.index)
  }

  def add = TODO
  
}
