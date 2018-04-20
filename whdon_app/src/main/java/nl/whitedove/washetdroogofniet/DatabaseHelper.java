package nl.whitedove.washetdroogofniet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import nl.whitedove.washetdroogofniet.backend.whdonApi.model.Melding;

class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "whdon";

    private static final String TAB_MELDING = "Melding";
    private static final String MDG_ID = "Id";
    private static final String MDG_TELID = "TelId";
    private static final String MDG_LOCATIE = "Locatie";
    private static final String MDG_LAND = "Land";
    private static final String MDG_DATUM = "Datum";
    private static final String MDG_DROOG = "Droog";
    private static final String MDG_NAT = "Nat";
    private static final String MDG_TEMPERATUUR = "Temperatuur";
    private static final String MDG_WEERTYPE = "Weertype";
    private static final String MDG_WINDSPEED = "Windspeed";
    private static final String MDG_WINDDIR = "Winddir";

    private static DatabaseHelper sInstance;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE " + TAB_MELDING + "("
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
                + ")";
        db.execSQL(sql);

        sql = "CREATE INDEX IX1 ON " + TAB_MELDING + " (" + MDG_TELID + ")";
        db.execSQL(sql);

        sql = "CREATE INDEX IX2 ON " + TAB_MELDING + " (" + MDG_LOCATIE + ")";
        db.execSQL(sql);

        sql = "CREATE INDEX IX3 ON " + TAB_MELDING + " (" + MDG_DATUM + ")";
        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String sql = "ALTER TABLE " + TAB_MELDING + " ADD COLUMN " + MDG_TEMPERATUUR + " INTEGER NOT NULL DEFAULT 999";
            db.execSQL(sql);
        }
        if (oldVersion < 3) {
            String sql = "ALTER TABLE " + TAB_MELDING + " ADD COLUMN " + MDG_WEERTYPE + " INTEGER NOT NULL DEFAULT 0";
            db.execSQL(sql);
        }
        if (oldVersion < 4) {
            String sql = "ALTER TABLE " + TAB_MELDING + " ADD COLUMN " + MDG_WINDDIR + " INTEGER NOT NULL DEFAULT 0";
            db.execSQL(sql);
            sql = "ALTER TABLE " + TAB_MELDING + " ADD COLUMN " + MDG_WINDSPEED + " INTEGER NOT NULL DEFAULT 0";
            db.execSQL(sql);
        }
        if (oldVersion < 5) {
            String sql = "ALTER TABLE " + TAB_MELDING + " ADD COLUMN " + MDG_LAND + " TEXT NOT NULL DEFAULT 'NL'";
            db.execSQL(sql);
        }
    }

    void addMeldingen(List<Melding> meldingen) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("BEGIN TRANSACTION");
        for (Melding melding : meldingen) {

            String selectQuery = "SELECT"
                    + " " + MDG_ID
                    + " FROM " + TAB_MELDING
                    + " WHERE " + MDG_TELID + " = ?"
                    + " AND " + MDG_DATUM + " = ?"
                    + " LIMIT 1";

            Cursor cursor;
            cursor = db.rawQuery(selectQuery, new String[]{melding.getId(), Long.toString(melding.getDatum())});
            Boolean bestaat = cursor.moveToFirst();
            cursor.close();

            if (!bestaat) {
                ContentValues values = new ContentValues();
                values.put(MDG_TELID, melding.getId());
                values.put(MDG_LOCATIE, melding.getLocatie());
                values.put(MDG_DATUM, melding.getDatum());
                values.put(MDG_DROOG, melding.getDroog() ? 1 : 0);
                values.put(MDG_NAT, melding.getNat() ? 1 : 0);
                values.put(MDG_TEMPERATUUR, melding.getTemperatuur());
                values.put(MDG_WEERTYPE, melding.getWeerType());
                values.put(MDG_WINDDIR, melding.getWindDir());
                values.put(MDG_WINDSPEED, melding.getWindSpeed());
                values.put(MDG_LAND, melding.getLand());
                db.insert(TAB_MELDING, null, values);
            }
        }
        db.execSQL("END TRANSACTION");
    }

    Statistiek GetPersoonlijkeStatistiek(String id) {
        String selectQuery = "SELECT"
                + " SUM(" + MDG_DROOG + ") AS DROOG,"
                + " SUM(" + MDG_NAT + ") AS NAT"
                + " FROM " + TAB_MELDING
                + " WHERE " + MDG_TELID + " = ?";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        cursor = db.rawQuery(selectQuery, new String[]{id});

        Statistiek response = new Statistiek();
        response.setId(id);

        if (cursor.moveToFirst()) {
            response.setAantalDroog(cursor.getInt(0));
            response.setAantalNat(cursor.getInt(1));
        } else {
            response.setAantalDroog(0);
            response.setAantalNat(0);
        }
        cursor.close();
        return response;
    }

    ArrayList<Statistiek> GetPersoonlijkeStatsPerPlaats(String id) {
        String where = "";

        if (id != null) where = " WHERE " + MDG_TELID + " = ?";
        String selectQuery = "SELECT"
                + " " + MDG_LOCATIE + " AS LOCATIE,"
                + " " + MDG_LAND + " AS LAND,"
                + " SUM(" + MDG_DROOG + ") AS DROOG,"
                + " SUM(" + MDG_NAT + ") AS NAT,"
                + " MAX(" + MDG_DATUM + ") AS DATUM"
                + " FROM " + TAB_MELDING
                + where
                + " GROUP BY " + MDG_LOCATIE + "," + MDG_LAND
                + " ORDER BY " + MDG_DATUM + " DESC"
                + " LIMIT 100";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        if (id == null)
            cursor = db.rawQuery(selectQuery, null);
        else
            cursor = db.rawQuery(selectQuery, new String[]{id});

        ArrayList<Statistiek> stats = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                Statistiek stat = new Statistiek();
                stat.setLocatie(cursor.getString(0));
                stat.setLand(cursor.getString(1));
                stat.setAantalDroog(cursor.getInt(2));
                stat.setAantalNat(cursor.getInt(3));
                stats.add(stat);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return stats;
    }

    void DeleteMeldingen() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TAB_MELDING, null, null);
    }

    Melding GetLaatsteMelding(String id) {

        String selectQuery = "SELECT"
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
                + " LIMIT 1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        cursor = db.rawQuery(selectQuery, new String[]{id});

        Melding melding = new Melding();
        melding.setId(id);

        if (cursor.moveToFirst()) {
            melding.setLocatie(cursor.getString(0));
            melding.setDatum(cursor.getLong(1));
            melding.setDroog(cursor.getInt(2) == 1);
            melding.setNat(cursor.getInt(3) == 1);
            melding.setTemperatuur(cursor.getLong(4));
            melding.setWeerType(cursor.getLong(5));
            melding.setWindDir(cursor.getLong(6));
            melding.setWindSpeed(cursor.getLong(7));
            melding.setLand(cursor.getString(8));
        } else {
            melding.setError("Geen meldingen");
        }

        cursor.close();
        return melding;
    }

    ArrayList<Statistiek> GetStatistieken() {

        String selectQuery = "SELECT"
                + " " + MDG_LOCATIE + ","
                + " SUM(" + MDG_DROOG + ") AS DROOG,"
                + " SUM(" + MDG_NAT + ") AS NAT"
                + " FROM " + TAB_MELDING
                + " GROUP BY " + MDG_LOCATIE
                + " ORDER BY " + MDG_LOCATIE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        cursor = db.rawQuery(selectQuery, null);

        ArrayList<Statistiek> stats = new ArrayList<>();

        int totaalAantalDroog = 0;
        int totaalAantalNat = 0;

        if (cursor.moveToFirst()) {
            do {
                Statistiek stat = new Statistiek();
                stat.setLocatie(cursor.getString(0));
                stat.setAantalDroog(cursor.getInt(1));
                totaalAantalDroog += cursor.getInt(1);
                stat.setAantalNat(cursor.getInt(2));
                totaalAantalNat += cursor.getInt(2);
                stats.add(stat);
            } while (cursor.moveToNext());
        }
        cursor.close();

        Statistiek stat = new Statistiek();
        stat.setLocatie("Totaal");
        stat.setAantalDroog(totaalAantalDroog);
        stat.setAantalNat(totaalAantalNat);
        stats.add(stat);
        return stats;
    }

    ArrayList<Melding> GetLaatste25Meldingen() {

        String selectQuery = "SELECT"
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
                + " LIMIT 25";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        cursor = db.rawQuery(selectQuery, null);

        ArrayList<Melding> meldingen = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                Melding melding = new Melding();
                melding.setLocatie(cursor.getString(0));
                melding.setDatum(cursor.getLong(1));
                melding.setDroog(cursor.getInt(2) == 1);
                melding.setNat(cursor.getInt(3) == 1);
                melding.setTemperatuur(cursor.getLong(4));
                melding.setWeerType(cursor.getLong(5));
                melding.setWindDir(cursor.getLong(6));
                melding.setWindSpeed(cursor.getLong(7));
                melding.setLand(cursor.getString(8));
                meldingen.add(melding);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return meldingen;
    }

    ArrayList<Melding> GetMeldingen(String id) {

        String selectQuery = "SELECT"
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
                + " ORDER BY " + MDG_DATUM + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        cursor = db.rawQuery(selectQuery, new String[]{id});

        ArrayList<Melding> meldingen = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                Melding melding = new Melding();
                melding.setId(id);
                melding.setLocatie(cursor.getString(0));
                melding.setDatum(cursor.getLong(1));
                melding.setDroog(cursor.getInt(2) == 1);
                melding.setNat(cursor.getInt(3) == 1);
                melding.setTemperatuur(cursor.getLong(4));
                melding.setWeerType(cursor.getLong(5));
                melding.setWindDir(cursor.getLong(6));
                melding.setWindSpeed(cursor.getLong(7));
                melding.setLand(cursor.getString(8));
                meldingen.add(melding);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return meldingen;
    }

    ArrayList<Statistiek1Dag> GetStatistiek30Dagen(DateTime vanafDatum) {

        ArrayList<Statistiek1Dag> stats = new ArrayList<>();

        for (int i = 0; i < 30; i++) {

            String selectQuery = "SELECT"
                    + " SUM(" + MDG_DROOG + ") AS DROOG,"
                    + " SUM(" + MDG_NAT + ") AS NAT,"
                    + " MIN(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN 999 ELSE " + MDG_TEMPERATUUR + " END) AS MINTEMPERATUUR,"
                    + " MAX(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN -999 ELSE " + MDG_TEMPERATUUR + " END) AS MAXTEMPERATUUR"
                    + " FROM " + TAB_MELDING
                    + " WHERE " + MDG_DATUM + " BETWEEN ? AND ?";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor;
            DateTime va = new DateTime(vanafDatum.getYear(), vanafDatum.getMonthOfYear(), vanafDatum.getDayOfMonth(), 0, 0).plusDays(i);
            DateTime tm = va.plusDays(1);
            cursor = db.rawQuery(selectQuery, new String[]{Long.toString(va.getMillis()), Long.toString(tm.getMillis())});

            Statistiek1Dag stat = new Statistiek1Dag();
            stat.setDatum(va);

            if (cursor.moveToFirst()) {
                stat.setAantalDroog(cursor.getInt(0));
                stat.setAantalNat(cursor.getInt(1));
                stat.setMinTemperatuur(cursor.getInt(2));
                stat.setMaxTemperatuur(cursor.getInt(3));
            } else {
                stat.setAantalDroog(0);
                stat.setAantalNat(0);
                stat.setMinTemperatuur(0);
                stat.setMaxTemperatuur(0);
            }

            cursor.close();
            stats.add(stat);
        }

        return stats;
    }

    ArrayList<StatistiekAantalGebruikers> GetAantalGebruikers30Dagen(DateTime vanafDatum) {

        ArrayList<StatistiekAantalGebruikers> stats = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            String selectQuery = "SELECT"
                    + " COUNT(DISTINCT " + MDG_TELID + ") AS AANTAL"
                    + " FROM " + TAB_MELDING
                    + " WHERE " + MDG_DATUM + " < ?";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor;
            DateTime tm = vanafDatum.plusDays(i);

            cursor = db.rawQuery(selectQuery, new String[]{Long.toString(tm.plusDays(1).getMillis())});
            StatistiekAantalGebruikers stat = new StatistiekAantalGebruikers();
            stat.setDatum(tm);

            if (cursor.moveToFirst())
                stat.setAantalGebruikers(cursor.getInt(0));
            else
                stat.setAantalGebruikers(0);

            cursor.close();
            stats.add(stat);
        }
        return stats;
    }

    Statistiek1Plaats GetStatistiekLocatie(String locatie) {

        String selectQuery;
        Cursor cursor;
        SQLiteDatabase db = this.getWritableDatabase();

        if (locatie.equals("Totaal")) {
            selectQuery = "SELECT"
                    + " SUM(" + MDG_DROOG + ") AS DROOG,"
                    + " SUM(" + MDG_NAT + ") AS NAT,"
                    + " MIN(" + MDG_DATUM + ") AS MINDATUM,"
                    + " MAX(" + MDG_DATUM + ") AS MAXDATUM,"
                    + " SUM(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN 0 ELSE 1 END) AS AANTALTEMPERATUUR,"
                    + " SUM(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN 0 ELSE " + MDG_TEMPERATUUR + " END) AS SOMTEMPERATUUR"
                    + " FROM " + TAB_MELDING;
            cursor = db.rawQuery(selectQuery, null);
        } else {
            selectQuery = "SELECT"
                    + " SUM(" + MDG_DROOG + ") AS DROOG,"
                    + " SUM(" + MDG_NAT + ") AS NAT,"
                    + " MIN(" + MDG_DATUM + ") AS MINDATUM,"
                    + " MAX(" + MDG_DATUM + ") AS MAXDATUM,"
                    + " SUM(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN 0 ELSE 1 END) AS AANTALTEMPERATUUR,"
                    + " SUM(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN 0 ELSE " + MDG_TEMPERATUUR + " END) AS SOMTEMPERATUUR"
                    + " FROM " + TAB_MELDING
                    + " WHERE " + MDG_LOCATIE + "=?";

            cursor = db.rawQuery(selectQuery, new String[]{locatie});
        }

        Statistiek1Plaats stat = new Statistiek1Plaats();

        stat.setLocatie(locatie);

        if (cursor.moveToFirst()) {
            stat.setAantalDroog(cursor.getInt(0));
            stat.setAantalNat(cursor.getInt(1));
            stat.setDatumStart(cursor.getLong(2));
            stat.setDatumEnd(cursor.getLong(3));
            stat.setAantalTemperatuur(cursor.getInt(4));
            stat.setSomTemperatuur(cursor.getInt(5));
        }

        cursor.close();
        return stat;
    }

    ArrayList<Statistiek1Maand> GetStatistiek12Maanden(int jaar, int maand) {

        ArrayList<Statistiek1Maand> stats = new ArrayList<>();
        DateTime vanaf;

        if (maand == 12)
            vanaf = new DateTime(jaar, 1, 1, 0, 0);
        else
            vanaf = new DateTime(jaar - 1, maand + 1, 1, 0, 0);

        for (int i = 0; i < 12; i++) {

            String selectQuery = "SELECT "
                    + " SUM(" + MDG_DROOG + ") AS DROOG,"
                    + " SUM(" + MDG_NAT + ") AS NAT,"
                    + " AVG(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN NULL ELSE " + MDG_TEMPERATUUR + " END) AS AVGTEMPERATUUR,"
                    + " MIN(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN 999 ELSE " + MDG_TEMPERATUUR + " END) AS MINTEMPERATUUR,"
                    + " MAX(CASE WHEN " + MDG_TEMPERATUUR + " = 999 THEN -999 ELSE " + MDG_TEMPERATUUR + " END) AS MAXTEMPERATUUR"
                    + " FROM " + TAB_MELDING
                    + " WHERE " + MDG_DATUM + " BETWEEN ? AND ?";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor;
            DateTime va = vanaf.plusMonths(i);
            DateTime tm = va.plusMonths(1);
            cursor = db.rawQuery(selectQuery, new String[]{Long.toString(va.getMillis()), Long.toString(tm.getMillis())});

            Statistiek1Maand stat = new Statistiek1Maand();
            stat.setMaand(va.getMonthOfYear());

            if (cursor.moveToFirst()) {
                stat.setAantalDroog(cursor.getInt(0));
                stat.setAantalNat(cursor.getInt(1));
                stat.setAvgTemperatuur(cursor.getFloat(2));
                stat.setMinTemperatuur(cursor.getFloat(3));
                stat.setMaxTemperatuur(cursor.getFloat(4));
            } else {
                stat.setAantalDroog(0);
                stat.setAantalNat(0);
                stat.setAvgTemperatuur(0);
                stat.setMinTemperatuur(0);
                stat.setMaxTemperatuur(0);
            }

            cursor.close();
            stats.add(stat);
        }
        return stats;
    }

    ArrayList<Statistiek1Uur> GetStatistiek24Uur(Helper.Periode ajm, int jaar, int maand) {

        String selectQuery = "SELECT"
                + " ((3600000 + " + MDG_DATUM + ") / 3600000) % 24 AS UUR,"
                + " SUM(" + MDG_DROOG + ") AS DROOG,"
                + " SUM(" + MDG_NAT + ") AS NAT"
                + " FROM " + TAB_MELDING
                + " WHERE " + MDG_DATUM + " BETWEEN ? AND ?"
                + " GROUP BY ((3600000 + " + MDG_DATUM + ") / 3600000) % 24"
                + " ORDER BY UUR";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        DateTime va;
        DateTime tm;

        va = new DateTime(2015, 12, 1, 0, 0);
        tm = DateTime.now().plusDays(1);

        if (ajm == Helper.Periode.Jaar) {
            va = new DateTime(jaar, 1, 1, 0, 0);
            tm = va.plusYears(1);
        }

        if (ajm == Helper.Periode.Maand) {
            va = new DateTime(jaar, maand, 1, 0, 0);
            tm = va.plusMonths(1);
        }

        cursor = db.rawQuery(selectQuery, new String[]{Long.toString(va.getMillis()), Long.toString(tm.getMillis())});

        ArrayList<Statistiek1Uur> stats = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                Statistiek1Uur stat = new Statistiek1Uur();
                stat.setUur(cursor.getInt(0));
                stat.setAantalDroog(cursor.getInt(1));
                stat.setAantalNat(cursor.getInt(2));
                stats.add(stat);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return stats;
    }

    ArrayList<StatistiekDagVdWeek> GetStatistiekDagVdWeek(Helper.Periode ajm, int jaar, int maand) {

        // 1 jan 1970 is een donderdag (ma=0, di=1, wo=2, do=3 etc), daarom 3 x 86400000 erbij

        String selectQuery = "SELECT"
                + " ((259200000 + " + MDG_DATUM + ") / 86400000) % 7 AS DAG,"
                + " SUM(" + MDG_DROOG + ") AS DROOG,"
                + " SUM(" + MDG_NAT + ") AS NAT"
                + " FROM " + TAB_MELDING
                + " WHERE " + MDG_DATUM + " BETWEEN ? AND ?"
                + " GROUP BY ((259200000 + " + MDG_DATUM + ") / 86400000) % 7"
                + " ORDER BY DAG";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        DateTime va;
        DateTime tm;

        va = new DateTime(2015, 12, 1, 0, 0);
        tm = DateTime.now().plusDays(1);

        if (ajm == Helper.Periode.Jaar) {
            va = new DateTime(jaar, 1, 1, 0, 0);
            tm = va.plusYears(1);
        }

        if (ajm == Helper.Periode.Maand) {
            va = new DateTime(jaar, maand, 1, 0, 0);
            tm = va.plusMonths(1);
        }

        cursor = db.rawQuery(selectQuery, new String[]{Long.toString(va.getMillis()), Long.toString(tm.getMillis())});

        ArrayList<StatistiekDagVdWeek> stats = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                StatistiekDagVdWeek stat = new StatistiekDagVdWeek();
                stat.setDag(cursor.getInt(0));
                stat.setAantalDroog(cursor.getInt(1));
                stat.setAantalNat(cursor.getInt(2));
                stats.add(stat);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return stats;
    }

    ArrayList<StatistiekWeertype> GetStatistiekWeerType(Helper.Periode ajm, int jaar, int maand) {

        String selectQuery = "SELECT"
                + " " + MDG_WEERTYPE + ","
                + " COUNT(*) AS AANTAL"
                + " FROM " + TAB_MELDING
                + " WHERE " + MDG_WEERTYPE + " > 0 "
                + " AND " + MDG_DATUM + " BETWEEN ? AND ?"
                + " GROUP BY " + MDG_WEERTYPE
                + " ORDER BY AANTAL DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        DateTime va;
        DateTime tm;

        va = new DateTime(2015, 12, 1, 0, 0);
        tm = DateTime.now().plusDays(1);

        if (ajm == Helper.Periode.Jaar) {
            va = new DateTime(jaar, 1, 1, 0, 0);
            tm = va.plusYears(1);
        }

        if (ajm == Helper.Periode.Maand) {
            va = new DateTime(jaar, maand, 1, 0, 0);
            tm = va.plusMonths(1);
        }

        cursor = db.rawQuery(selectQuery, new String[]{Long.toString(va.getMillis()), Long.toString(tm.getMillis())});

        ArrayList<StatistiekWeertype> stats = new ArrayList<>();

        int totaal = 0;
        if (cursor.moveToFirst()) {
            do {
                StatistiekWeertype stat = new StatistiekWeertype();
                WeerHelper.WeerType weerType = WeerHelper.WeerType.valueOf(cursor.getLong(0));
                stat.setWeerType(weerType);
                stat.setAantal(cursor.getInt(1));
                stat.setWeerTypeOmschrijving(WeerHelper.WeerTypeToWeerOmschrijving(weerType));
                stats.add(stat);
                totaal += stat.getAantal();
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (stats.size() == 0) return stats;

        for (int i = 0; i < stats.size(); i++) {
            float percentage = 100.0F * stats.get(i).getAantal() / totaal;
            stats.get(i).setPercentage(percentage);
        }

        return stats;
    }


    private ArrayList<StatistiekWind> WindStatAddZeros(ArrayList<StatistiekWind> windstat) {
        for (WeerHelper.WindDirection windDir : WeerHelper.WindDirection.values()) {
            boolean gevonden = false;
            for (int i = 0; i < windstat.size(); i++) {
                if (windDir == windstat.get(i).getWindDir()) {
                    gevonden = true;
                    break;
                }
            }

            if (!gevonden && windDir != WeerHelper.WindDirection.Onbekend) {
                StatistiekWind stat = new StatistiekWind();
                stat.setWindDir(windDir);
                stat.setWindOmschrijving(WeerHelper.WindDirectionToOmschrijving(windDir));
                stat.setAantal(0);
                stat.setAvgWindSpeed(0);
                stat.setMaxWindSpeed(0);
                stat.setPercentage(0);
                windstat.add(stat);
            }
        }

        return windstat;
    }

    ArrayList<StatistiekWind> GetStatistiekWind(Helper.Periode ajm, int jaar, int maand) {

        String selectQuery = "SELECT"
                + " " + MDG_WINDDIR + ","
                + " COUNT(*) AS AANTAL,"
                + " AVG(" + MDG_WINDSPEED + ") AS AVGWINDSPEED,"
                + " MIN(" + MDG_WINDSPEED + ") AS MINWINDSPEED,"
                + " MAX(" + MDG_WINDSPEED + ") AS MAXWINDSPEED "
                + " FROM " + TAB_MELDING
                + " WHERE " + MDG_WINDDIR + " > 0 "
                + " AND " + MDG_DATUM + " BETWEEN ? AND ?"
                + " GROUP BY " + MDG_WINDDIR;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        DateTime va;
        DateTime tm;

        va = new DateTime(2015, 12, 1, 0, 0);
        tm = DateTime.now().plusDays(1);

        if (ajm == Helper.Periode.Jaar) {
            va = new DateTime(jaar, 1, 1, 0, 0);
            tm = va.plusYears(1);
        }

        if (ajm == Helper.Periode.Maand) {
            va = new DateTime(jaar, maand, 1, 0, 0);
            tm = va.plusMonths(1);
        }

        cursor = db.rawQuery(selectQuery, new String[]{Long.toString(va.getMillis()), Long.toString(tm.getMillis())});

        ArrayList<StatistiekWind> stats = new ArrayList<>();

        int totaal = 0;
        if (cursor.moveToFirst()) {
            do {
                StatistiekWind stat = new StatistiekWind();
                WeerHelper.WindDirection windDir = WeerHelper.WindDirection.valueOf(cursor.getLong(0));
                stat.setWindDir(windDir);
                stat.setAantal(cursor.getInt(1));
                stat.setWindOmschrijving(WeerHelper.WindDirectionToOmschrijving(windDir));
                stat.setAvgWindSpeed(cursor.getFloat(2));
                stat.setMinWindSpeed(cursor.getFloat(3));
                stat.setMaxWindSpeed(cursor.getFloat(4));
                stats.add(stat);
                totaal += stat.getAantal();
            } while (cursor.moveToNext());
        }
        cursor.close();

        for (int i = 0; i < stats.size(); i++) {
            float percentage = 100.0F * stats.get(i).getAantal() / totaal;
            stats.get(i).setPercentage(percentage);
        }

        stats = WindStatAddZeros(stats);
        return stats;
    }

    StatistiekRecords GetStatistiekRecords() {

        String selectQuery = "SELECT"
                + " " + MDG_TEMPERATUUR + " AS MINTEMPERATUUR,"
                + " " + MDG_DATUM
                + " FROM " + TAB_MELDING
                + " ORDER BY " + MDG_TEMPERATUUR + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor;
        cursor = db.rawQuery(selectQuery, null);

        StatistiekRecords stat = new StatistiekRecords();

        if (cursor.moveToFirst()) {
            stat.setMinTemp(cursor.getFloat(1));
            stat.setMinTempDatum(new DateTime(cursor.getLong(2)));
        }
        cursor.close();

        selectQuery = "SELECT"
                + " " + MDG_TEMPERATUUR + " AS MAXTEMPERATUUR,"
                + " " + MDG_DATUM
                + " FROM " + TAB_MELDING
                + " ORDER BY " + MDG_TEMPERATUUR + " DESC";

        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            stat.setMaxTemp(cursor.getFloat(1));
            stat.setMaxTempDatum(new DateTime(cursor.getLong(2)));
        }
        cursor.close();

        selectQuery = "SELECT"
                + " " + MDG_WINDSPEED + " AS MAXWINDSPEED,"
                + " " + MDG_DATUM
                + " FROM " + TAB_MELDING
                + " ORDER BY " + MDG_WINDSPEED + " DESC";

        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            stat.setMaxWind(cursor.getFloat(1));
            stat.setMaxWindDatum(new DateTime(cursor.getLong(2)));
        }
        cursor.close();

        selectQuery = "SELECT"
                + " STRFTIME('%Y-%m', " + MDG_DATUM + " / 1000, 'unixepoch', 'localtime') AS yyyymm,"
                + " COUNT(*) AS AANTAL,"
                + " SUM(CASE WHEN " + MDG_NAT + " = 1 THEN 1 ELSE 0 END) AS nat,"
                + " SUM(CASE WHEN " + MDG_DROOG + " = 1 THEN 1 ELSE 0 END) AS droog"
                + " FROM " + TAB_MELDING
                + " GROUP BY STRFTIME('%Y-%m', " + MDG_DATUM + " / 1000, 'unixepoch', 'localtime')";

        cursor = db.rawQuery(selectQuery, null);

        String droogJM = "2015-01";
        String natJM = "2015-01";
        float droogPercent = 0;
        float natPercent = 0;

        if (cursor.moveToFirst()) {
            do {
                String jaarmaand = cursor.getString(1);
                float totaal = cursor.getFloat(2);
                float aantalDroog = cursor.getFloat(3);
                float aantalNat = cursor.getFloat(4);
                float percDroog = 100f * aantalDroog / totaal;
                float percNat = 100f * aantalNat / totaal;
                if (percDroog > droogPercent) {
                    droogPercent = percDroog;
                    droogJM = jaarmaand;
                }
                if (percNat > natPercent) {
                    natPercent = percNat;
                    natJM = jaarmaand;
                }
            } while (cursor.moveToNext());

        }
        cursor.close();
        int droogJaar = Integer.parseInt(droogJM.substring(0, 4));
        int droogMaand = Integer.parseInt(droogJM.substring(5));
        stat.setDroogsteMaand(new DateTime(droogJaar, droogMaand, 1, 0, 0));
        stat.setPercentDroog(droogPercent);

        int natJaar = Integer.parseInt(natJM.substring(0, 4));
        int natMaand = Integer.parseInt(natJM.substring(5));
        stat.setDroogsteMaand(new DateTime(natJaar, natMaand, 1, 0, 0));
        stat.setPercentNat(natPercent);
        stat.setMaxWindDatum(new DateTime(cursor.getLong(2)));

        return stat;
    }
}

