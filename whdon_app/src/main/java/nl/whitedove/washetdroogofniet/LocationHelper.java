package nl.whitedove.washetdroogofniet;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

class LocationHelper {

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
            LocationHelper.BewaarLocatie(cxt, Helper.mLocatie);
        } else {
            Helper.ShowMessage(cxt, "Locatie kon niet bepaald worden, adres is leeg");
        }
    }

    static String GetLocatieVoorWeer(Context cxt) {
        String locatie = Helper.mLocatie;

        if (locatie == null || locatie.equalsIgnoreCase("Onbekend")) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cxt);

            locatie = preferences.getString("locatie", "");
            if (locatie.isEmpty()) {
                locatie = "Nederland";
            }
        }
        return locatie;
    }

    static void BewaarLocatie(Context cxt, String locatie) {
        if (locatie == null || locatie.isEmpty() || locatie.equalsIgnoreCase("Onbekend") || locatie.equalsIgnoreCase("Nederland")) {
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("locatie", locatie);
        editor.apply();
    }
}
