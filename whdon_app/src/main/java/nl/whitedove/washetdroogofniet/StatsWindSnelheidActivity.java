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

public class StatsWindSnelheidActivity extends Activity {
    static int mJaar = DateTime.now().getYear();
    static int mMaand = DateTime.now().getMonthOfYear();
    static Helper.Periode mAllesJaarMaand = Helper.Periode.Maand;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.windsnelheid_statistieken);
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
        final RadioButton rbAlles = findViewById(R.id.rbAlles);
        final RadioButton rbJaar = findViewById(R.id.rbJaar);
        final RadioButton rbMaand = findViewById(R.id.rbMaand);
        final RadioGroup rgAllesJaarMaand = findViewById(R.id.rgAllesJaarMaand);
        rbAlles.setChecked(mAllesJaarMaand == Helper.Periode.Alles);
        rbJaar.setChecked(mAllesJaarMaand == Helper.Periode.Jaar);
        rbMaand.setChecked(mAllesJaarMaand == Helper.Periode.Maand);
        RadioGroup.OnCheckedChangeListener cl = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                RadioButton rb = radioGroup.findViewById(checkedId);
                if (rb.getId() == R.id.rbAlles) {
                    mAllesJaarMaand = Helper.Periode.Alles;
                }

                if (rb.getId() == R.id.rbJaar) {
                    mAllesJaarMaand = Helper.Periode.Jaar;
                    mJaar = DateTime.now().getYear();
                }

                if (rb.getId() == R.id.rbMaand) {
                    mAllesJaarMaand = Helper.Periode.Maand;
                    mMaand = DateTime.now().getMonthOfYear();
                }
                ToondataBackground();
            }
        };
        rgAllesJaarMaand.setOnCheckedChangeListener(cl);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void InitSwipes() {
        OnSwipeTouchListener sl = new OnSwipeTouchListener(StatsWindSnelheidActivity.this) {
            public void onSwipeLeft() {
                if (mAllesJaarMaand == Helper.Periode.Jaar) {
                    mJaar++;
                }

                if (mAllesJaarMaand == Helper.Periode.Maand) {
                    mMaand++;
                    if (mMaand == 13) {
                        mJaar++;
                        mMaand = 1;
                    }
                }
                ToondataBackground();
            }

            public void onSwipeRight() {
                if (mAllesJaarMaand == Helper.Periode.Jaar) {
                    mJaar--;
                }

                if (mAllesJaarMaand == Helper.Periode.Maand) {
                    mMaand--;
                    if (mMaand == 0) {
                        mJaar--;
                        mMaand = 12;
                    }
                }
                ToondataBackground();
            }
        };

        final RelativeLayout rlPerMaand = findViewById(R.id.rlWindsnelheid);
        rlPerMaand.setOnTouchListener(sl);

        final RadarChart rChart = findViewById(R.id.rcWindsnelheid);
        rChart.setOnTouchListener(sl);

        Helper.INSTANCE.showMessage(StatsWindSnelheidActivity.this,

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
        if (mAllesJaarMaand == Helper.Periode.Alles) {
            tvWind.setText(String.format("%s", getString(R.string.Windsnelheid)));
        }

        if (mAllesJaarMaand == Helper.Periode.Jaar) {
            tvWind.setText(String.format("%s %s", getString(R.string.Windsnelheid), String.format(getString(R.string.Jaartal), Integer.toString(mJaar))));
        }

        if (mAllesJaarMaand == Helper.Periode.Maand) {
            DateTime dat = new DateTime(mJaar, mMaand, 1, 0, 0);
            String mnd = dat.toString("MMM", Locale.getDefault()).replace(".", "");
            tvWind.setText(String.format("%s %s", getString(R.string.Windsnelheid), String.format(getString(R.string.JaartalEnMaand), mnd, Integer.toString(mJaar))));
        }

        Collections.sort(stats, StatsWindComparator.instance);

        ArrayList<RadarEntry> dataMn = new ArrayList<>();
        ArrayList<RadarEntry> dataA = new ArrayList<>();
        ArrayList<RadarEntry> dataMx = new ArrayList<>();

        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < stats.size(); i++) {
            dataMn.add(new RadarEntry(stats.get(i).getMinWindSpeed()));
            dataA.add(new RadarEntry(stats.get(i).getAvgWindSpeed()));
            dataMx.add(new RadarEntry(stats.get(i).getMaxWindSpeed()));
            labels.add(stats.get(i).getWindOmschrijving());
        }

        final RadarChart chart = findViewById(R.id.rcWindsnelheid);

        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);

        chart.setTouchEnabled(false);
        chart.setNoDataText(getString(R.string.nodata));
        YAxis rYAs = chart.getYAxis();
        rYAs.setDrawLabels(true);
        rYAs.setAxisMinimum(0f);

        XAxis rXAs = chart.getXAxis();
        rXAs.setDrawLabels(true);
        rXAs.setValueFormatter(new IndexAxisValueFormatter(labels));

        RadarDataSet dsMn = new RadarDataSet(dataMn, getString(R.string.MinWindSnelheid));
        dsMn.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        dsMn.setFillColor(ContextCompat.getColor(this, R.color.colorFillMin));
        dsMn.setDrawFilled(true);
        dsMn.setLineWidth(2);

        RadarDataSet dsA = new RadarDataSet(dataA, getString(R.string.GemmWindSnelheid));
        dsA.setColor(ContextCompat.getColor(this, R.color.colorAvg));
        dsA.setFillColor(ContextCompat.getColor(this, R.color.colorFillAvg));
        dsA.setDrawFilled(true);
        dsA.setLineWidth(2);

        RadarDataSet dsMx = new RadarDataSet(dataMx, getString(R.string.MaxWindSnelheid));
        dsMx.setColor(ContextCompat.getColor(this, R.color.colorMax));
        dsMx.setFillColor(ContextCompat.getColor(this, R.color.colorFillMax));
        dsMx.setDrawFilled(true);
        dsMx.setLineWidth(2);

        RadarData data = new RadarData(dsMn, dsA, dsMx);
        data.setValueTextSize(14f);
        data.setValueTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        data.setDrawValues(false);

        chart.setData(data);
        chart.animateXY(500, 500);
        chart.invalidate();
    }

    private static class AsyncGetStatistiekWindTask extends AsyncTask<Context, Void, ArrayList<StatistiekWind>> {
        private WeakReference<StatsWindSnelheidActivity> activityWeakReference;

        AsyncGetStatistiekWindTask(StatsWindSnelheidActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<StatistiekWind> doInBackground(Context... params) {
            Context context = params[0];
            DatabaseHelper dh = DatabaseHelper.Companion.getInstance(context);
            return dh.GetStatistiekWind(mAllesJaarMaand, mJaar, mMaand);
        }

        @Override
        protected void onPostExecute(ArrayList<StatistiekWind> stats) {
            StatsWindSnelheidActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonStatistiekWind(stats);
        }
    }
}