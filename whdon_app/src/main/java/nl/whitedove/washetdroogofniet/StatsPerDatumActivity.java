package nl.whitedove.washetdroogofniet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;

public class StatsPerDatumActivity extends Activity {
    DatabaseHelper mDH;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.per_datum_statistieken);

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
        new AsyncGetStatistiekenTask().execute();
    }

    @SuppressLint("DefaultLocale")
    private void ToonStatistiekenPerDag(ArrayList<Statistiek1Dag> stats) {
        if (stats == null || stats.size() == 0) {
            return;
        }
        final BarChart chart = (BarChart) findViewById(R.id.bcPerdag);
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.setAutoScaleMinMaxEnabled(true);
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.setNoDataText(getString(R.string.nodata));
        chart.setScaleEnabled(false);
        XAxis xAs1 = chart.getXAxis();
        xAs1.setDrawGridLines(false);

        ArrayList<BarEntry> dataT = new ArrayList<>();

        for (int i = 0; i < 31; i++) {
            dataT.add(new BarEntry(-1 * stats.get(i).getDag(), new float[]{stats.get(i).getAantalNat(), stats.get(i).getAantalDroog()}));
        }

        BarDataSet dsT = new BarDataSet(dataT, "");
        dsT.setStackLabels(new String[]{"Nat", "Droog"});

        dsT.setColors(ContextCompat.getColor(this, R.color.colorNatStart), ContextCompat.getColor(this, R.color.colorDroogStart));
        IValueFormatter myValueFormat = new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "";
            }
        };

        dsT.setValueFormatter(myValueFormat);
        dsT.setAxisDependency(YAxis.AxisDependency.LEFT);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dsT);

        BarData data = new BarData(dataSets);
        chart.setData(data);
        chart.animateXY(500, 500);
        chart.invalidate();
    }

    private class AsyncGetStatistiekenTask extends AsyncTask<Void, Void, ArrayList<Statistiek1Dag>> {

        @Override
        protected ArrayList<Statistiek1Dag> doInBackground(Void... params) {
            return mDH.GetStatistiek30Dagen();
        }

        @Override
        protected void onPostExecute(ArrayList<Statistiek1Dag> stats) {
            ToonStatistiekenPerDag(stats);
        }

    }
}