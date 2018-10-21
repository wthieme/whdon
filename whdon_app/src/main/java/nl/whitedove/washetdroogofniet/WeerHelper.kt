package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.support.v4.content.ContextCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.joda.time.DateTime
import org.joda.time.Minutes
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

object WeerHelper {

    private var HuidigeTemperatuur = 999
    var huidigeWeertype = WeerType.Onbekend
    var huidigeWindSpeed = 999
    var huidigeWindDir = WindDirection.Onbekend

    enum class WeerType(val value: Long) {
        Onbekend(0L), Zonnig(1L), Halfbewolkt(2L), Bewolkt(3L), Regen(9L), Buien(10L), Onweer(11L), Sneeuw(13L), Mist(50L);


        companion object {
            @SuppressLint("UseSparseArrays")
            private val map = HashMap<Long, WeerType>()

            init {
                for (weerType in WeerType.values()) {
                    map[weerType.value] = weerType
                }
            }

            fun valueOf(weerType: Long): WeerType {
                return map[weerType] ?: Onbekend
            }
        }
    }

    enum class WindDirection(val value: Long) {
        Onbekend(0L), Noord(1L), NoordOost(2L), Oost(3L), ZuidOost(4L), Zuid(5L), ZuidWest(6L), West(7L), NoordWest(8L);

        companion object {
            @SuppressLint("UseSparseArrays")
            private val map = HashMap<Long, WindDirection>()

            init {
                for (windDirection in WindDirection.values()) {
                    map[windDirection.value] = windDirection
                }
            }

            fun valueOf(windDirection: Long): WindDirection {
                return map[windDirection] ?: Onbekend
            }
        }
    }

    private fun getweatherData(): String? {

        if (Helper.mCurrentBestLocation == null) return null

        val lat = Helper.mCurrentBestLocation!!.latitude
        val lon = Helper.mCurrentBestLocation!!.longitude
        val sLat = String.format(Locale.ROOT, "%.6f", lat)
        val sLon = String.format(Locale.ROOT, "%.6f", lon)

        val builder = Uri.Builder()
        builder.scheme("http")
                .authority("api.openweathermap.org")
                .appendPath("data")
                .appendPath("2.5")
                .appendPath("weather")
                .appendQueryParameter("lat", sLat)
                .appendQueryParameter("lon", sLon)
                .appendQueryParameter("appid", "e246fa149696b433128c8e774219bbc8")
                .appendQueryParameter("cnt", "1")
                .appendQueryParameter("units", "metric")

        val url = builder.build().toString()
        Helper.l("weather url:$url")

        val client = OkHttpClient()
        val request = Request.Builder()
                .addHeader("Cache-Control", "no-cache")
                .url(url)
                .build()

        var weatherData: String? = null
        val response: Response
        try {
            response = client.newCall(request).execute()
            if (response.isSuccessful)
                weatherData = response.body()!!.string()
        } catch (ignored: IOException) {
        }

        return weatherData
    }

    @Throws(JSONException::class)
    fun bepaalWeer(): Weer? {

        val weatherJson = WeerHelper.getweatherData()
        if (weatherJson == null || weatherJson.isEmpty()) {
            return null
        }

        val jObj = JSONObject(weatherJson)
        val jArrWeather = jObj.getJSONArray("weather")
        val main = jObj.getJSONObject("main")
        val weather = jArrWeather.getJSONObject(0)
        val wind = jObj.getJSONObject("wind")

        val result = Weer()
        var plaats = jObj.getString("name")
        plaats = plaats.replace("Gemeente", "")
        result.plaats = plaats
        if (weather.has("icon")) result.icon = weather.getString("icon")
        if (main.has("temp")) result.graden = Math.round(main.getDouble("temp")).toInt()
        result.wind = if (wind.has("speed")) Math.round(3.6f * wind.getDouble("speed")).toInt() else 0
        val windRichting = if (wind.has("deg")) Math.round(wind.getDouble("deg")).toInt() else -1
        result.setWindDir(windRichting)
        return result
    }

    fun bepaalBrDataTxt(cxt: Context, buienData: BuienData?): String {
        return if (buienData == null || buienData.noData!!) cxt.getString(R.string.NoBrData) else cxt.getString(R.string.DroogTxt)
    }

