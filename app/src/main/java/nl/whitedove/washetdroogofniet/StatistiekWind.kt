package nl.whitedove.washetdroogofniet

class StatistiekWind {

    var windDir: WeerHelper.WindDirection = WeerHelper.WindDirection.Onbekend
    var windOmschrijving: String = ""
    var aantal: Int = 0
    var avgWindSpeed: Float = 0.toFloat()
    var minWindSpeed: Float = 0.toFloat()
    var maxWindSpeed: Float = 0.toFloat()
    var percentage: Float = 0.toFloat()
}
