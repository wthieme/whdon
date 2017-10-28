package nl.whitedove.washetdroogofniet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class StatsWeerTypeActivity extends Activity {

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
        ToondataBackground();
    }

    private void InitDb() {
        mDH = new DatabaseHelper(getApplicationContext());
    }

    private void Terug() {
        Intent intent = new Intent(this, StatsPerPlaatsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void ToondataBackground() {
        new AsyncGetStatistiekWeerTypeTask(this).execute();
    }

    @SuppressLint("DefaultLocale")
    private void ToonStatistiekWeerType(ArrayList<StatistiekWeertype> stats) {
        if (stats == null || stats.size() == 0) {
            return;
        }

        ArrayList<PieEntry> dataT = new ArrayList<>();
        ArrayList<LegendEntry> legendEntries = new ArrayList<>();

        int totaal = 0;
        for (int i = 0; i < stats.size(); i++) {
            totaal += stats.get(i).getAantal();
        }

        int totaalPercentage=0;
        for (int i = 0; i < stats.size(); i++) {
            int percentage = Math.round(100.0F * stats.get(i).getAantal() / totaal);
            totaalPercentage += percentage;
            dataT.add(new PieEntry(percentage, String.format("%d%%", percentage)));
            LegendEntry le1 = new LegendEntry();
            le1.formColor = ContextCompat.getColor(this, R.color.colorNatStart);
            le1.label = stats.get(i).getWeerTypeOschrijving();
            le1.form = Legend.LegendForm.SQUARE;
            le1.formSize = 10;
            legendEntries.add(le1);
        }

        final PieChart chart = findViewById(R.id.pcPerWeertype);

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

        PieDataSet dsT = new PieDataSet(dataT, "");
        dsT.setColors(ContextCompat.getColor(this, R.color.colorDroogStart), ContextCompat.getColor(this, R.color.colorNatStart));
        IValueFormatter myValueFormat = new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "";
            }
        };
        dsT.setValueFormatter(myValueFormat);

        PieData data = new PieData(dsT);
        data.setValueTextSize(14f);
        data.setValueTextColor(ContextCompat.getColor(this, R.color.colorTekst));

        chart.setData(data);
        chart.animateXY(500, 500);
        chart.invalidate();
    }

    private static class AsyncGetStatistiekWeerTypeTask extends AsyncTask<Void, Void, ArrayList<StatistiekWeertype>> {
        private WeakReference<StatsWeerTypeActivity> activityWeakReference;

        AsyncGetStatistiekWeerTypeTask(StatsWeerTypeActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<StatistiekWeertype> doInBackground(Void... params) {
            return mDH.GetStatistiekWeerType();
        }

        @Override
        protected void onPostExecute(ArrayList<StatistiekWeertype> stats) {
            StatsWeerTypeActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonStatistiekWeerType(stats);
        }
    }
}