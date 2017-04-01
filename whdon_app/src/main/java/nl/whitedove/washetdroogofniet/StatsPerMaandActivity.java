package nl.whitedove.washetdroogofniet;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
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
import java.util.Locale;

public class StatsPerMaandActivity extends Activity {
    DatabaseHelper mDH;
    static int jaar = DateTime.now().getYear();

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
        InitSwipes();
        ToondataBackground();
    }

    private void InitSwipes() {
        final BarChart chart = (BarChart) findViewById(R.id.bcPerMaand);

        chart.setOnTouchListener(new OnSwipeTouchListener(StatsPerMaandActivity.this) {
            public void onSwipeLeft() {
                jaar++;
                ToondataBackground();
            }

            public void onSwipeRight() {
                jaar--;
                ToondataBackground();
            }
        });
        Helper.ShowMessage(StatsPerMaandActivity.this, getString(R.string.SwipeLinksOfRechts));
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

    private void ToonStatistiekenPerMaand(ArrayList<Statistiek1Maand> stats) {
        if (stats == null || stats.size() == 0) {
            return;
        }
        final TextView tvMaandTitel = (TextView) findViewById(R.id.tvMaandTitel);
        tvMaandTitel.setText(getString(R.string.per_maand) + " " + Integer.toString(jaar));
        final BarChart chart = (BarChart) findViewById(R.id.bcPerMaand);
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
        xAs.setLabelCount(12);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setLabelCount(6, true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setLabelCount(6, true);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(100f);

        ArrayList<BarEntry> dataT = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

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

            DateTime datum = new DateTime(2000, stats.get(i).getMaand(), 1, 0, 0);
            dataT.add(new BarEntry(i, new float[]{percNat, percDroog}));
            labels.add(datum.toString("MMM", Locale.getDefault()));
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

    private class AsyncGetStatistiekenTask extends AsyncTask<Void, Void, ArrayList<Statistiek1Maand>> {

        @Override
        protected ArrayList<Statistiek1Maand> doInBackground(Void... params) {
            return mDH.GetStatistiek12Maanden(jaar);
        }

        @Override
        protected void onPostExecute(ArrayList<Statistiek1Maand> stats) {
            ToonStatistiekenPerMaand(stats);
        }
    }
}