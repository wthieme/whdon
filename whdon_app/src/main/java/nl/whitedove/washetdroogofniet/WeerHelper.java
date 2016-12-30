package nl.whitedove.washetdroogofniet;

import android.content.Context;
import android.net.Uri;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

class WeerHelper {

    static Weer BepaalWeer(Context cxt) throws JSONException {

        String weatherJson = WeerHelper.getWeatherData(cxt);
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
        result.setIcon(weather.getString("icon"));
        result.setGraden((int) Math.round(main.getDouble("temp")));
        result.setWind((int) Math.round(3.6f * wind.getDouble("speed")));
        result.setWindRichting((int) Math.round(wind.getDouble("deg")));
        return result;
    }

    static String BepaalBrDataTxt(Context cxt, BuienData buienData) {
        if (buienData == null || buienData.getNoData()) return cxt.getString(R.string.NoBrData);
        return cxt.getString(R.string.Droog);
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

        HttpURLConnection con = null;
        InputStream is = null;

        // String BrUrl = "http://gps.buienradar.nl/getrr.php?lat=%s&lon=%s";
        // http://gadgets.buienradar.nl/data/raintext?lat=51&lon=3.9

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("gadgets.buienradar.nl")
                .appendPath("data")
                .appendPath("raintext")
                .appendQueryParameter("lat", lat)
                .appendQueryParameter("lon", lon);

        String url = builder.build().toString();

        Helper.Log("Buienradar url:" + url);
        try {
            con = (HttpURLConnection) (new URL(url)).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();

            // Let's read the response
            StringBuilder buffer = new StringBuilder();
            is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null)
                buffer.append(line).append("\r\n");

            is.close();
            con.disconnect();
            return buffer.toString();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Throwable ignored) {
            }
            try {
                if (con != null) {
                    con.disconnect();
                }
            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    private static String getWeatherData(Context cxt) {

        HttpURLConnection con = null;
        InputStream is = null;

        String locatie = LocationHelper.GetLocatieVoorWeer(cxt);

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
        try {
            con = (HttpURLConnection) (new URL(url)).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();

            // Let's read the response
            StringBuilder buffer = new StringBuilder();
            is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null)
                buffer.append(line).append("\r\n");

            is.close();
            con.disconnect();
            return buffer.toString();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Throwable ignored) {
            }
            try {
                if (con != null) {
                    con.disconnect();
                }
            } catch (Throwable ignored) {
            }
        }

        return null;
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
