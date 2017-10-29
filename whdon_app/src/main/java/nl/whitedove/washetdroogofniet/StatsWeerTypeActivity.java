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
        setContentView(R.layout.weertype_statistieken);

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
        mDH = new DatabaseHelper(getApplicationContext());
    }

    private void Terug() {
        Intent intent = new Intent(this, MainActivity.class);
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

        int[] colors = new int[]{ContextCompat.getColor(this, R.color.colorGrafiek1),
                ContextCompat.getColor(this, R.color.colorGrafiek2),
                ContextCompat.getColor(this, R.color.colorGrafiek3),
                ContextCompat.getColor(this, R.color.colorGrafiek4),
                ContextCompat.getColor(this, R.color.colorGrafiek5),
                ContextCompat.getColor(this, R.color.colorGrafiek6),
                ContextCompat.getColor(this, R.color.colorGrafiek7),
                ContextCompat.getColor(this, R.color.colorGrafiek8)};

        ArrayList<PieEntry> dataT = new ArrayList<>();
        ArrayList<LegendEntry> legendEntries = new ArrayList<>();

        for (int i = 0; i < stats.size(); i++) {
            dataT.add(new PieEntry(stats.get(i).getPercentage(), String.format("%d%%", stats.get(i).getPercentage())));
            LegendEntry le = new LegendEntry();
            le.formColor = colors[i];
            le.label = stats.get(i).getWeerTypeOschrijving();
            le.form = Legend.LegendForm.SQUARE;
            le.formSize = 10;
            legendEntries.add(le);
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
        dsT.setColors(colors);
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