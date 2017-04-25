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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.joda.time.DateTime;

import java.util.ArrayList;

public class StatsAantalGebruikersActivity extends Activity {
    DatabaseHelper mDH;
    static DateTime datum = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(), 0, 0).minusDays(29);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aantal_gebruikers_statistieken);

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
        final RelativeLayout rlAantalGebruikers = (RelativeLayout) findViewById(R.id.rlAantalGebruikers);

        rlAantalGebruikers.setOnTouchListener(new OnSwipeTouchListener(StatsAantalGebruikersActivity.this) {
            public void onSwipeLeft() {
                datum = datum.plusDays(30);
                ToondataBackground();
            }

            public void onSwipeRight() {
                datum = datum.minusDays(30);
                ToondataBackground();
            }
        });

        final LineChart chart = (LineChart) findViewById(R.id.lcAantalGebruikers);

        chart.setOnTouchListener(new OnSwipeTouchListener(StatsAantalGebruikersActivity.this) {
            public void onSwipeLeft() {
                datum = datum.plusDays(30);
                ToondataBackground();
            }

            public void onSwipeRight() {
                datum = datum.minusDays(30);
                ToondataBackground();
            }
        });

        Helper.ShowMessage(StatsAantalGebruikersActivity.this, getString(R.string.SwipeLinksOfRechts));
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
        new AsyncGetAantalGebruikersStatistiekenTask().execute();
    }

    @SuppressLint("DefaultLocale")
    private void ToonGetAantalGebruikersStatistieken(ArrayList<StatistiekAantalGebruikers> stats) {
        if (stats == null || stats.size() == 0) {
            return;
        }
        final TextView tvAantalGebruikersSubtitel = (TextView) findViewById(R.id.tvAantalGebruikersSubtitel);
        String vanaf = Helper.dFormat.print(datum);
        String tm = Helper.dFormat.print(datum.plusDays(29));
        tvAantalGebruikersSubtitel.setText(String.format(getString(R.string.vantmdatum), vanaf, tm));

        final LineChart chart = (LineChart) findViewById(R.id.lcAantalGebruikers);
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.setAutoScaleMinMaxEnabled(true);
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.setScaleEnabled(false);
        chart.setNoDataText(getString(R.string.nodata));

        int minVal = Integer.MAX_VALUE;
        int maxVal = Integer.MIN_VALUE;

        for (int i = 0; i < 30; i++) {
            int aantal = stats.get(i).getAantalGebruikers();
            if (aantal < minVal) minVal = aantal;
            if (aantal > maxVal) maxVal = aantal;
        }

        minVal = minVal - 1;
        if (minVal < 0) minVal = 0;
        int labelCount = maxVal - minVal + 2;
        while (labelCount > 10) labelCount = labelCount / 2;

        YAxis yAsL = chart.getAxisLeft();
        yAsL.setLabelCount(labelCount, true);
        yAsL.setAxisMinimum(minVal);
        yAsL.setAxisMaximum(maxVal + 1);
        YAxis yAsR = chart.getAxisRight();
        yAsR.setAxisMinimum(minVal);
        yAsR.setAxisMaximum(maxVal + 1);
        yAsR.setLabelCount(labelCount, true);

        XAxis xAs = chart.getXAxis();
        xAs.setDrawGridLines(false);
        xAs.setTextSize(10.0f);
        xAs.setLabelCount(30);

        ArrayList<Entry> dataY = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            Entry e = new Entry(i, stats.get(i).getAantalGebruikers());
            String sDatum = (i == 0 || i == 15 || i == 29) ? Helper.dmFormat.print(stats.get(i).getDatum()) : "";
            labels.add(sDatum);
            dataY.add(e);
        }

        xAs.setValueFormatter(new IndexAxisValueFormatter(labels));

        LineDataSet ds = new LineDataSet(dataY, "Cumulatief aantal gebruikers");
        ds.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        ds.setCircleColor(ContextCompat.getColor(this, R.color.colorNatStart));
        IValueFormatter myValueFormat = new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "";
            }
        };

        ds.setValueFormatter(myValueFormat);
        ds.setAxisDependency(YAxis.AxisDependency.LEFT);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(ds);
        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.animateXY(500, 500);
        chart.invalidate();
    }

    private class AsyncGetAantalGebruikersStatistiekenTask extends AsyncTask<Void, Void, ArrayList<StatistiekAantalGebruikers>> {

        @Override
        protected ArrayList<StatistiekAantalGebruikers> doInBackground(Void... params) {
            return mDH.GetAantalGebruikers30Dagen(datum);
        }

        @Override
        protected void onPostExecute(ArrayList<StatistiekAantalGebruikers> stats) {
            ToonGetAantalGebruikersStatistieken(stats);
        }
    }
}