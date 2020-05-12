import javax.inject.Inject
import play.api.http.DefaultHttpFilters
import play.filters.cors.CORSFilter
import play.filters.csrf.CSRFFilter

class Filters @Inject() (corsFilter: CORSFilter, csrfFilter: CSRFFilter)
  extends DefaultHttpFilters(corsFilter, csrfFilter)