package nl.whitedove.washetdroogofniet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class WeerHelper {

    public enum WeerType {
        Onbekend(0L), Zonnig(1L), Halfbewolkt(2L), Bewolkt(3L), Regen(9L), Buien(10L), Onweer(11L), Sneeuw(13L), Mist(50L);

        private long value;
        @SuppressLint("UseSparseArrays")
        private static Map<Long, WeerType> map = new HashMap<>();

        WeerType(long value) {
            this.value = value;
        }

        static {
            for (WeerType weerType : WeerType.values()) {
                map.put(weerType.value, weerType);
            }
        }

        public static WeerType valueOf(long weerType) {
            return map.get(weerType);
        }

        public long getValue() {
            return value;
        }
    }

    public enum WindDirection {
        Onbekend(0L), Noord(1L), NoordOost(2L), Oost(3L), ZuidOost(4L), Zuid(5L), ZuidWest(6L), West(7L), NoordWest(8L);

        private long value;
        @SuppressLint("UseSparseArrays")
        private static Map<Long, WindDirection> map = new HashMap<>();

        WindDirection(long value) {
            this.value = value;
        }

        static {
            for (WindDirection windDirection : WindDirection.values()) {
                map.put(windDirection.value, windDirection);
            }
        }

        public static WindDirection valueOf(long windDirection) {
            return map.get(windDirection);
        }

        public long getValue() {
            return value;
        }
    }

    private static int HuidigeTemperatuur = 999;

    private static WeerType HuidigeWeertype = WeerType.Onbekend;

    private static int HuidigeWindSpeed = 999;

    private static WindDirection HuidigeWindDir = WindDirection.Onbekend;

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
        int windRichting = wind.has("deg") ? (int) Math.round(wind.getDouble("deg")) : -1;
        result.setWindDir(windRichting);
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

        int i = 0;
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
            if (response.isSuccessful()) //noinspection ConstantConditions
                brData = response.body().string();
        } catch (IOException ignored) {
        }
        return brData;
    }

    private static String getWeatherData() {

        String locatie = LocationHelper.GetLocatieVoorWeer();
        String country = LocationHelper.GetCountryVoorWeer();

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("api.openweathermap.org")
                .appendPath("data")
                .appendPath("2.5")
                .appendPath("weather")
                .appendQueryParameter("q", String.format("%s,%s", locatie, country))
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
            if (response.isSuccessful()) //noinspection ConstantConditions
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

    static int GetHuidigeTemperatuur() {
        return HuidigeTemperatuur;
    }

    static void SetHuidigeTemperatuur(int temperatuur) {
        HuidigeTemperatuur = temperatuur;
    }

    static WeerType getHuidigeWeertype() {
        return HuidigeWeertype;
    }

    static void setHuidigeWeertype(WeerType huidigeWeertype) {
        HuidigeWeertype = huidigeWeertype;
    }

    static int getHuidigeWindSpeed() {
        return HuidigeWindSpeed;
    }

    static void setHuidigeWindSpeed(int huidigeWindSpeed) {
        HuidigeWindSpeed = huidigeWindSpeed;
    }

    static WindDirection getHuidigeWindDir() {
        return HuidigeWindDir;
    }

    static void setHuidigeWindDir(WindDirection huidigeWindDir) {
        HuidigeWindDir = huidigeWindDir;
    }

    @SuppressLint("DefaultLocale")
    static String WeerTypeToWeerIcoon(WeerType weerType) {
        if (weerType == WeerType.Onbekend) {
            return null;
        } else {
            return String.format("i%02dd", weerType.getValue());
        }
    }

    static WeerType WeerIcoonToWeerType(String weerIcoon) {
        switch (weerIcoon) {
            case "01d":
            case "01n":
                return WeerType.Zonnig;
            case "02d":
            case "02n":
                return WeerType.Halfbewolkt;
            case "03d":
            case "03n":
            case "04d":
            case "04n":
                return WeerType.Bewolkt;
            case "09d":
            case "09n":
                return WeerType.Regen;
            case "10d":
            case "10n":
                return WeerType.Buien;
            case "11d":
            case "11n":
                return WeerType.Onweer;
            case "13d":
            case "13n":
                return WeerType.Sneeuw;
            case "50d":
            case "50n":
                return WeerType.Mist;
        }
        return WeerType.Onbekend;
    }

    static String WeerTypeToWeerOmschrijving(WeerType weerType) {
        switch (weerType) {
            case Onbekend:
                return null;
            case Zonnig:
                return "Zonnig";
            case Halfbewolkt:
                return "Halfbewolkt";
            case Bewolkt:
                return "Bewolkt";
            case Buien:
                return "Buien";
            case Mist:
                return "Mist";
            case Onweer:
                return "Onweer";
            case Regen:
                return "Regen";
            case Sneeuw:
                return "Sneeuw";
        }
        return null;
    }

    static Integer WeerTypeToWeerKleur(Context context, WeerType weerType) {
        switch (weerType) {
            case Onbekend:
                return ContextCompat.getColor(context, R.color.colorPrimary);
            case Zonnig:
                return ContextCompat.getColor(context, R.color.colorGrafiek5);
            case Halfbewolkt:
                return ContextCompat.getColor(context, R.color.colorGrafiek4);
            case Bewolkt:
                return ContextCompat.getColor(context, R.color.colorGrafiek3);
            case Buien:
                return ContextCompat.getColor(context, R.color.colorGrafiek6);
            case Mist:
                return ContextCompat.getColor(context, R.color.colorGrafiek7);
            case Onweer:
                return ContextCompat.getColor(context, R.color.colorGrafiek2);
            case Regen:
                return ContextCompat.getColor(context, R.color.colorGrafiek1);
            case Sneeuw:
                return ContextCompat.getColor(context, R.color.colorGrafiek8);
        }
        return null;
    }

    static String WindDirectionToOmschrijving(WindDirection windDir) {
        switch (windDir) {
            case Onbekend:
                return null;
            case Noord:
                return "Noord";
            case NoordOost:
                return "Noordoost";
            case Oost:
                return "Oost";
            case ZuidOost:
                return "Zuidoost";
            case Zuid:
                return "Zuid";
            case ZuidWest:
                return "Zuidwest";
            case West:
                return "West";
            case NoordWest:
                return "Noordwest";
        }
        return null;
    }

}
