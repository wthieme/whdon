package nl.whitedove.washetdroogofniet

import org.joda.time.DateTime

internal class Statistiek1Dag {

    var datum: DateTime? = null
    var aantalDroog: Int = 0
    var aantalNat: Int = 0
    var minTemperatuur: Float = 0.toFloat()
    var maxTemperatuur: Float = 0.toFloat()
}
