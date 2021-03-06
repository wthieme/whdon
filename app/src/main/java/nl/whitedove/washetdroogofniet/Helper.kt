package nl.whitedove.washetdroogofniet

import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat
import android.widget.TextView
import android.widget.Toast
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

internal object Helper {

    var dFormat = DateTimeFormat.forPattern("dd-MM-yyyy").withLocale(Locale.getDefault())!!
    var dmFormat = DateTimeFormat.forPattern("dd-MM").withLocale(Locale.getDefault())!!
    var dtFormat = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm").withLocale(Locale.getDefault())!!
    var mCurrentBestLocation: Location? = null
    var mLocatie: String? = null
    var mCountry: String? = null
    const val ONE_MINUTE = 1000 * 60
    const val ONE_KM = 1000
    const val DEBUG = false
    const val ZOOM = 9.0f
    const val ZOOM1Plaats = 7.0f

    internal enum class Periode {
        Alles, Jaar, Maand
    }

    internal enum class MapsDisplay {
        Standaard, Satelliet
    }

    fun l(log: String) {
        if (Helper.DEBUG) {
            println(log)
        }
    }

    fun testInternet(ctx: Context): Boolean {
        val result: Boolean
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        result = netInfo != null && netInfo.isConnected
        if (!result) Helper.showMessage(ctx, "Geen internet connectie")
        return result
    }

    fun tryParseInt(value: String): Boolean {
        return try {
            Integer.parseInt(value)
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun getGuid(cxt: Context): String {
        if (Helper.DEBUG) {
            return "d81371b2-f958-4f80-abc6-66fed2c38713"
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(cxt)
        val editor = preferences.edit()

        var guid = preferences.getString("guid", "")
        if (guid!!.isEmpty()) {
            guid = UUID.randomUUID().toString()
            editor.putString("guid", guid)
            editor.apply()
        }
        return guid
    }

    fun showMessage(cxt: Context, melding: String) {
        Helper.l(melding)
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(cxt, melding, duration)
        toast.view.setBackgroundColor(ContextCompat.getColor(cxt, R.color.colorPrimary))
        val text = toast.view.findViewById(android.R.id.message) as TextView
        text.setTextColor(ContextCompat.getColor(cxt, R.color.colorAccent))
        toast.show()
    }

    fun getLastSyncDate(cxt: Context): DateTime {
        val preferences = PreferenceManager.getDefaultSharedPreferences(cxt)
        val dat = preferences.getLong("syncdate", DateTime(2000, 1, 1, 0, 0).millis)
        return DateTime(dat)
    }

    fun setLastSyncDate(cxt: Context, date: DateTime) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(cxt)
        val editor = preferences.edit()
        editor.putLong("syncdate", date.millis)
        editor.apply()
    }
}