    fun bepaalBuien(): BuienData {

        val result = BuienData()
        var brString: String? = null

        if (Helper.mCurrentBestLocation != null) {

            val lat = Helper.mCurrentBestLocation!!.latitude
            val lon = Helper.mCurrentBestLocation!!.longitude
            val sLat = String.format(Locale.ROOT, "%.6f", lat)
            val sLon = String.format(Locale.ROOT, "%.6f", lon)

            brString = WeerHelper.getBrData(sLat, sLon)
        }

        if (brString == null || brString.isEmpty()) {
            result.noData = true
            val brData = ArrayList<RegenEntry>()
            // Add some fake data
            for (i in 0..9) {
                brData.add(RegenEntry("", 0))
            }
            result.regenData = brData
            return result
        }

        result.noData = false

        val regenData = ArrayList<RegenEntry>()
        val parts = brString.split("\r\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (entry in parts) {

            val tr = entry.split(Pattern.quote("|").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val re = RegenEntry()

            if (Helper.tryParseInt(tr[0]))
                re.regen = Integer.parseInt(tr[0])
            else
                re.regen = 0
            re.tijd = tr[1]
            regenData.add(re)
        }

        result.regenData = regenData
        return result
    }

    private fun getBrData(lat: String, lon: String): String? {

        val builder = Uri.Builder()
        builder.scheme("http")
                .authority("gadgets.buienradar.nl")
                .appendPath("data")
                .appendPath("raintext")
                .appendQueryParameter("lat", lat)
                .appendQueryParameter("lon", lon)

        val url = builder.build().toString()
        Helper.l("Buienradar url:$url")

        val client = OkHttpClient()
        val request = Request.Builder()
                .addHeader("Cache-Control", "no-cache")
                .url(url)
                .build()

        var brData: String? = null
        val response: Response
        try {
            response = client.newCall(request).execute()
            if (response.isSuccessful)

                brData = response.body()!!.string()
        } catch (ignored: IOException) {
        }

        return brData
    }

    fun berekenNuXPositie(buienData: BuienData?): Float {
        if (buienData == null || buienData.noData!!) return 0f
        val nu = DateTime.now()
        val eersteEntry = buienData.regenData!![0]
        val tijd = eersteEntry.tijd
        val sUur = tijd.substring(0, 2)
        val sMin = tijd.substring(3)
        val iUur = Integer.parseInt(sUur)
        val iMin = Integer.parseInt(sMin)
        var first = DateTime(nu.year, nu.monthOfYear, nu.dayOfMonth, iUur, iMin)
        if (first.isAfter(nu)) first = first.minusDays(1)
        val minuten = Minutes.minutesBetween(first, nu).minutes
        return 24f * (minuten / 120f)
    }

    fun getHuidigeTemperatuur(): Int {
        return HuidigeTemperatuur
    }

    fun setHuidigeTemperatuur(temperatuur: Int) {
        HuidigeTemperatuur = temperatuur
    }

    @SuppressLint("DefaultLocale")
    fun weerTypeToWeerIcoon(weerType: WeerType): String? {
        return if (weerType == WeerType.Onbekend) {
            null
        } else {
            String.format("i%02dd", weerType.value)
        }
    }

    fun weerIcoonToWeerType(weerIcoon: String): WeerType {
        when (weerIcoon) {
            "01d", "01n" -> return WeerType.Zonnig
            "02d", "02n" -> return WeerType.Halfbewolkt
            "03d", "03n", "04d", "04n" -> return WeerType.Bewolkt
            "09d", "09n" -> return WeerType.Regen
            "10d", "10n" -> return WeerType.Buien
            "11d", "11n" -> return WeerType.Onweer
            "13d", "13n" -> return WeerType.Sneeuw
            "50d", "50n" -> return WeerType.Mist
        }
        return WeerType.Onbekend
    }

    fun weerTypeToWeerOmschrijving(weerType: WeerType): String {
        return when (weerType) {
            WeerHelper.WeerType.Onbekend -> ""
            WeerHelper.WeerType.Zonnig -> "Zonnig"
            WeerHelper
                    .WeerType.Halfbewolkt -> "Halfbewolkt"
            WeerHelper.WeerType.Bewolkt -> "Bewolkt"
            WeerHelper.WeerType.Buien -> "Buien"
            WeerHelper.WeerType.Mist -> "Mist"
            WeerHelper.WeerType.Onweer -> "Onweer"
            WeerHelper.WeerType.Regen -> "Regen"
            WeerHelper.WeerType.Sneeuw -> "Sneeuw"
        }
    }

    fun weerTypeToWeerKleur(context: Context, weerType: WeerType): Int {
        return when (weerType) {
            WeerHelper.WeerType.Onbekend -> ContextCompat.getColor(context, R.color.colorPrimary)
            WeerHelper.WeerType.Zonnig -> ContextCompat.getColor(context, R.color.colorGrafiek5)
            WeerHelper.WeerType.Halfbewolkt -> ContextCompat.getColor(context, R.color.colorGrafiek4)
            WeerHelper.WeerType.Bewolkt -> ContextCompat.getColor(context, R.color.colorGrafiek3)
            WeerHelper.WeerType.Buien -> ContextCompat.getColor(context, R.color.colorGrafiek6)
            WeerHelper.WeerType.Mist -> ContextCompat.getColor(context, R.color.colorGrafiek7)
            WeerHelper.WeerType.Onweer -> ContextCompat.getColor(context, R.color.colorGrafiek2)
            WeerHelper.WeerType.Regen -> ContextCompat.getColor(context, R.color.colorGrafiek1)
            WeerHelper.WeerType.Sneeuw -> ContextCompat.getColor(context, R.color.colorGrafiek8)
        }
    }

    fun windDirectionToOmschrijving(windDir: WindDirection): String {
        return when (windDir) {
            WeerHelper.WindDirection.Onbekend -> ""
            WeerHelper.WindDirection.Noord -> "Noord"
            WeerHelper.WindDirection.NoordOost -> "Noordoost"
            WeerHelper.WindDirection.Oost -> "Oost"
            WeerHelper.WindDirection.ZuidOost -> "Zuidoost"
            WeerHelper.WindDirection.Zuid -> "Zuid"
            WeerHelper.WindDirection.ZuidWest -> "Zuidwest"
            WeerHelper.WindDirection.West -> "West"
            WeerHelper.WindDirection.NoordWest -> "Noordwest"
        }
    }
}
