package nl.whitedove.washetdroogofniet

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

internal object Database {
    var mMelding = Melding()
    var mMeldingen = ArrayList<Melding>()

    internal object Names {
        var collectionName = "meldingen"
        const val datum = "datum"
        const val droog = "droog"
        const val nat = "nat"
        const val id = "id"
        const val locatie = "locatie"
        const val land = "land"
        const val temperatuur = "temperatuur"
        const val weerType = "weerType"
        const val windDir = "windDir"
        const val windSpeed = "windSpeed"
    }

    private fun getCollectionName(): String {
        if (Helper.DEBUG)
            return "DEV" + Database.Names.collectionName
        else
            return Database.Names.collectionName
    }

    fun meldingOpslaan(melding: Melding): Melding {

        if (melding.locatie == null ||
                melding.locatie.equals("", ignoreCase = true) ||
                melding.locatie.equals("Onbekend", ignoreCase = true) ||
                melding.locatie.equals("Nederland", ignoreCase = true)) {
            val response = Melding()
            response.error = "Melding niet mogelijk, locatie onbekend"
            return response
        }

        val db = FirebaseFirestore.getInstance()
        val doc = java.util.HashMap<String, Any>()

        doc[Names.datum] = melding.datum!!.millis
        doc[Names.droog] = melding.droog!!
        doc[Names.nat] = melding.nat!!
        doc[Names.id] = melding.id!!
        doc[Names.land] = melding.land!!
        doc[Names.temperatuur] = melding.temperatuur!!
        doc[Names.weerType] = melding.weerType!!.value
        doc[Names.windDir] = melding.windDir!!.value
        doc[Names.windSpeed] = melding.windSpeed!!
        doc[Names.locatie] = melding.locatie!!
        db.collection(getCollectionName()).document().set(doc)

        melding.error = ""
        return melding
    }

    fun getLaatsteMelding(id: String, callback: Runnable) {
        mMelding = Melding()
        mMelding.error = "Geen meldingen"
        val db = FirebaseFirestore.getInstance()
        val fmt = ISODateTimeFormat.dateTime()
        db.collection(getCollectionName())
                .whereEqualTo(Names.id, id)
                .orderBy(Names.datum, Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            val doc = document.data

                            mMelding.datum = DateTime(doc[Database.Names.datum] as Long)
                            mMelding.droog = doc[Names.droog] as Boolean
                            mMelding.nat = doc[Names.nat] as Boolean
                            mMelding.id = doc[Names.id] as String
                            mMelding.land = doc[Names.land] as String
                            mMelding.locatie = doc[Names.locatie] as String
                            mMelding.temperatuur = (doc[Names.temperatuur] as Long).toInt()
                            mMelding.weerType = WeerHelper.WeerType.valueOf((doc[Names.weerType] as Long).toInt())
                            mMelding.windSpeed = (doc[Names.windSpeed] as Long).toInt()
                            mMelding.windDir = WeerHelper.WindDirection.valueOf((doc[Names.windDir] as Long).toInt())
                        }
                        callback.run()
                    }
                }
    }

    fun getAlleMeldingenVanaf(datumVanaf: DateTime, callback: Runnable
    ) {
        val meldingen = ArrayList<Melding>()
        val db = FirebaseFirestore.getInstance()
        db.collection(getCollectionName())
                .whereGreaterThanOrEqualTo(Names.datum, datumVanaf.millis)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            val doc = document.data

                            val melding = Melding()
                            melding.error = ""
                            melding.datum = DateTime(doc[Database.Names.datum] as Long)
                            melding.droog = doc[Names.droog] as Boolean
                            melding.nat = doc[Names.nat] as Boolean
                            melding.id = doc[Names.id] as String
                            melding.land = doc[Names.land] as String
                            if (melding.land.isNullOrEmpty()) melding.land = "NL"
                            melding.locatie = doc[Names.locatie] as String
                            melding.temperatuur = (doc[Names.temperatuur] as Long).toInt()
                            melding.weerType = WeerHelper.WeerType.valueOf((doc[Names.weerType] as Long).toInt())
                            melding.windSpeed = (doc[Names.windSpeed] as Long).toInt()
                            melding.windDir = WeerHelper.WindDirection.valueOf((doc[Names.windDir] as Long).toInt())

                            meldingen.add(melding)
                        }

                        mMeldingen = ArrayList()
                        for (m in meldingen)
                            mMeldingen.add(m)

                        callback.run()
                    }
                }
    }
}