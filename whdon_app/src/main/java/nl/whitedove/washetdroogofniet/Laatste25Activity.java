package nl.whitedove.washetdroogofniet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.ArrayList;

import nl.whitedove.washetdroogofniet.backend.whdonApi.model.Melding;

public class Laatste25Activity extends Activity {
    DatabaseHelper mDH;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.laatste25);

        FloatingActionButton fabTerug = (FloatingActionButton) findViewById(R.id.btnTerug);
        fabTerug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Terug();
            }
        });
        InitDb();
        ToondataBackground();
    }

    private void InitDb() {
        mDH = new DatabaseHelper(getApplicationContext());
    }

    private void Terug() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void ToondataBackground() {
        Context cxt = getApplicationContext();
        new AsyncGetLaatste25Task().execute();
    }

    @SuppressLint("DefaultLocale")
    private void ToonLaatste25(ArrayList<Melding> meldingen) {
        if (meldingen == null || meldingen.size() == 0) {
            return;
        }

        TextView tvPsAantalDroog = (TextView) findViewById(R.id.tvPsAantalDroog);
        TextView tvPsAantalNat = (TextView) findViewById(R.id.tvPsAantalNat);
        TextView tvPsGemm = (TextView) findViewById(R.id.tvPsGemm);

        int aantalDroog = 0;
        int aantalNat = 0;
        float aantalGemm;
        DateTime datum = DateTime.now();

        for (Melding rMeld : meldingen) {
            aantalDroog += rMeld.getDroog() ? 1 : 0;
            aantalNat += rMeld.getNat() ? 1 : 0;
            DateTime melddat = new DateTime(rMeld.getDatum());
            if (melddat.isBefore(datum)) {
                datum = new DateTime(rMeld.getDatum());
            }
        }

        int aantalDagen = Days.daysBetween(datum, DateTime.now()).getDays() + 1;
        aantalGemm = (aantalNat + aantalDroog) / (1.0f * aantalDagen);

        tvPsAantalDroog.setText(String.format("%d", aantalDroog));
        tvPsAantalNat.setText(String.format("%d", aantalNat));
        tvPsGemm.setText(String.format("%.1f", aantalGemm));

        final ListView lvLaatste25 = (ListView) findViewById(R.id.lvLaatste25);
        lvLaatste25.setAdapter(new CustomListAdapterMeldingen(this, meldingen));
    }

    private class AsyncGetLaatste25Task extends AsyncTask<Void, Void,  ArrayList<Melding>> {

        @Override
        protected  ArrayList<Melding> doInBackground(Void... params) {
            return mDH.GetLaatste25Meldingen();
        }

        @Override
        protected void onPostExecute( ArrayList<Melding> meldingen) {
            ToonLaatste25(meldingen);
        }
    }
}