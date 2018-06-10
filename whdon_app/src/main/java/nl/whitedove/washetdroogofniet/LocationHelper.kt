package nl.whitedove.washetdroogofniet

import android.content.Context
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.support.v4.content.ContextCompat
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.util.*

internal object LocationHelper {

    internal enum class LocationType {
        Unknown, Gps, Net
    }

    fun bepaalLocatie(cxt: Context) {

        if (!Helper.testInternet(cxt)!!) {
            return
        }

        if (Helper.mCurrentBestLocation == null) {
            Helper.showMessage(cxt, "Locatie kan niet bepaald worden. Staan de locatie services aan?")
            Helper.mLocatie = "Onbekend"
            return
        }

        val lat = Helper.mCurrentBestLocation!!.latitude
        val lng = Helper.mCurrentBestLocation!!.longitude
        val geocoder = Geocoder(cxt, Locale("nl_NL"))

        val list: List<Address>?
        try {
            list = geocoder.getFromLocation(lat, lng, 1)
        } catch (e: IOException) {
            Helper.showMessage(cxt, "Onverwachte fout bij ophalen locatie")
            return
        }

        if (list != null && list.isNotEmpty()) {
            val address = list[0]
            Helper.mLocatie = address.locality
            Helper.mCountry = address.countryCode
        } else {
            Helper.showMessage(cxt, "Locatie kon niet bepaald worden, adres is leeg")
        }
    }

    fun getLocatieVoorWeer(): String {
        var locatie: String? = Helper.mLocatie
        if (locatie == null || locatie.equals("Onbekend", ignoreCase = true)) {
            locatie = "Nederland"
        }
        return locatie
    }

    fun getCountryVoorWeer(): String {
        var country: String? = Helper.mCountry
        if (country == null || country.equals("Onbekend", ignoreCase = true)) {
            country = "NL"
        }
        return country
    }

    fun bepaalLatLng(cxt: Context): LatLng? {

        if (Helper.mCurrentBestLocation == null) {
            Helper.showMessage(cxt, "Locatie kan niet bepaald worden. Staan de locatie services aan?")
            return null
        }

        val lat = Helper.mCurrentBestLocation!!.latitude
        val lng = Helper.mCurrentBestLocation!!.longitude
        return LatLng(lat, lng)
    }

    fun getLocationFromAddress(context: Context, strAddress: String): LatLng? {
        val coder = Geocoder(context)
        var address: List<Address>?
        var p1: LatLng? = null

        try {
            address = coder.getFromLocationName(strAddress, 1)
            if (address == null || address.isEmpty()) {
                address = coder.getFromLocationName("Centrum $strAddress", 1)
                if (address == null || address.isEmpty()) {
                    return null
                }
            }
            val location = address[0]
            location.latitude
            location.longitude

            p1 = LatLng(location.latitude, location.longitude)
        } catch (ignored: Exception) {
        }

        return p1

    }

    fun toonHuidigeLocatie(context: Context, tvlocatie: TextView, srt: LocationHelper.LocationType) {
        var loc: String
        if (Helper.mLocatie != null) {
            loc = Helper.mLocatie!!

            if (Helper.mCountry != null)
                loc = loc + "," + Helper.mCountry

            val iconFont = FontManager.GetTypeface(context, FontManager.FONTAWESOME_SOLID)

            var icon = ""
            if (srt == LocationHelper.LocationType.Gps)
                icon = context.getString(R.string.fa_map_marker)

            if (srt == LocationHelper.LocationType.Net)
                icon = context.getString(R.string.fa_signal)

            FontManager.SetIconAndText(tvlocatie,
                    iconFont,
                    icon,
                    ContextCompat.getColor(context, R.color.colorPrimary),
                    Typeface.DEFAULT,
                    loc,
                    ContextCompat.getColor(context, R.color.colorPrimary))
        }
    }
}
