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
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.joda.time.DateTime;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class StatsAantalGebruikersActivity extends Activity {
    static DateTime datum = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(), 0, 0).minusDays(29);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aantal_gebruikers_statistieken);

        FloatingActionButton fabTerug = findViewById(R.id.btnTerug);
        fabTerug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Terug();
            }
        });
        InitSwipes();
        ToondataBackground();
    }

    private void InitSwipes() {
        OnSwipeTouchListener sl = new OnSwipeTouchListener(StatsAantalGebruikersActivity.this) {
            public void onSwipeLeft() {
                datum = datum.plusDays(30);
                ToondataBackground();
            }

            public void onSwipeRight() {
                datum = datum.minusDays(30);
                ToondataBackground();
            }
        };

        final RelativeLayout rlAantalGebruikers = findViewById(R.id.rlAantalGebruikers);
        rlAantalGebruikers.setOnTouchListener(sl);

        final LineChart chart = findViewById(R.id.lcAantalGebruikers);
        chart.setOnTouchListener(sl);

        Helper.INSTANCE.showMessage(StatsAantalGebruikersActivity.this, getString(R.string.SwipeLinksOfRechts));
    }

    private void Terug() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void ToondataBackground() {
        Context context = getApplicationContext();
        new AsyncGetAantalGebruikersStatistiekenTask(this).execute(context);
    }

    @SuppressLint("DefaultLocale")
    private void ToonGetAantalGebruikersStatistieken(ArrayList<StatistiekAantalGebruikers> stats) {
        if (stats == null || stats.size() == 0) {
            return;
        }
        final TextView tvAantalGebruikersSubtitel = findViewById(R.id.tvAantalGebruikersSubtitel);
        String vanaf = Helper.INSTANCE.getDFormat().print(datum);
        String tm = Helper.INSTANCE.getDFormat().print(datum.plusDays(29));
        tvAantalGebruikersSubtitel.setText(String.format(getString(R.string.vantmdatum), vanaf, tm));

        final LineChart lChart = findViewById(R.id.lcAantalGebruikers);
        lChart.setHighlightPerTapEnabled(false);
        lChart.setHighlightPerDragEnabled(false);
        lChart.setAutoScaleMinMaxEnabled(true);
        Description desc = new Description();
        desc.setText("");
        lChart.setDescription(desc);
        lChart.setScaleEnabled(false);
        lChart.setNoDataText(getString(R.string.nodata));

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

        YAxis yAsL = lChart.getAxisLeft();
        yAsL.setLabelCount(labelCount, true);
        yAsL.setAxisMinimum(minVal);
        yAsL.setAxisMaximum(maxVal + 1);
        YAxis yAsR = lChart.getAxisRight();
        yAsR.setAxisMinimum(minVal);
        yAsR.setAxisMaximum(maxVal + 1);
        yAsR.setLabelCount(labelCount, true);

        XAxis xAs = lChart.getXAxis();
        xAs.setDrawGridLines(false);
        xAs.setTextSize(10.0f);
        xAs.setLabelCount(30);

        ArrayList<Entry> dataY = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            Entry e = new Entry(i, stats.get(i).getAantalGebruikers());
            String sDatum = (i == 0 || i == 10 || i == 20 || i == 29) ? Helper.INSTANCE.getDmFormat().print(stats.get(i).getDatum()) : "";
            labels.add(sDatum);
            dataY.add(e);
        }

        xAs.setValueFormatter(new IndexAxisValueFormatter(labels));

        LineDataSet ds = new LineDataSet(dataY, "Cumulatief aantal gebruikers");
        ds.setColor(ContextCompat.getColor(this, R.color.colorTemperatuurDark));
        ds.setCircleColor(ContextCompat.getColor(this, R.color.colorTemperatuur));
        ds.setCircleColorHole(ContextCompat.getColor(this, R.color.colorTemperatuur));
        ds.setCircleRadius(2.5f);
        ds.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        ds.setCubicIntensity(0.2f);
        IValueFormatter myValueFormat = new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "";
            }
        };

        ds.setValueFormatter(myValueFormat);
        ds.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineData data = new LineData(ds);
        lChart.setData(data);
        lChart.animateXY(500, 500);
        lChart.invalidate();
    }

    private static class AsyncGetAantalGebruikersStatistiekenTask extends AsyncTask<Context, Void, ArrayList<StatistiekAantalGebruikers>> {
        private WeakReference<StatsAantalGebruikersActivity> activityWeakReference;

        AsyncGetAantalGebruikersStatistiekenTask(StatsAantalGebruikersActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<StatistiekAantalGebruikers> doInBackground(Context... params) {
            Context context = params[0];
            DatabaseHelper dh = DatabaseHelper.Companion.getInstance(context);
            return dh.GetAantalGebruikers30Dagen(datum);
        }

        @Override
        protected void onPostExecute(ArrayList<StatistiekAantalGebruikers> stats) {
            StatsAantalGebruikersActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonGetAantalGebruikersStatistieken(stats);
        }
    }
}