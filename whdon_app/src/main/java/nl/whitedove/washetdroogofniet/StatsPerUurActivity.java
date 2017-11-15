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
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class StatsPerUurActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.per_uur_statistieken);

        FloatingActionButton fabTerug = findViewById(R.id.btnTerug);
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
    }

    private void Terug() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void ToondataBackground() {
        Context context = getApplicationContext();
        new AsyncGetStatistiekenPerUurTask(this).execute(context);
    }

    private void ToonStatistiekenPerUur(ArrayList<Statistiek1Uur> stats) {
        if (stats == null || stats.size() == 0) {
            return;
        }
        final BarChart chart = findViewById(R.id.bcPerUur);
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.setAutoScaleMinMaxEnabled(true);
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.setNoDataText(getString(R.string.nodata));
        chart.setScaleEnabled(false);
        XAxis xAs = chart.getXAxis();
        xAs.setDrawGridLines(false);
        xAs.setDrawLabels(true);
        xAs.setDrawAxisLine(false);
        xAs.setYOffset(5.0f);
        xAs.setTextSize(10.0f);
        xAs.setLabelCount(24);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setAxisMinimum(0f);

        ArrayList<BarEntry> dataT = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        //Initialize with zero's
        for (int i = 0; i < 24; i++) {
            dataT.add(new BarEntry(i, new float[]{0, 0}));
            labels.add(Integer.toString(i));
        }

        for (int i = 0; i < stats.size(); i++) {
            int uur = stats.get(i).getUur();
            int aantalNat = stats.get(i).getAantalNat();
            int aantalDroog = stats.get(i).getAantalDroog();
            dataT.get(uur).setVals(new float[]{aantalNat, aantalDroog});
        }

        xAs.setValueFormatter(new IndexAxisValueFormatter(labels));

        BarDataSet dsT = new BarDataSet(dataT, "");
        dsT.setStackLabels(new String[]{this.getString(R.string.NatTxt), this.getString(R.string.DroogTxt)});
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

    private static class AsyncGetStatistiekenPerUurTask extends AsyncTask<Context, Void, ArrayList<Statistiek1Uur>> {

        private WeakReference<StatsPerUurActivity> activityWeakReference;

        AsyncGetStatistiekenPerUurTask(StatsPerUurActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<Statistiek1Uur> doInBackground(Context... params) {
            Context context = params[0];
            DatabaseHelper dh = DatabaseHelper.getInstance(context);
            return dh.GetStatistiek24Uur();
        }

        @Override
        protected void onPostExecute(ArrayList<Statistiek1Uur> stats) {
            StatsPerUurActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonStatistiekenPerUur(stats);
        }
    }
}