package conf

import play.api.Play
import play.api.Play.current


object Configuration {

  val paKey = Play.application.configuration.getString("pa.apiKey").getOrElse {
    throw new IllegalStateException("Missing PA_API_KEY system property")
  }

}
