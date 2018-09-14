package nl.whitedove.washetdroogofniet

import org.joda.time.DateTime

internal class StatistiekRecords {

    var maxWindDatum: DateTime? = null
    var maxWind = 0
    var maxWindRichting: WeerHelper.WindDirection? = null
    var maxWindLocatie = ""

    var minTempDatum: DateTime? = null
    var minTemp = 0
    var minTempLocatie = ""

    var maxTempDatum: DateTime? = null
    var maxTempLocatie = ""
    var maxTemp = 0

    var natsteMaand: DateTime? = null
    var percentNat = 0f

    var droogsteMaand: DateTime? = null
    var percentDroog = 0f

    var langstePeriodeDroogVanaf: DateTime? = null
    var langstePeriodeDroogTm: DateTime? = null
    var langstePeriodeNatVanaf: DateTime? = null
    var langstePeriodeNatTm: DateTime? = null
}

