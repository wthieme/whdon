package nl.whitedove.washetdroogofniet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Locale;

public class StatsPerMaandActivity extends Activity {
    DatabaseHelper mDH;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.per_maand_statistieken);

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
        new AsyncGetStatistiekenTask().execute();
    }

    private void ToonStatistiekenPerMaand(ArrayList<Statistiek1Maand> stats) {
        if (stats == null || stats.size() == 0) {
            return;
        }
        final BarChart chart = (BarChart) findViewById(R.id.bcPerMaand);
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.setDescription("");
        chart.setNoDataText(getString(R.string.nodata));
        chart.setScaleEnabled(false);
        XAxis xAs1 = chart.getXAxis();
        xAs1.setLabelsToSkip(0);
        xAs1.setDrawGridLines(false);

        ArrayList<BarEntry> dataT = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            int aantalNat = stats.get(i).getAantalNat();
            int aantalDroog = stats.get(i).getAantalDroog();
            int totaal = aantalNat + aantalDroog;
            int percDroog = 0;
            int percNat = 0;

            if (totaal > 0) {
                percDroog = Math.round(100.0F * aantalDroog / totaal);
                percNat = 100 - percDroog;
            }

            dataT.add(new BarEntry(new float[]{percNat, percDroog}, i));
            DateTime datum = new DateTime(2000, stats.get(i).getMaand(), 1, 0, 0);
            xVals.add(datum.toString("MMM", Locale.getDefault()));
        }

        BarDataSet dsT = new BarDataSet(dataT, "");
        dsT.setStackLabels(new String[]{"Nat", "Droog"});

        dsT.setColors(new int[]{ContextCompat.getColor(this, R.color.colorNatStart), ContextCompat.getColor(this, R.color.colorDroogStart)});
        ValueFormatter myformat = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "";
            }
        };

        dsT.setValueFormatter(myformat);
        dsT.setAxisDependency(YAxis.AxisDependency.LEFT);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dsT);

        BarData data = new BarData(xVals, dataSets);
        chart.setData(data);
        chart.animateXY(500, 500);
        chart.invalidate();
    }

    private class AsyncGetStatistiekenTask extends AsyncTask<Void, Void, ArrayList<Statistiek1Maand>> {

        @Override
        protected ArrayList<Statistiek1Maand> doInBackground(Void... params) {
            return mDH.GetStatistiek12Maanden();
        }

        @Override
        protected void onPostExecute(ArrayList<Statistiek1Maand> stats) {
            ToonStatistiekenPerMaand(stats);
        }

    }
}