package nl.whitedove.washetdroogofniet

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import nl.whitedove.washetdroogofniet.backend.whdonApi.model.Melding
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*

internal class DatabaseHelper private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        var sql = ("CREATE TABLE " + TAB_MELDING + "("
                + MDG_ID + " INTEGER PRIMARY KEY NOT NULL,"
                + MDG_TELID + " TEXT NOT NULL,"
                + MDG_LOCATIE + " TEXT NOT NULL,"
                + MDG_DATUM + " INTEGER NOT NULL,"
                + MDG_DROOG + " INTEGER NOT NULL,"
                + MDG_NAT + " INTEGER NOT NULL,"
                + MDG_TEMPERATUUR + " INTEGER NOT NULL,"
                + MDG_WEERTYPE + " INTEGER NOT NULL,"
                + MDG_WINDDIR + " INTEGER NOT NULL,"
                + MDG_WINDSPEED + " INTEGER NOT NULL,"
                + MDG_LAND + " TEXT NOT NULL"
                + ")")
        db.execSQL(sql)

        sql = "CREATE INDEX IX1 ON $TAB_MELDING ($MDG_TELID)"
        db.execSQL(sql)

        sql = "CREATE INDEX IX2 ON $TAB_MELDING ($MDG_LOCATIE)"
        db.execSQL(sql)

        sql = "CREATE INDEX IX3 ON $TAB_MELDING ($MDG_DATUM)"
        db.execSQL(sql)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            val sql = "ALTER TABLE $TAB_MELDING ADD COLUMN $MDG_TEMPERATUUR INTEGER NOT NULL DEFAULT 999"
            db.execSQL(sql)
        }
        if (oldVersion < 3) {
            val sql = "ALTER TABLE $TAB_MELDING ADD COLUMN $MDG_WEERTYPE INTEGER NOT NULL DEFAULT 0"
            db.execSQL(sql)
        }
        if (oldVersion < 4) {
            var sql = "ALTER TABLE $TAB_MELDING ADD COLUMN $MDG_WINDDIR INTEGER NOT NULL DEFAULT 0"
            db.execSQL(sql)
            sql = "ALTER TABLE $TAB_MELDING ADD COLUMN $MDG_WINDSPEED INTEGER NOT NULL DEFAULT 0"
            db.execSQL(sql)
        }
        if (oldVersion < 5) {
            val sql = "ALTER TABLE $TAB_MELDING ADD COLUMN $MDG_LAND TEXT NOT NULL DEFAULT 'NL'"
            db.execSQL(sql)
        }
    }

    fun addMeldingen(meldingen: List<Melding>) {
        val db = this.writableDatabase
        db.execSQL("BEGIN TRANSACTION")
        for (melding in meldingen) {

            val selectQuery = ("SELECT"
                    + " " + MDG_ID
                    + " FROM " + TAB_MELDING
                    + " WHERE " + MDG_TELID + " = ?"
                    + " AND " + MDG_DATUM + " = ?"
                    + " LIMIT 1")

            val cursor: Cursor
            cursor = db.rawQuery(selectQuery, arrayOf(melding.id, java.lang.Long.toString(melding.datum!!)))
            val bestaat = cursor.moveToFirst()
            cursor.close()

            if (!bestaat) {
                val values = ContentValues()
                values.put(MDG_TELID, melding.id)
                values.put(MDG_LOCATIE, melding.locatie)
                values.put(MDG_DATUM, melding.datum)
                values.put(MDG_DROOG, if (melding.droog) 1 else 0)
                values.put(MDG_NAT, if (melding.nat) 1 else 0)
                values.put(MDG_TEMPERATUUR, melding.temperatuur)
                values.put(MDG_WEERTYPE, melding.weerType)
                values.put(MDG_WINDDIR, melding.windDir)
                values.put(MDG_WINDSPEED, melding.windSpeed)
                values.put(MDG_LAND, melding.land)
                db.insert(TAB_MELDING, null, values)
            }
        }
        db.execSQL("END TRANSACTION")
    }

    fun GetPersoonlijkeStatistiek(id: String): Statistiek {
        val selectQuery = ("SELECT"
                + " SUM(" + MDG_DROOG + ") AS DROOG,"
                + " SUM(" + MDG_NAT + ") AS NAT"
                + " FROM " + TAB_MELDING
                + " WHERE " + MDG_TELID + " = ?")

        val db = this.writableDatabase
        val cursor: Cursor
        cursor = db.rawQuery(selectQuery, arrayOf(id))

        val response = Statistiek()
        response.id = id

        if (cursor.moveToFirst()) {
            response.aantalDroog = cursor.getInt(0)
            response.aantalNat = cursor.getInt(1)
        } else {
            response.aantalDroog = 0
            response.aantalNat = 0
        }
        cursor.close()
        return response
    }

    fun GetPersoonlijkeStatsPerPlaats(id: String?): ArrayList<Statistiek> {
        var where = ""

        if (id != null) where = " WHERE $MDG_TELID = ?"
        val selectQuery = ("SELECT"
                + " " + MDG_LOCATIE + " AS LOCATIE,"
                + " " + MDG_LAND + " AS LAND,"
                + " SUM(" + MDG_DROOG + ") AS DROOG,"
                + " SUM(" + MDG_NAT + ") AS NAT,"
                + " MAX(" + MDG_DATUM + ") AS DATUM"
                + " FROM " + TAB_MELDING
                + where
                + " GROUP BY " + MDG_LOCATIE + "," + MDG_LAND
                + " ORDER BY " + MDG_DATUM + " DESC")

        val db = this.writableDatabase
        val cursor: Cursor
        if (id == null)
            cursor = db.rawQuery(selectQuery, null)
        else
            cursor = db.rawQuery(selectQuery, arrayOf(id))

        val stats = ArrayList<Statistiek>()

        if (cursor.moveToFirst()) {
            do {
                val stat = Statistiek()
                stat.locatie = cursor.getString(0)
                stat.land = cursor.getString(1)
                stat.aantalDroog = cursor.getInt(2)
                stat.aantalNat = cursor.getInt(3)
                stats.add(stat)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return stats
    }

    fun DeleteMeldingen() {
        val db = this.writableDatabase
        db.delete(TAB_MELDING, null, null)
    }

    fun GetLaatsteMelding(id: String): Melding {

        val selectQuery = ("SELECT"
                + " " + MDG_LOCATIE + ","
                + " " + MDG_DATUM + ","
                + " " + MDG_DROOG + ","
                + " " + MDG_NAT + ","
                + " " + MDG_TEMPERATUUR + ","
                + " " + MDG_WEERTYPE + ","
                + " " + MDG_WINDDIR + ","
                + " " + MDG_WINDSPEED + ","
                + " " + MDG_LAND
                + " FROM " + TAB_MELDING
                + " WHERE " + MDG_TELID + " = ?"
                + " ORDER BY " + MDG_DATUM + " DESC"
                + " LIMIT 1")

        val db = this.writableDatabase
        val cursor: Cursor
        cursor = db.rawQuery(selectQuery, arrayOf(id))

        val melding = Melding()
        melding.id = id

        if (cursor.moveToFirst()) {
            melding.locatie = cursor.getString(0)
            melding.datum = cursor.getLong(1)
            melding.droog = cursor.getInt(2) == 1
            melding.nat = cursor.getInt(3) == 1
            melding.temperatuur = cursor.getLong(4)
            melding.weerType = cursor.getLong(5)
            melding.windDir = cursor.getLong(6)
            melding.windSpeed = cursor.getLong(7)
            melding.land = cursor.getString(8)
        } else {
            melding.error = "Geen meldingen"
        }

        cursor.close()
        return melding
    }

    fun GetStatistieken(): ArrayList<Statistiek> {

        val selectQuery = ("SELECT"
                + " " + MDG_LOCATIE + ","
                + " SUM(" + MDG_DROOG + ") AS DROOG,"
                + " SUM(" + MDG_NAT + ") AS NAT"
                + " FROM " + TAB_MELDING
                + " GROUP BY " + MDG_LOCATIE
                + " ORDER BY " + MDG_LOCATIE)

        val db = this.writableDatabase
        val cursor: Cursor
        cursor = db.rawQuery(selectQuery, null)

        val stats = ArrayList<Statistiek>()

        var totaalAantalDroog = 0
        var totaalAantalNat = 0

        if (cursor.moveToFirst()) {
            do {
                val stat = Statistiek()
                stat.locatie = cursor.getString(0)
                stat.aantalDroog = cursor.getInt(1)
                totaalAantalDroog += cursor.getInt(1)
                stat.aantalNat = cursor.getInt(2)
                totaalAantalNat += cursor.getInt(2)
                stats.add(stat)
            } while (cursor.moveToNext())
        }
        cursor.close()

        val stat = Statistiek()
        stat.locatie = "Totaal"
        stat.aantalDroog = totaalAantalDroog
        stat.aantalNat = totaalAantalNat
        stats.add(stat)
        return stats
    }

    fun GetLaatste25Meldingen(): ArrayList<Melding> {

        val selectQuery = ("SELECT"
                + " " + MDG_LOCATIE + ","
                + " " + MDG_DATUM + ","
                + " " + MDG_DROOG + ","
                + " " + MDG_NAT + ","
                + " " + MDG_TEMPERATUUR + ","
                + " " + MDG_WEERTYPE + ","
                + " " + MDG_WINDDIR + ","
                + " " + MDG_WINDSPEED + ","
                + " " + MDG_LAND
                + " FROM " + TAB_MELDING
                + " ORDER BY " + MDG_DATUM + " DESC"
                + " LIMIT 25")

        val db = this.writableDatabase
        val cursor: Cursor
        cursor = db.rawQuery(selectQuery, null)

        val meldingen = ArrayList<Melding>()

        if (cursor.moveToFirst()) {
            do {
                val melding = Melding()
                melding.locatie = cursor.getString(0)
                melding.datum = cursor.getLong(1)
                melding.droog = cursor.getInt(2) == 1
                melding.nat = cursor.getInt(3) == 1
                melding.temperatuur = cursor.getLong(4)
                melding.weerType = cursor.getLong(5)
                melding.windDir = cursor.getLong(6)
                melding.windSpeed = cursor.getLong(7)
                melding.land = cursor.getString(8)
                meldingen.add(melding)
            } while (cursor.moveToNext())
        }
        cursor.close()

        return meldingen
    }

    fun GetMeldingen(id: String): ArrayList<Melding> {

        val selectQuery = ("SELECT"
                + " " + MDG_LOCATIE + ","
                + " " + MDG_DATUM + ","
                + " " + MDG_DROOG + ","
                + " " + MDG_NAT + ","
                + " " + MDG_TEMPERATUUR + ","
                + " " + MDG_WEERTYPE + ","
                + " " + MDG_WINDDIR + ","
                + " " + MDG_WINDSPEED + ","
                + " " + MDG_LAND
                + " FROM " + TAB_MELDING
                + " WHERE " + MDG_TELID + " = ?"
                + " ORDER BY " + MDG_DATUM + " DESC")

        val db = this.writableDatabase
        val cursor: Cursor
        cursor = db.rawQuery(selectQuery, arrayOf(id))

        val meldingen = ArrayList<Melding>()

        if (cursor.moveToFirst()) {
            do {
                val melding = Melding()
                melding.id = id
                melding.locatie = cursor.getString(0)
                melding.datum = cursor.getLong(1)
                melding.droog = cursor.getInt(2) == 1
                melding.nat = cursor.getInt(3) == 1
                melding.temperatuur = cursor.getLong(4)
                melding.weerType = cursor.getLong(5)
                melding.windDir = cursor.getLong(6)
                melding.windSpeed = cursor.getLong(7)
                melding.land = cursor.getString(8)
                meldingen.add(melding)
            } while (cursor.moveToNext())
        }
        cursor.close()

        return meldingen
    }

    fun GetStatistiek30Dagen(vanafDatum: DateTime): ArrayList<Statistiek1Dag> {

        val stats = ArrayList<Statistiek1Dag>()

        for (i in 0..29) {

            val selectQuery = ("SELECT"
                    + " SUM(" + MDG_DROOG + ") AS DROOG,"
                    + " SUM(" + MDG_NAT + ") AS NAT,"
                    + " MIN(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN 999 ELSE " + MDG_TEMPERATUUR + " END) AS MINTEMPERATUUR,"
                    + " MAX(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN -999 ELSE " + MDG_TEMPERATUUR + " END) AS MAXTEMPERATUUR"
                    + " FROM " + TAB_MELDING
                    + " WHERE " + MDG_DATUM + " BETWEEN ? AND ?")

            val db = this.writableDatabase
            val cursor: Cursor
            val va = DateTime(vanafDatum.year, vanafDatum.monthOfYear, vanafDatum.dayOfMonth, 0, 0).plusDays(i)
            val tm = va.plusDays(1)
            cursor = db.rawQuery(selectQuery, arrayOf(java.lang.Long.toString(va.millis), java.lang.Long.toString(tm.millis)))

            val stat = Statistiek1Dag()
            stat.datum = va

            if (cursor.moveToFirst()) {
                stat.aantalDroog = cursor.getInt(0)
                stat.aantalNat = cursor.getInt(1)
                stat.minTemperatuur = cursor.getInt(2)
                stat.maxTemperatuur = cursor.getInt(3)
            } else {
                stat.aantalDroog = 0
                stat.aantalNat = 0
                stat.minTemperatuur = 0
                stat.maxTemperatuur = 0
            }

            cursor.close()
            stats.add(stat)
        }

        return stats
    }

    fun GetAantalGebruikers30Dagen(vanafDatum: DateTime): ArrayList<StatistiekAantalGebruikers> {

        val stats = ArrayList<StatistiekAantalGebruikers>()
        for (i in 0..29) {
            val selectQuery = ("SELECT"
                    + " COUNT(DISTINCT " + MDG_TELID + ") AS AANTAL"
                    + " FROM " + TAB_MELDING
                    + " WHERE " + MDG_DATUM + " < ?")

            val db = this.writableDatabase
            val cursor: Cursor
            val tm = vanafDatum.plusDays(i)

            cursor = db.rawQuery(selectQuery, arrayOf(java.lang.Long.toString(tm.plusDays(1).millis)))
            val stat = StatistiekAantalGebruikers()
            stat.datum = tm

            if (cursor.moveToFirst())
                stat.aantalGebruikers = cursor.getInt(0)
            else
                stat.aantalGebruikers = 0

            cursor.close()
            stats.add(stat)
        }
        return stats
    }

    fun GetStatistiekLocatie(locatie: String): Statistiek1Plaats {

        val selectQuery: String
        val cursor: Cursor
        val db = this.writableDatabase

        if (locatie == "Totaal") {
            selectQuery = ("SELECT"
                    + " SUM(" + MDG_DROOG + ") AS DROOG,"
                    + " SUM(" + MDG_NAT + ") AS NAT,"
                    + " MIN(" + MDG_DATUM + ") AS MINDATUM,"
                    + " MAX(" + MDG_DATUM + ") AS MAXDATUM,"
                    + " SUM(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN 0 ELSE 1 END) AS AANTALTEMPERATUUR,"
                    + " SUM(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN 0 ELSE " + MDG_TEMPERATUUR + " END) AS SOMTEMPERATUUR"
                    + " FROM " + TAB_MELDING)
            cursor = db.rawQuery(selectQuery, null)
        } else {
            selectQuery = ("SELECT"
                    + " SUM(" + MDG_DROOG + ") AS DROOG,"
                    + " SUM(" + MDG_NAT + ") AS NAT,"
                    + " MIN(" + MDG_DATUM + ") AS MINDATUM,"
                    + " MAX(" + MDG_DATUM + ") AS MAXDATUM,"
                    + " SUM(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN 0 ELSE 1 END) AS AANTALTEMPERATUUR,"
                    + " SUM(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN 0 ELSE " + MDG_TEMPERATUUR + " END) AS SOMTEMPERATUUR"
                    + " FROM " + TAB_MELDING
                    + " WHERE " + MDG_LOCATIE + "=?")

            cursor = db.rawQuery(selectQuery, arrayOf(locatie))
        }

        val stat = Statistiek1Plaats()

        stat.locatie = locatie

        if (cursor.moveToFirst()) {
            stat.aantalDroog = cursor.getInt(0)
            stat.aantalNat = cursor.getInt(1)
            stat.datumStart = cursor.getLong(2)
            stat.datumEnd = cursor.getLong(3)
            stat.aantalTemperatuur = cursor.getInt(4)
            stat.somTemperatuur = cursor.getInt(5)
        }

        cursor.close()
        return stat
    }

    fun GetStatistiek12Maanden(jaar: Int, maand: Int): ArrayList<Statistiek1Maand> {

        val stats = ArrayList<Statistiek1Maand>()
        val vanaf: DateTime

        if (maand == 12)
            vanaf = DateTime(jaar, 1, 1, 0, 0)
        else
            vanaf = DateTime(jaar - 1, maand + 1, 1, 0, 0)

        for (i in 0..11) {

            val selectQuery = ("SELECT "
                    + " SUM(" + MDG_DROOG + ") AS DROOG,"
                    + " SUM(" + MDG_NAT + ") AS NAT,"
                    + " AVG(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN NULL ELSE " + MDG_TEMPERATUUR + " END) AS AVGTEMPERATUUR,"
                    + " MIN(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN 999 ELSE " + MDG_TEMPERATUUR + " END) AS MINTEMPERATUUR,"
                    + " MAX(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN -999 ELSE " + MDG_TEMPERATUUR + " END) AS MAXTEMPERATUUR"
                    + " FROM " + TAB_MELDING
                    + " WHERE " + MDG_DATUM + " BETWEEN ? AND ?")

            val db = this.writableDatabase
            val cursor: Cursor
            val va = vanaf.plusMonths(i)
            val tm = va.plusMonths(1)
            cursor = db.rawQuery(selectQuery, arrayOf(java.lang.Long.toString(va.millis), java.lang.Long.toString(tm.millis)))

            val stat = Statistiek1Maand()
            stat.maand = va.monthOfYear

            if (cursor.moveToFirst()) {
                stat.aantalDroog = cursor.getInt(0)
                stat.aantalNat = cursor.getInt(1)
                stat.avgTemperatuur = cursor.getFloat(2)
                stat.minTemperatuur = cursor.getFloat(3)
                stat.maxTemperatuur = cursor.getFloat(4)
            } else {
                stat.aantalDroog = 0
                stat.aantalNat = 0
                stat.avgTemperatuur = 0f
                stat.minTemperatuur = 0f
                stat.maxTemperatuur = 0f
            }

            cursor.close()
            stats.add(stat)
        }
        return stats
    }

    fun GetStatistiek24Uur(ajm: Helper.Periode, jaar: Int, maand: Int): ArrayList<Statistiek1Uur> {

        val selectQuery = ("SELECT"
                + " ((3600000 + " + MDG_DATUM + ") / 3600000) % 24 AS UUR,"
                + " SUM(" + MDG_DROOG + ") AS DROOG,"
                + " SUM(" + MDG_NAT + ") AS NAT"
                + " FROM " + TAB_MELDING
                + " WHERE " + MDG_DATUM + " BETWEEN ? AND ?"
                + " GROUP BY ((3600000 + " + MDG_DATUM + ") / 3600000) % 24"
                + " ORDER BY UUR")

        val db = this.writableDatabase
        val cursor: Cursor
        var va: DateTime
        var tm: DateTime

        va = DateTime(2015, 12, 1, 0, 0)
        tm = DateTime.now().plusDays(1)

        if (ajm === Helper.Periode.Jaar) {
            va = DateTime(jaar, 1, 1, 0, 0)
            tm = va.plusYears(1)
        }

        if (ajm === Helper.Periode.Maand) {
            va = DateTime(jaar, maand, 1, 0, 0)
            tm = va.plusMonths(1)
        }

        cursor = db.rawQuery(selectQuery, arrayOf(java.lang.Long.toString(va.millis), java.lang.Long.toString(tm.millis)))

        val stats = ArrayList<Statistiek1Uur>()
        // Zomertijd correctie
        val zone = DateTimeZone.getDefault()
        val standardOffset = zone.isStandardOffset(DateTime.now().millis)
        val zomerUur = if (standardOffset) 0 else 1

        if (cursor.moveToFirst()) {
            do {
                val stat = Statistiek1Uur()
                val uur = cursor.getInt(0) + zomerUur
                stat.uur = if (uur >= 24) 0 else uur
                stat.aantalDroog = cursor.getInt(1)
                stat.aantalNat = cursor.getInt(2)
                stats.add(stat)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return stats
    }

    fun GetStatistiekDagVdWeek(ajm: Helper.Periode, jaar: Int, maand: Int): ArrayList<StatistiekDagVdWeek> {

        // 1 jan 1970 is een donderdag (ma=0, di=1, wo=2, do=3 etc), daarom 3 x 86400000 erbij

        val selectQuery = ("SELECT"
                + " ((259200000 + " + MDG_DATUM + ") / 86400000) % 7 AS DAG,"
                + " SUM(" + MDG_DROOG + ") AS DROOG,"
                + " SUM(" + MDG_NAT + ") AS NAT"
                + " FROM " + TAB_MELDING
                + " WHERE " + MDG_DATUM + " BETWEEN ? AND ?"
                + " GROUP BY ((259200000 + " + MDG_DATUM + ") / 86400000) % 7"
                + " ORDER BY DAG")

        val db = this.writableDatabase
        val cursor: Cursor
        var va: DateTime
        var tm: DateTime

        va = DateTime(2015, 12, 1, 0, 0)
        tm = DateTime.now().plusDays(1)

        if (ajm === Helper.Periode.Jaar) {
            va = DateTime(jaar, 1, 1, 0, 0)
            tm = va.plusYears(1)
        }

        if (ajm === Helper.Periode.Maand) {
            va = DateTime(jaar, maand, 1, 0, 0)
            tm = va.plusMonths(1)
        }

        cursor = db.rawQuery(selectQuery, arrayOf(java.lang.Long.toString(va.millis), java.lang.Long.toString(tm.millis)))

        val stats = ArrayList<StatistiekDagVdWeek>()

        if (cursor.moveToFirst()) {
            do {
                val stat = StatistiekDagVdWeek()
                stat.dag = cursor.getInt(0)
                stat.aantalDroog = cursor.getInt(1)
                stat.aantalNat = cursor.getInt(2)
                stats.add(stat)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return stats
    }

    fun GetStatistiekWeerType(ajm: Helper.Periode, jaar: Int, maand: Int): ArrayList<StatistiekWeertype> {

        val selectQuery = ("SELECT"
                + " " + MDG_WEERTYPE + ","
                + " COUNT(*) AS AANTAL"
                + " FROM " + TAB_MELDING
                + " WHERE " + MDG_WEERTYPE + " > 0 "
                + " AND " + MDG_DATUM + " BETWEEN ? AND ?"
                + " GROUP BY " + MDG_WEERTYPE
                + " ORDER BY AANTAL DESC")

        val db = this.writableDatabase
        val cursor: Cursor
        var va: DateTime
        var tm: DateTime

        va = DateTime(2015, 12, 1, 0, 0)
        tm = DateTime.now().plusDays(1)

        if (ajm === Helper.Periode.Jaar) {
            va = DateTime(jaar, 1, 1, 0, 0)
            tm = va.plusYears(1)
        }

        if (ajm === Helper.Periode.Maand) {
            va = DateTime(jaar, maand, 1, 0, 0)
            tm = va.plusMonths(1)
        }

        cursor = db.rawQuery(selectQuery, arrayOf(java.lang.Long.toString(va.millis), java.lang.Long.toString(tm.millis)))

        val stats = ArrayList<StatistiekWeertype>()

        var totaal = 0
        if (cursor.moveToFirst()) {
            do {
                val stat = StatistiekWeertype()
                val weerType = WeerHelper.WeerType.valueOf(cursor.getLong(0))
                stat.weerType = weerType
                stat.aantal = cursor.getInt(1)
                stat.weerTypeOmschrijving = WeerHelper.weerTypeToWeerOmschrijving(weerType)
                stats.add(stat)
                totaal += stat.aantal
            } while (cursor.moveToNext())
        }
        cursor.close()
        if (stats.size == 0) return stats

        for (i in stats.indices) {
            val percentage = 100.0f * stats[i].aantal / totaal
            stats[i].percentage = percentage
        }

        return stats
    }


    private fun WindStatAddZeros(windstat: ArrayList<StatistiekWind>): ArrayList<StatistiekWind> {
        for (windDir in WeerHelper.WindDirection.values()) {
            var gevonden = false
            for (i in windstat.indices) {
                if (windDir == windstat[i].windDir) {
                    gevonden = true
                    break
                }
            }

            if (!gevonden && windDir != WeerHelper.WindDirection.Onbekend) {
                val stat = StatistiekWind()
                stat.windDir = windDir
                stat.windOmschrijving = WeerHelper.windDirectionToOmschrijving(windDir)
                stat.aantal = 0
                stat.avgWindSpeed = 0f
                stat.maxWindSpeed = 0f
                stat.percentage = 0f
                windstat.add(stat)
            }
        }

        return windstat
    }

    fun GetStatistiekWind(ajm: Helper.Periode, jaar: Int, maand: Int): ArrayList<StatistiekWind> {

        val selectQuery = ("SELECT"
                + " " + MDG_WINDDIR + ","
                + " COUNT(*) AS AANTAL,"
                + " AVG(" + MDG_WINDSPEED + ") AS AVGWINDSPEED,"
                + " MIN(" + MDG_WINDSPEED + ") AS MINWINDSPEED,"
                + " MAX(" + MDG_WINDSPEED + ") AS MAXWINDSPEED "
                + " FROM " + TAB_MELDING
                + " WHERE " + MDG_WINDDIR + " > 0 "
                + " AND " + MDG_DATUM + " BETWEEN ? AND ?"
                + " GROUP BY " + MDG_WINDDIR)

        val db = this.writableDatabase
        val cursor: Cursor
        var va: DateTime
        var tm: DateTime

        va = DateTime(2015, 12, 1, 0, 0)
        tm = DateTime.now().plusDays(1)

        if (ajm === Helper.Periode.Jaar) {
            va = DateTime(jaar, 1, 1, 0, 0)
            tm = va.plusYears(1)
        }

        if (ajm === Helper.Periode.Maand) {
            va = DateTime(jaar, maand, 1, 0, 0)
            tm = va.plusMonths(1)
        }

        cursor = db.rawQuery(selectQuery, arrayOf(java.lang.Long.toString(va.millis), java.lang.Long.toString(tm.millis)))

        var stats = ArrayList<StatistiekWind>()

        var totaal = 0
        if (cursor.moveToFirst()) {
            do {
                val stat = StatistiekWind()
                val windDir = WeerHelper.WindDirection.valueOf(cursor.getLong(0))
                stat.windDir = windDir
                stat.aantal = cursor.getInt(1)
                stat.windOmschrijving = WeerHelper.windDirectionToOmschrijving(windDir)
                stat.avgWindSpeed = cursor.getFloat(2)
                stat.minWindSpeed = cursor.getFloat(3)
                stat.maxWindSpeed = cursor.getFloat(4)
                stats.add(stat)
                totaal += stat.aantal
            } while (cursor.moveToNext())
        }
        cursor.close()

        for (i in stats.indices) {
            val percentage = 100.0f * stats[i].aantal / totaal
            stats[i].percentage = percentage
        }

        stats = WindStatAddZeros(stats)
        return stats
    }

    fun GetStatistiekRecords(aj: Helper.Periode, jaar: Int): StatistiekRecords {

        val stat = StatistiekRecords()
        val db = this.writableDatabase
        val cursor: Cursor

        val selectQuery = ("SELECT"
                + " " + MDG_DATUM + ","
                + " " + MDG_NAT + ","
                + " " + MDG_DROOG + ","
                + " " + MDG_TEMPERATUUR + ","
                + " " + MDG_WINDSPEED + ","
                + " " + MDG_WINDDIR + ","
                + " " + MDG_LOCATIE
                + " FROM " + TAB_MELDING
                + " WHERE " + MDG_DATUM + " BETWEEN ? AND ?"
                + " ORDER BY " + MDG_DATUM)

        var va: DateTime
        var tm: DateTime

        va = DateTime(2015, 1, 1, 0, 0)
        tm = DateTime.now().plusDays(1)

        if (aj === nl.whitedove.washetdroogofniet.Helper.Periode.Jaar) {
            va = DateTime(jaar, 1, 1, 0, 0)
            tm = va.plusYears(1)
        }

        cursor = db.rawQuery(selectQuery, arrayOf(java.lang.Long.toString(va.millis), java.lang.Long.toString(tm.millis)))

        var recordWindDatum = DateTime(2015, 1, 1, 0, 0)
        var recordWind = 0
        var recordWindRichting: WeerHelper.WindDirection = WeerHelper.WindDirection.Zuid
        var recordMaxWindLocatie = ""

        var recordMinTempDatum = DateTime(2015, 1, 1, 0, 0)
        var recordMinTemp = 0
        var recordMinTempLocatie = ""

        var recordMaxTempDatum = DateTime(2015, 1, 1, 0, 0)
        var recordMaxTemp = 0
        var recordMaxTempLocatie = ""

        var recordDroogJM = DateTime(2015, 1, 1, 0, 0)
        var recordNatJM = DateTime(2015, 1, 1, 0, 0)
        var percRecordDroog = 0f
        var percRecordNat = 0f

        var totaalInMaand = 0
        var totaalDroogInMaand = 0
        var totaalNatInMaand = 0

        var langsteDroogVanaf = DateTime(2015, 1, 1, 0, 0)
        var langsteDroogTm = DateTime(2015, 1, 1, 0, 0)
        var langsteNatVanaf = DateTime(2015, 1, 1, 0, 0)
        var langsteNatTm = DateTime(2015, 1, 1, 0, 0)

        var maxVerschilNat = 0L
        var maxVerschilDroog = 0L

        var vorigeDatum = DateTime(2015, 1, 1, 0, 0)
        var vorigeDatumNat = DateTime(2015, 1, 1, 0, 0)
        var vorigeDatumDroog = DateTime(2015, 1, 1, 0, 0)
        var laatsteDatumNat = DateTime(2015, 1, 1, 0, 0)
        var vorigeDroog = false
        var recordCorrectie = false

        if (cursor.moveToFirst()) {
            do {
                // Haal waardes uit cursor
                val datumTemp = DateTime(cursor.getLong(0))
                val datum = DateTime(datumTemp.year, datumTemp.monthOfYear, datumTemp.dayOfMonth,0,0)
                val nat = cursor.getInt(1) == 1
                val droog = cursor.getInt(2) == 1
                val temperatuur = cursor.getInt(3)
                val windspeed = cursor.getInt(4)
                val windrichting = WeerHelper.WindDirection.valueOf(cursor.getLong(5))
                val locatie = cursor.getString(6)

                if (windspeed != 999 && windspeed > recordWind) {
                    recordWind = windspeed
                    recordWindDatum = datum
                    recordWindRichting = windrichting
                    recordMaxWindLocatie = locatie
                }

                if (temperatuur != 999 && temperatuur < recordMinTemp) {
                    recordMinTemp = temperatuur
                    recordMinTempDatum = datum
                    recordMinTempLocatie = locatie
                }

                if (temperatuur != 999 && temperatuur > recordMaxTemp) {
                    recordMaxTemp = temperatuur
                    recordMaxTempDatum = datum
                    recordMaxTempLocatie = locatie
                }

                // Bereken maand totalen en droogste en natste maand
                totaalInMaand++

                // Controleer nieuwe maand
                if (datum.year != vorigeDatum.year || datum.monthOfYear != vorigeDatum.monthOfYear) {

                    val percDroog = 100f * totaalDroogInMaand / totaalInMaand
                    if (percDroog > percRecordDroog) {
                        // Record droog
                        percRecordDroog = percDroog
                        recordDroogJM = vorigeDatum
                    }

                    val percNat = 100f * totaalNatInMaand / totaalInMaand
                    if (percNat > percRecordNat) {
                        // Record nat
                        percRecordNat = percNat
                        recordNatJM = vorigeDatum
                    }

                    // Reset de tellers
                    totaalInMaand = 1
                    totaalDroogInMaand = if (droog) 1 else 0
                    totaalNatInMaand = if (nat) 1 else 0

                } else {
                    // Zelfde maand, blijf tellen
                    totaalDroogInMaand += if (droog) 1 else 0
                    totaalNatInMaand += if (nat) 1 else 0
                }

                if (vorigeDatumNat.year == 2015)
                    vorigeDatumNat = datum
                if (vorigeDatumDroog.year == 2015)
                    vorigeDatumDroog = datum

                // Bereken langste natte en droge periode
                val verschilDroog = datum.millis - vorigeDatumDroog.millis
                val verschilNat = datum.millis - vorigeDatumNat.millis

                if (droog) {
                    if (verschilDroog > maxVerschilDroog) {
                        langsteDroogVanaf = vorigeDatumDroog
                        langsteDroogTm = datum
                        maxVerschilDroog = verschilDroog
                        recordCorrectie = false
                    }
                } else {
                    vorigeDatumDroog = datum.plusDays(1)
                }

                if (nat && vorigeDroog && !recordCorrectie)
                {
                    langsteDroogTm=datum
                    recordCorrectie = true
                }

                // Voor de langste natte periode geldt dat de vorige natte dag minder dan 1 dag geleden moet zijn
                if (datum.minusDays(1).minusSeconds(1).isBefore(laatsteDatumNat)) {
                    if (verschilNat > maxVerschilNat) {
                        langsteNatVanaf = vorigeDatumNat
                        langsteNatTm = datum
                        maxVerschilNat = verschilNat
                    }
                } else {
                    vorigeDatumNat = datum
                }

                // Bewaar gegevens van de vorige rij
                vorigeDatum = datum
                vorigeDroog = droog
                if (nat) laatsteDatumNat = datum

            } while (cursor.moveToNext())

        }
        cursor.close()

        // Zet resultaten
        stat.minTemp = recordMinTemp
        stat.minTempDatum = recordMinTempDatum
        stat.minTempLocatie = recordMinTempLocatie

        stat.maxTemp = recordMaxTemp
        stat.maxTempDatum = recordMaxTempDatum
        stat.maxTempLocatie = recordMaxTempLocatie

        stat.maxWind = recordWind
        stat.maxWindDatum = recordWindDatum
        stat.maxWindRichting = recordWindRichting
        stat.maxWindLocatie = recordMaxWindLocatie

        stat.droogsteMaand = recordDroogJM
        stat.percentDroog = percRecordDroog

        stat.natsteMaand = recordNatJM
        stat.percentNat = percRecordNat

        stat.langstePeriodeNatVanaf = langsteNatVanaf
        stat.langstePeriodeNatTm = langsteNatTm.minusDays(1)
        stat.langstePeriodeDroogVanaf = langsteDroogVanaf
        stat.langstePeriodeDroogTm = langsteDroogTm.minusDays(1)

        return stat
    }

    companion object {

        private const val DATABASE_VERSION = 5
        private const val DATABASE_NAME = "whdon"

        private const val TAB_MELDING = "Melding"
        private const val MDG_ID = "Id"
        private const val MDG_TELID = "TelId"
        private const val MDG_LOCATIE = "Locatie"
        private const val MDG_LAND = "Land"
        private const val MDG_DATUM = "Datum"
        private const val MDG_DROOG = "Droog"
        private const val MDG_NAT = "Nat"
        private const val MDG_TEMPERATUUR = "Temperatuur"
        private const val MDG_WEERTYPE = "Weertype"
        private const val MDG_WINDSPEED = "Windspeed"
        private const val MDG_WINDDIR = "Winddir"

        private var sInstance: DatabaseHelper? = null

        @Synchronized
        fun getInstance(context: Context): DatabaseHelper {
            if (sInstance == null) {
                sInstance = DatabaseHelper(context.applicationContext)
            }
            return sInstance as DatabaseHelper
        }
    }
}

