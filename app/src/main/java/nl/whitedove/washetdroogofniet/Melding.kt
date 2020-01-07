package nl.whitedove.washetdroogofniet

import org.joda.time.DateTime

class Melding {
    var locatie: String? = null
    var datum: DateTime? = null
    var nat: Boolean? = null
    var droog: Boolean? = null
    var id: String? = null
    var error: String? = null
    var temperatuur: Int? = null
    var weerType: WeerHelper.WeerType? = null
    var windSpeed: Int? = null
    var windDir: WeerHelper.WindDirection? = null
    var land: String? = null
}
