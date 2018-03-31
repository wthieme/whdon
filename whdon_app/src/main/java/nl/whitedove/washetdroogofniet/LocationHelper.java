package nl.whitedove.washetdroogofniet;

import android.content.Context;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

class LocationHelper {

    enum LocationType {Unknown, Gps, Net}

    static void BepaalLocatie(Context cxt) {

        if (!Helper.TestInternet(cxt)) {
            return;
        }

        if (Helper.mCurrentBestLocation == null) {
            Helper.ShowMessage(cxt, "Locatie kan niet bepaald worden. Staan de locatie services aan?");
            Helper.mLocatie = "Onbekend";
            return;
        }

        Double lat = Helper.mCurrentBestLocation.getLatitude();
        Double lng = Helper.mCurrentBestLocation.getLongitude();
        Geocoder geocoder = new Geocoder(cxt, new Locale("nl_NL"));

        List<Address> list;
        try {
            list = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            Helper.ShowMessage(cxt, "Onverwachte fout bij ophalen locatie");
            return;
        }

        if (list != null && list.size() > 0) {
            Address address = list.get(0);
            Helper.mLocatie = address.getLocality();
            Helper.mCountry = address.getCountryCode();
        } else {
            Helper.ShowMessage(cxt, "Locatie kon niet bepaald worden, adres is leeg");
        }
    }

    static String GetLocatieVoorWeer() {
        String locatie = Helper.mLocatie;
        if (locatie == null || locatie.equalsIgnoreCase("Onbekend")) {
            locatie = "Nederland";
        }
        return locatie;
    }

    static String GetCountryVoorWeer() {
        String country = Helper.mCountry;
        if (country == null || country.equalsIgnoreCase("Onbekend")) {
            country = "NL";
        }
        return country;
    }

    static LatLng BepaalLatLng(Context cxt) {

        if (Helper.mCurrentBestLocation == null) {
            Helper.ShowMessage(cxt, "Locatie kan niet bepaald worden. Staan de locatie services aan?");
            return null;
        }

        Double lat = Helper.mCurrentBestLocation.getLatitude();
        Double lng = Helper.mCurrentBestLocation.getLongitude();
        return new LatLng(lat, lng);
    }

    static LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 1);
            if (address == null || address.size() == 0) {
                address = coder.getFromLocationName("Centrum " + strAddress, 1);
                if (address == null || address.size() == 0) {
                    return null;
                }
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (Exception ignored) {
        }
        return p1;

    }

    static void ToonHuidigeLocatie(Context context, TextView tvlocatie, LocationHelper.LocationType srt) {
        String loc;
        if (Helper.mLocatie != null) {
            loc = Helper.mLocatie;

            if (Helper.mCountry != null)
                loc = loc + "," + Helper.mCountry;

            Typeface iconFont = FontManager.GetTypeface(context, FontManager.FONTAWESOME_SOLID);

            String icon = "";
            if (srt == LocationHelper.LocationType.Gps)
                icon = context.getString(R.string.fa_map_marker);

            if (srt == LocationHelper.LocationType.Net)
                icon = context.getString(R.string.fa_signal);

            FontManager.SetIconAndText(tvlocatie,
                    iconFont,
                    icon,
                    ContextCompat.getColor(context, R.color.colorPrimary),
                    Typeface.DEFAULT,
                    loc,
                    ContextCompat.getColor(context, R.color.colorPrimary));
        }
    }


}
