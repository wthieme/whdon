package nl.whitedove.washetdroogofniet;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;
import java.util.UUID;

import nl.whitedove.washetdroogofniet.backend.whdonApi.WhdonApi;

class Helper {

    private static final String ApiUrl = "https://9-dot-washetdroogofnietbackend.appspot.com/_ah/api/";
    static WhdonApi myApiService = new WhdonApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
            .setRootUrl(Helper.ApiUrl)
            .build();

    static DateTimeFormatter dFormat = DateTimeFormat.forPattern("dd-MM-yyyy").withLocale(Locale.getDefault());
    static DateTimeFormatter dmFormat = DateTimeFormat.forPattern("dd-MM").withLocale(Locale.getDefault());
    static DateTimeFormatter dtFormat = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm").withLocale(Locale.getDefault());
    static Location mCurrentBestLocation;
    static String mLocatie;
    static final int ONE_MINUTE = 1000 * 60;
    static final int ONE_KM = 1000;
    static final boolean DEBUG = false;

    static void Log(String log) {
        if (Helper.DEBUG) {
            System.out.println(log);
        }
    }

    static Boolean TestInternet(Context ctx) {
        Boolean result;

        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            result = false;
        } else {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            result = netInfo != null && netInfo.isConnectedOrConnecting();
        }

        if (!result) {
            Helper.ShowMessage(ctx, "Geen internet connectie");
        }

        return result;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static String GetGuid(Context cxt) {
        if (Helper.DEBUG) {
            return "d81371b2-f958-4f80-abc6-66fed2c38713";
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        SharedPreferences.Editor editor = preferences.edit();

        String guid = preferences.getString("guid", "");
        if (guid.isEmpty()) {
            guid = UUID.randomUUID().toString();
            editor.putString("guid", guid);
            editor.apply();
        }
        return guid;
    }

    static void ShowMessage(Context cxt, String melding) {
        Helper.Log(melding);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(cxt, melding, duration);
        toast.show();
    }

    static DateTime GetLastSyncDate(Context cxt) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        Long dat = preferences.getLong("syncdate", new DateTime(2000, 1, 1, 0, 0).getMillis());
        return new DateTime(dat);
    }

    static void SetLastSyncDate(Context cxt, DateTime date) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(cxt);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("syncdate", date.getMillis());
        editor.apply();
    }
}
