package nl.whitedove.washetdroogofniet

import org.joda.time.DateTime

internal class StatistiekRecords {

    var maxWindDatum: DateTime? = null
    var maxWind: Int = 0
    var maxWindRichting: WeerHelper.WindDirection? = null
    var minTempDatum: DateTime? = null
    var minTemp: Int = 0
    var maxTempDatum: DateTime? = null
    var maxTemp: Int = 0
    var natsteMaand: DateTime? = null
    var percentNat: Float = 0.toFloat()
    var droogsteMaand: DateTime? = null
    var percentDroog: Float = 0.toFloat()
    var langstePeriodeDroogVanaf: DateTime? = null
    var langstePeriodeDroogTm: DateTime? = null
    var langstePeriodeNatVanaf: DateTime? = null
    var langstePeriodeNatTm: DateTime? = null

}

