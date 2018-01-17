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

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.joda.time.DateTime;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

public class StatsWindActivity extends Activity {
    static int mJaar = DateTime.now().getYear();

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
        InitSwipes();
        ToondataBackground();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void InitSwipes() {
        OnSwipeTouchListener sl = new OnSwipeTouchListener(StatsWindActivity.this) {
            public void onSwipeLeft() {
                mJaar++;
                ToondataBackground();
            }

            public void onSwipeRight() {
                mJaar--;
                ToondataBackground();
            }
        };

        final RelativeLayout rlPerMaand = findViewById(R.id.rlWindrichting);
        rlPerMaand.setOnTouchListener(sl);

        final RadarChart rChart = findViewById(R.id.rcPerWindrichting);
        rChart.setOnTouchListener(sl);

        Helper.ShowMessage(StatsWindActivity.this, getString(R.string.SwipeLinksOfRechts));
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

    private void ToonStatistiekWind(ArrayList<StatistiekWind> stats) {
        final TextView tvWind = findViewById(R.id.tvWind);
        tvWind.setText(String.format("%s %s", getString(R.string.PerWindRichting), String.format(getString(R.string.Jaartal), Integer.toString(mJaar))));

        Collections.sort(stats, StatsWindComparator.instance);

        ArrayList<RadarEntry> dataT = new ArrayList<>();

        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < stats.size(); i++) {
            dataT.add(new RadarEntry(stats.get(i).getPercentage()));
            labels.add(stats.get(i).getWindOmschrijving());
        }

        final RadarChart chart = findViewById(R.id.rcPerWindrichting);

        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);

        chart.setTouchEnabled(false);
        chart.setNoDataText(getString(R.string.nodata));
        chart.getLegend().setEnabled(false);
        YAxis rYAs = chart.getYAxis();
        rYAs.setDrawLabels(true);
        rYAs.setAxisMinimum(0f);

        XAxis rXAs = chart.getXAxis();
        rXAs.setDrawLabels(true);
        rXAs.setValueFormatter(new IndexAxisValueFormatter(labels));

        RadarDataSet dsT = new RadarDataSet(dataT, "");
        dsT.setColor(ContextCompat.getColor(this, R.color.colorTekst));
        dsT.setDrawFilled(true);

        RadarData data = new RadarData(dsT);
        data.setValueTextSize(14f);
        data.setValueTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        data.setDrawValues(false);

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
            return dh.GetStatistiekWind(mJaar);
        }

        @Override
        protected void onPostExecute(ArrayList<StatistiekWind> stats) {
            StatsWindActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonStatistiekWind(stats);
        }
    }
}