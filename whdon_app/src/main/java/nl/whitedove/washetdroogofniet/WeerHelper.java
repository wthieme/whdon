package nl.whitedove.washetdroogofniet;

import android.content.Context;
import android.net.Uri;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class WeerHelper {

    static Weer BepaalWeer() throws JSONException {

        String weatherJson = WeerHelper.getWeatherData();
        if (weatherJson == null || weatherJson.isEmpty()) {
            return null;
        }

        JSONObject jObj = new JSONObject(weatherJson);
        JSONArray jArrWeather = jObj.getJSONArray("weather");
        JSONObject main = jObj.getJSONObject("main");
        JSONObject weather = jArrWeather.getJSONObject(0);
        JSONObject wind = jObj.getJSONObject("wind");

        Weer result = new Weer();
        String plaats = jObj.getString("name");
        plaats = plaats.replace("Gemeente", "");
        result.setPlaats(plaats);
        if (weather.has("icon")) result.setIcon(weather.getString("icon"));
        if (main.has("temp")) result.setGraden((int) Math.round(main.getDouble("temp")));
        result.setWind(wind.has("speed") ? (int) Math.round(3.6f * wind.getDouble("speed")) : 0);
        result.setWindRichting(wind.has("deg") ? (int) Math.round(wind.getDouble("deg")) : -1);
        return result;
    }

    static String BepaalBrDataTxt(Context cxt, BuienData buienData) {
        if (buienData == null || buienData.getNoData()) return cxt.getString(R.string.NoBrData);
        return cxt.getString(R.string.DroogTxt);
    }

    static BuienData BepaalBuien() throws IOException {

        BuienData result = new BuienData();
        String brString = null;

        if (Helper.mCurrentBestLocation != null) {

            Double lat = Helper.mCurrentBestLocation.getLatitude();
            Double lon = Helper.mCurrentBestLocation.getLongitude();

            String sLat = String.format(Locale.ROOT, "%.6f", lat);
            String sLon = String.format(Locale.ROOT, "%.6f", lon);

            brString = WeerHelper.getBrData(sLat, sLon);
        }

        if (brString == null || brString.isEmpty()) {
            result.setNoData(true);
            ArrayList<RegenEntry> brData = new ArrayList<>();
            // Add some fake data
            for (int i = 0; i < 10; i++) {
                brData.add(new RegenEntry("", 0));
            }
            result.setRegenData(brData);
            return result;
        }

        result.setNoData(false);

        int i=0;
        //int j = 0;
        ArrayList<RegenEntry> regenData = new ArrayList<>();
        String[] parts = brString.split("\r\n");
        for (String entry : parts) {

            String[] tr = entry.split(Pattern.quote("|"));
            RegenEntry re = new RegenEntry();

            if (Helper.DEBUG) {
                Random rnd = new Random();
                i += rnd.nextInt(20) - 10;
            } else {
                i = 0;
            }

            if (Helper.tryParseInt(tr[0]))
                re.setRegen(i + Integer.parseInt(tr[0]));
            else
                re.setRegen(i);
            re.setTijd(tr[1]);
            regenData.add(re);
        }

        result.setRegenData(regenData);
        return result;
    }

    private static String getBrData(String lat, String lon) {

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("gadgets.buienradar.nl")
                .appendPath("data")
                .appendPath("raintext")
                .appendQueryParameter("lat", lat)
                .appendQueryParameter("lon", lon);

        String url = builder.build().toString();
        Helper.Log("Buienradar url:" + url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        String brData = null;
        Response response;
        try {
            response = client.newCall(request).execute();
            brData = response.body().string();
        } catch (IOException ignored) {
        }
        return brData;
    }

    private static String getWeatherData() {

        String locatie = LocationHelper.GetLocatieVoorWeer();

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("api.openweathermap.org")
                .appendPath("data")
                .appendPath("2.5")
                .appendPath("weather")
                .appendQueryParameter("q", String.format("%s,nl", locatie))
                .appendQueryParameter("appid", "e246fa149696b433128c8e774219bbc8")
                .appendQueryParameter("cnt", "1")
                .appendQueryParameter("units", "metric");

        String url = builder.build().toString();
        Helper.Log("weather url:" + url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        String weatherData = null;
        Response response;
        try {
            response = client.newCall(request).execute();
            weatherData = response.body().string();
        } catch (IOException ignored) {
        }
        return weatherData;
    }

    static float BerekenNuXPositie(BuienData buienData) {
        if (buienData == null || buienData.getNoData()) return 0f;
        DateTime nu = DateTime.now();
        RegenEntry eersteEntry = buienData.getRegenData().get(0);
        String tijd = eersteEntry.getTijd();
        String sUur = tijd.substring(0, 2);
        String sMin = tijd.substring(3);
        int iUur = Integer.parseInt(sUur);
        int iMin = Integer.parseInt(sMin);
        DateTime first = new DateTime(nu.getYear(), nu.getMonthOfYear(), nu.getDayOfMonth(), iUur, iMin);
        if (first.isAfter(nu)) first = first.minusDays(1);
        int minuten = Minutes.minutesBetween(first, nu).getMinutes();
        return 24f * (minuten / 120f);
    }
}
