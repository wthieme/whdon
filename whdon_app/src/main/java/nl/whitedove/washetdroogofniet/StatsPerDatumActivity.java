package nl.whitedove.washetdroogofniet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import org.joda.time.DateTime;

import java.util.ArrayList;

public class StatsPerDatumActivity extends Activity {
    DatabaseHelper mDH;
    static DateTime datum = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(), 0, 0).minusDays(29);

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
        InitSwipes();
        ToondataBackground();
    }

    private void InitSwipes() {
        final RelativeLayout rlPerDatum = (RelativeLayout) findViewById(R.id.rlPerDatum);

        rlPerDatum.setOnTouchListener(new OnSwipeTouchListener(StatsPerDatumActivity.this) {
            public void onSwipeLeft() {
                datum = datum.plusDays(30);
                ToondataBackground();
            }

            public void onSwipeRight() {
                datum = datum.minusDays(30);
                ToondataBackground();
            }
        });

        final BarChart chart = (BarChart) findViewById(R.id.bcPerDatum);

        chart.setOnTouchListener(new OnSwipeTouchListener(StatsPerDatumActivity.this) {
            public void onSwipeLeft() {
                datum = datum.plusDays(30);
                ToondataBackground();
            }

            public void onSwipeRight() {
                datum = datum.minusDays(30);
                ToondataBackground();
            }
        });

        Helper.ShowMessage(StatsPerDatumActivity.this, getString(R.string.SwipeLinksOfRechts));
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
        final TextView tvDatumSubtitel = (TextView) findViewById(R.id.tvDatumSubtitel);
        String vanaf = Helper.dFormat.print(datum);
        String tm = Helper.dFormat.print(datum.plusDays(29));
        tvDatumSubtitel.setText(String.format(getString(R.string.vantmdatum), vanaf, tm));

        final BarChart chart = (BarChart) findViewById(R.id.bcPerDatum);
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.setAutoScaleMinMaxEnabled(true);
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.setNoDataText(getString(R.string.nodata));
        chart.setScaleEnabled(false);
        chart.setFitBars(true);
        int max = 0;
        for (int i = 0; i < 30; i++) {
            int aantal = stats.get(i).getAantalNat() + stats.get(i).getAantalDroog();
            if (aantal > max) max = aantal;
        }

        YAxis lAs = chart.getAxisLeft();
        lAs.setAxisMinimum(0);
        lAs.setAxisMaximum(max + 1);
        if (max < 8) lAs.setLabelCount(max + 1);

        YAxis rAs = chart.getAxisRight();
        rAs.setAxisMinimum(0);
        rAs.setAxisMaximum(max + 1);
        if (max < 8) rAs.setLabelCount(max + 1);

        XAxis xAs = chart.getXAxis();
        xAs.setDrawGridLines(false);
        xAs.setTextSize(10.0f);
        xAs.setLabelCount(30);

        ArrayList<BarEntry> dataT = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            dataT.add(new BarEntry(i, new float[]{stats.get(i).getAantalNat(), stats.get(i).getAantalDroog()}));
            String sDatum = (i == 0 || i == 15 || i == 29) ? Helper.dmFormat.print(stats.get(i).getDatum()) : "";
            labels.add(sDatum);
        }

        xAs.setValueFormatter(new IndexAxisValueFormatter(labels));

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
            return mDH.GetStatistiek30Dagen(datum);
        }

        @Override
        protected void onPostExecute(ArrayList<Statistiek1Dag> stats) {
            ToonStatistiekenPerDag(stats);
        }

    }
}