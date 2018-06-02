package nl.whitedove.washetdroogofniet

internal class StatistiekWind {

    var windDir: WeerHelper.WindDirection? = null
    var windOmschrijving: String? = null
    var aantal: Int = 0
    var avgWindSpeed: Float = 0.toFloat()
    var minWindSpeed: Float = 0.toFloat()
    var maxWindSpeed: Float = 0.toFloat()
    var percentage: Float = 0.toFloat()
}
