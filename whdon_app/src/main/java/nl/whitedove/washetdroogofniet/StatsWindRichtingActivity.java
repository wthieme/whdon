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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.Locale;

public class StatsWindRichtingActivity extends Activity {
    static int mJaar = DateTime.now().getYear();
    static int mMaand = DateTime.now().getMonthOfYear();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.windrichting_statistieken);
        InitFab();
        InitRadio();
        InitSwipes();
        ToondataBackground();
    }

    private void InitFab() {
        FloatingActionButton fabTerug = findViewById(R.id.btnTerug);
        fabTerug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Terug();
            }
        });
    }

    private void InitRadio() {
        final RadioButton rbJaar = findViewById(R.id.rbJaar);
        final RadioButton rbJaarMaand = findViewById(R.id.rbJaarMaand);
        final RadioGroup rgJaarMaand = findViewById(R.id.rgJaarMaand);
        rbJaar.setChecked(mMaand == 0);
        rbJaarMaand.setChecked(mMaand != 0);
        RadioGroup.OnCheckedChangeListener cl = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                RadioButton rb = radioGroup.findViewById(checkedId);
                if (rb.getId() == R.id.rbJaar) mMaand = 0;
                else mMaand = DateTime.now().getMonthOfYear();
                ToondataBackground();
            }
        };
        rgJaarMaand.setOnCheckedChangeListener(cl);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void InitSwipes() {
        OnSwipeTouchListener sl = new OnSwipeTouchListener(StatsWindRichtingActivity.this) {
            public void onSwipeLeft() {
                if (mMaand == 0) {
                    mJaar++;
                } else {
                    mMaand++;
                    if (mMaand == 13) {
                        mJaar++;
                        mMaand = 1;
                    }
                }
                ToondataBackground();
            }

            public void onSwipeRight() {
                if (mMaand == 0) {
                    mJaar--;
                } else {
                    mMaand--;
                    if (mMaand == 0) {
                        mJaar--;
                        mMaand = 12;
                    }
                }
                ToondataBackground();
            }
        };

        final RelativeLayout rlPerMaand = findViewById(R.id.rlWindrichting);
        rlPerMaand.setOnTouchListener(sl);

        final RadarChart rChart = findViewById(R.id.rcPerWindrichting);
        rChart.setOnTouchListener(sl);

        Helper.ShowMessage(StatsWindRichtingActivity.this,

                getString(R.string.SwipeLinksOfRechts));
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
        if (mMaand == 0)
            tvWind.setText(String.format("%s %s", getString(R.string.PerWindRichting), String.format(getString(R.string.Jaartal), Integer.toString(mJaar))));
        else {
            DateTime dat = new DateTime(mJaar, mMaand, 1, 0, 0);
            String mnd = dat.toString("MMM", Locale.getDefault()).replace(".", "");
            tvWind.setText(String.format("%s %s", getString(R.string.PerWindRichting), String.format(getString(R.string.JaartalEnMaand), mnd, Integer.toString(mJaar))));
        }

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
        dsT.setLineWidth(2);

        RadarData data = new RadarData(dsT);
        data.setValueTextSize(14f);
        data.setValueTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        data.setDrawValues(false);

        chart.setData(data);
        chart.animateXY(500, 500);
        chart.invalidate();
    }

    private static class AsyncGetStatistiekWindTask extends AsyncTask<Context, Void, ArrayList<StatistiekWind>> {
        private WeakReference<StatsWindRichtingActivity> activityWeakReference;

        AsyncGetStatistiekWindTask(StatsWindRichtingActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<StatistiekWind> doInBackground(Context... params) {
            Context context = params[0];
            DatabaseHelper dh = DatabaseHelper.getInstance(context);
            return dh.GetStatistiekWind(mJaar, mMaand);
        }

        @Override
        protected void onPostExecute(ArrayList<StatistiekWind> stats) {
            StatsWindRichtingActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonStatistiekWind(stats);
        }
    }
}