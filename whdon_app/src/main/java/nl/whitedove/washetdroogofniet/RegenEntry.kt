package nl.whitedove.washetdroogofniet

internal class RegenEntry {

    var tijd: String = ""
    var regen: Int = 0

    constructor(tijd: String, regen: Int) {
        this.tijd = tijd
        this.regen = regen
    }

    constructor()

}