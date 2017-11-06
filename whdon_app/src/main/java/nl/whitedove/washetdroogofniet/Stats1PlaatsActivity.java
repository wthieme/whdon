package nl.whitedove.washetdroogofniet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class Stats1PlaatsActivity extends Activity {

    static DatabaseHelper mDH;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.een_plaats_statistieken);

        FloatingActionButton fabTerug = findViewById(R.id.btnTerug);
        fabTerug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Terug();
            }
        });

        Intent myIntent = getIntent(); // gets the previously created intent
        String locatie = myIntent.getStringExtra("Locatie");

        InitDb();
        ToondataBackground(locatie);
    }

    private void InitDb() {
        mDH = new DatabaseHelper(getApplicationContext());
    }

    private void Terug() {
        Intent intent = new Intent(this, StatsPerPlaatsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void ToondataBackground(String locatie) {
        new AsyncGetStatistiekLocatieTask(this).execute(locatie);
    }

    @SuppressLint("DefaultLocale")
    private void ToonStatistiekLocatie(Statistiek1Plaats stat) {
        if (stat == null || stat.getLocatie() == null) {
            return;
        }

        TextView tvLocatie = findViewById(R.id.tvLocatie);
        TextView tvPsDatumStart = findViewById(R.id.tvPsDatumStart);
        TextView tvPsDatumEnd = findViewById(R.id.tvPsDatumEnd);
        TextView tvPsAantalDroog = findViewById(R.id.tvPsAantalDroog);
        TextView tvPsAantalNat = findViewById(R.id.tvPsAantalNat);
        TextView tvPsGemm = findViewById(R.id.tvPsGemm);
        TextView tvPsGemmTemp = findViewById(R.id.tvPsGemmTemp);

        int aantalDroog = stat.getAantalDroog();
        int aantalNat = stat.getAantalNat();
        int totaal = aantalDroog + aantalNat;

        float aantalGemm;
        DateTime datumStart = new DateTime(stat.getDatumStart());
        DateTime datumEnd = new DateTime(stat.getDatumEnd());

        int aantalDagen = Days.daysBetween(datumStart, datumEnd).getDays() + 1;

        aantalGemm = (aantalNat + aantalDroog) / (1.0f * aantalDagen);
        int percDroog = Math.round(100.0F * aantalDroog / totaal);
        int percNat = 100 - percDroog;

        tvLocatie.setText(stat.getLocatie());
        tvPsDatumStart.setText(Helper.dFormat.print(datumStart));
        tvPsDatumEnd.setText(Helper.dFormat.print(datumEnd));
        tvPsAantalDroog.setText(String.format("%d", aantalDroog));
        tvPsAantalNat.setText(String.format("%d", aantalNat));
        tvPsGemm.setText(String.format("%.1f", aantalGemm));

        int aantalTemp = stat.getAantalTemperatuur();

        if (aantalTemp > 0) {
            int tempSom = stat.getSomTemperatuur();
            float tempGemm = (1.0f * tempSom) / (1.0f * aantalTemp);
            tvPsGemmTemp.setText(String.format("%.1f", tempGemm));
        }

        final PieChart chart = findViewById(R.id.pcPerdag);

        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);

        chart.setTouchEnabled(false);
        chart.setNoDataText(getString(R.string.nodata));
        chart.getLegend().setEnabled(false);
        chart.setEntryLabelColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        chart.setDrawEntryLabels(true);

        ArrayList<PieEntry> dataT = new ArrayList<>();

        dataT.add(new PieEntry(percDroog, String.format("%d%%", percDroog)));
        dataT.add(new PieEntry(percNat, String.format("%d%%", percNat)));

        Legend legend = chart.getLegend();
        LegendEntry le1 = new LegendEntry();
        le1.formColor = ContextCompat.getColor(this, R.color.colorNatStart);
        le1.label = this.getString(R.string.NatTxt);
        le1.form = Legend.LegendForm.SQUARE;
        le1.formSize = 10;
        LegendEntry le2 = new LegendEntry();
        le2.formColor = ContextCompat.getColor(this, R.color.colorDroogStart);
        le2.label = this.getString(R.string.DroogTxt);
        le2.form = Legend.LegendForm.SQUARE;
        le2.formSize = 10;

        legend.setCustom(new LegendEntry[]{le1, le2});
        legend.setEnabled(true);
        legend.setXEntrySpace(20f);
        legend.setTextSize(12f);
        legend.setWordWrapEnabled(true);

        PieDataSet dsT = new PieDataSet(dataT, "");
        dsT.setColors(ContextCompat.getColor(this, R.color.colorDroogStart), ContextCompat.getColor(this, R.color.colorNatStart));
        dsT.setDrawValues(false);

        PieData data = new PieData(dsT);
        data.setValueTextSize(14f);
        data.setValueTextColor(ContextCompat.getColor(this, R.color.colorTekst));

        chart.setData(data);
        chart.animateXY(500, 500);
        chart.invalidate();
    }

    private static class AsyncGetStatistiekLocatieTask extends AsyncTask<String, Void, Statistiek1Plaats> {
        private WeakReference<Stats1PlaatsActivity> activityWeakReference;

        AsyncGetStatistiekLocatieTask(Stats1PlaatsActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Statistiek1Plaats doInBackground(String... params) {
            String locatie = params[0];
            return mDH.GetStatistiekLocatie(locatie);
        }

        @Override
        protected void onPostExecute(Statistiek1Plaats stat) {
            Stats1PlaatsActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonStatistiekLocatie(stat);
        }
    }
}