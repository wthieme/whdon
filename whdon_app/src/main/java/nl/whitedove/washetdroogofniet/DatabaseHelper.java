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

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "whdon";

    private static final String TAB_MELDING = "Melding";
    private static final String MDG_ID = "Id";
    private static final String MDG_TELID = "TelId";
    private static final String MDG_LOCATIE = "Locatie";
    private static final String MDG_DATUM = "Datum";
    private static final String MDG_DROOG = "Droog";
    private static final String MDG_NAT = "Nat";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE " + TAB_MELDING + "("
                + MDG_ID + " INTEGER PRIMARY KEY NOT NULL,"
                + MDG_TELID + " TEXT NOT NULL,"
                + MDG_LOCATIE + " TEXT NOT NULL,"
                + MDG_DATUM + " INTEGER NOT NULL,"
                + MDG_DROOG + " INTEGER NOT NULL,"
                + MDG_NAT + " INTEGER NOT NULL"
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
    }

    void addMeldingen(List<Melding> meldingen) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("BEGIN TRANSACTION");
        for (Melding melding : meldingen) {

            String selectQuery = "SELECT "
                    + MDG_ID
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
                db.insert(TAB_MELDING, null, values);
            }
        }
        db.execSQL("END TRANSACTION");
        db.close();
    }

    Statistiek GetPersoonlijkeStatistiek(String id) {
        String selectQuery = "SELECT "
                + "SUM(" + MDG_DROOG + ") AS DROOG,"
                + "SUM(" + MDG_NAT + ") AS NAT"
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

    void DeleteMeldingen() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TAB_MELDING, null, null);
        db.close();
    }

    Melding GetLaatsteMelding(String id) {

        String selectQuery = "SELECT "
                + MDG_LOCATIE + ","
                + MDG_DATUM + ","
                + MDG_DROOG + ","
                + MDG_NAT
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
        } else {
            melding.setError("Geen meldingen");
        }

        cursor.close();
        return melding;
    }

    ArrayList<Statistiek> GetStatistieken() {

        String selectQuery = "SELECT "
                + MDG_LOCATIE + ","
                + "SUM(" + MDG_DROOG + ") AS DROOG,"
                + "SUM(" + MDG_NAT + ") AS NAT"
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

        String selectQuery = "SELECT "
                + MDG_LOCATIE + ","
                + MDG_DATUM + ","
                + MDG_DROOG + ","
                + MDG_NAT
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
                meldingen.add(melding);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return meldingen;
    }

    ArrayList<Melding> GetMeldingen(String id) {

        String selectQuery = "SELECT "
                + MDG_LOCATIE + ","
                + MDG_DATUM + ","
                + MDG_DROOG + ","
                + MDG_NAT
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
                meldingen.add(melding);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return meldingen;
    }


    ArrayList<Statistiek1Dag> GetStatistiek30Dagen(DateTime vanafDatum) {

        ArrayList<Statistiek1Dag> stats = new ArrayList<>();

        for (int i = 0; i < 30; i++) {

            String selectQuery = "SELECT "
                    + "SUM(" + MDG_DROOG + ") AS DROOG,"
                    + "SUM(" + MDG_NAT + ") AS NAT"
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
            } else {
                stat.setAantalDroog(0);
                stat.setAantalNat(0);
            }

            cursor.close();
            stats.add(stat);
        }

        return stats;
    }

    ArrayList<StatistiekAantalGebruikers> GetAantalGebruikers30Dagen(DateTime vanafDatum) {

        ArrayList<StatistiekAantalGebruikers> stats = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            String selectQuery = "SELECT "
                    + "COUNT(DISTINCT " + MDG_TELID + ") AS AANTAL"
                    + " FROM " + TAB_MELDING
                    + " WHERE " + MDG_DATUM + " < ?";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor;
            DateTime tm = vanafDatum.plusDays(i+1);

            cursor = db.rawQuery(selectQuery, new String[]{Long.toString(tm.getMillis())});
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
            selectQuery = "SELECT "
                    + "SUM(" + MDG_DROOG + ") AS DROOG,"
                    + "SUM(" + MDG_NAT + ") AS NAT,"
                    + "MIN(" + MDG_DATUM + ") AS MINDATUM,"
                    + "MAX(" + MDG_DATUM + ") AS MAXDATUM"
                    + " FROM " + TAB_MELDING;
            cursor = db.rawQuery(selectQuery, null);
        } else {
            selectQuery = "SELECT "
                    + "SUM(" + MDG_DROOG + ") AS DROOG,"
                    + "SUM(" + MDG_NAT + ") AS NAT,"
                    + "MIN(" + MDG_DATUM + ") AS MINDATUM,"
                    + "MAX(" + MDG_DATUM + ") AS MAXDATUM"
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
        }

        cursor.close();
        return stat;
    }

    ArrayList<Statistiek1Maand> GetStatistiek12Maanden(int jaar) {

        ArrayList<Statistiek1Maand> stats = new ArrayList<>();

        for (int i = 0; i < 12; i++) {

            String selectQuery = "SELECT "
                    + "SUM(" + MDG_DROOG + ") AS DROOG,"
                    + "SUM(" + MDG_NAT + ") AS NAT"
                    + " FROM " + TAB_MELDING
                    + " WHERE " + MDG_DATUM + " BETWEEN ? AND ?";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor;
            DateTime va = new DateTime(jaar, i + 1, 1, 0, 0);
            DateTime tm = va.plusMonths(1);
            cursor = db.rawQuery(selectQuery, new String[]{Long.toString(va.getMillis()), Long.toString(tm.getMillis())});

            Statistiek1Maand stat = new Statistiek1Maand();
            stat.setMaand(va.getMonthOfYear());

            if (cursor.moveToFirst()) {
                stat.setAantalDroog(cursor.getInt(0));
                stat.setAantalNat(cursor.getInt(1));
            } else {
                stat.setAantalDroog(0);
                stat.setAantalNat(0);
            }

            cursor.close();
            stats.add(stat);
        }
        return stats;
    }
}