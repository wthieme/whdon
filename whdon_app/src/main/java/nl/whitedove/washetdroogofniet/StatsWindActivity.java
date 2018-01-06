package nl.whitedove.washetdroogofniet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class StatsWindActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wind_statistieken);

        FloatingActionButton fabTerug = findViewById(R.id.btnTerug);
        fabTerug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Terug();
            }
        });
        ToondataBackground();
    }

    private void Terug() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void ToondataBackground() {
        Context context = getApplicationContext();
        new AsyncGetStatistiekWindTask(this).execute(context);
    }

    @SuppressLint("DefaultLocale")
    private void ToonStatistiekWind(ArrayList<StatistiekWind> stats) {
        if (stats == null || stats.size() == 0) {
            return;
        }

        int[] colors = new int[]{ContextCompat.getColor(this, R.color.colorGrafiek1),
                ContextCompat.getColor(this, R.color.colorGrafiek2),
                ContextCompat.getColor(this, R.color.colorGrafiek3),
                ContextCompat.getColor(this, R.color.colorGrafiek4),
                ContextCompat.getColor(this, R.color.colorGrafiek5),
                ContextCompat.getColor(this, R.color.colorGrafiek6),
                ContextCompat.getColor(this, R.color.colorGrafiek7),
                ContextCompat.getColor(this, R.color.colorGrafiek8)};

        ArrayList<RadarEntry> dataT = new ArrayList<>();
        ArrayList<LegendEntry> legendEntries = new ArrayList<>();

        int totaal = 0;
        for (int i = 0; i < stats.size(); i++) {
            totaal += stats.get(i).getAantal();
        }

        int totaalPercentage = 0;
        for (int i = 0; i < stats.size(); i++) {
            totaalPercentage += Math.round(100.0F * stats.get(i).getAantal() / totaal);
        }

        int correctie = 100 - totaalPercentage;

        for (int i = 0; i < stats.size(); i++) {
            int perc = Math.round(stats.get(i).getPercentage());
            if (i == 0) perc += correctie;
            dataT.add(new RadarEntry(stats.get(i).getPercentage(), (perc <= 2) ? "" : String.format("%d%%", perc)));
            LegendEntry le = new LegendEntry();
            le.formColor = colors[i];
            le.label = stats.get(i).getWindOmschrijving();
            le.form = Legend.LegendForm.SQUARE;
            le.formSize = 10;
            legendEntries.add(le);
        }

        final RadarChart chart = findViewById(R.id.rcPerWindrichting);

        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);

        chart.setTouchEnabled(false);
        chart.setNoDataText(getString(R.string.nodata));
        chart.getLegend().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setCustom(legendEntries);
        legend.setEnabled(true);
        legend.setXEntrySpace(20f);
        legend.setTextSize(12f);
        legend.setWordWrapEnabled(true);

        RadarDataSet dsT = new RadarDataSet(dataT, "");
        dsT.setColors(colors);
        dsT.setDrawValues(false);

        RadarData data = new RadarData(dsT);
        data.setValueTextSize(14f);
        data.setValueTextColor(ContextCompat.getColor(this, R.color.colorTekst));

        chart.setData(data);
        chart.animateXY(500, 500);
        chart.invalidate();
    }

    private static class AsyncGetStatistiekWindTask extends AsyncTask<Context, Void, ArrayList<StatistiekWind>> {
        private WeakReference<StatsWindActivity> activityWeakReference;

        AsyncGetStatistiekWindTask(StatsWindActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<StatistiekWind> doInBackground(Context... params) {
            Context context = params[0];
            DatabaseHelper dh = DatabaseHelper.getInstance(context);
            return dh.GetStatistiekWind();
        }

        @Override
        protected void onPostExecute(ArrayList<StatistiekWind> stats) {
            StatsWindActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonStatistiekWind(stats);
        }
    }
}