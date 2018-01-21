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

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.joda.time.DateTime;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

public class StatsWeerTypeActivity extends Activity {
    static int mJaar = DateTime.now().getYear();
    static int mMaand = DateTime.now().getMonthOfYear();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weertype_statistieken);
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
        OnSwipeTouchListener sl = new OnSwipeTouchListener(StatsWeerTypeActivity.this) {
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

        final RelativeLayout rlPerMaand = findViewById(R.id.rlPerWeertype);
        rlPerMaand.setOnTouchListener(sl);

        final PieChart pChart = findViewById(R.id.pcPerWeertype);
        pChart.setOnTouchListener(sl);

        Helper.ShowMessage(StatsWeerTypeActivity.this, getString(R.string.SwipeLinksOfRechts));
    }

    private void Terug() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void ToondataBackground() {
        Context context = getApplicationContext();
        new AsyncGetStatistiekWeerTypeTask(this).execute(context);
    }

    @SuppressLint("DefaultLocale")
    private void ToonStatistiekWeerType(ArrayList<StatistiekWeertype> stats) {

        final TextView tvWeertype = findViewById(R.id.tvWeertype);
        final TextView tvGeenGegevens = findViewById(R.id.tvGeenGegevens);

        if (mMaand == 0)
            tvWeertype.setText(String.format("%s %s", getString(R.string.PerWeerType), String.format(getString(R.string.Jaartal), Integer.toString(mJaar))));
        else {
            DateTime dat = new DateTime(mJaar, mMaand, 1, 0, 0);
            String mnd = dat.toString("MMM", Locale.getDefault()).replace(".", "");
            tvWeertype.setText(String.format("%s %s", getString(R.string.PerWeerType), String.format(getString(R.string.JaartalEnMaand), mnd, Integer.toString(mJaar))));
        }

        ArrayList<Integer> colors = new ArrayList<>();
        ArrayList<PieEntry> dataT = new ArrayList<>();
        ArrayList<LegendEntry> legendEntries = new ArrayList<>();

        for (int i = 0; i < stats.size(); i++) {
            float perc = stats.get(i).getPercentage();
            int rPerc = Math.round(perc);
            dataT.add(new PieEntry(perc, (rPerc <= 2) ? "" : String.format("%d%%", rPerc)));
            LegendEntry le = new LegendEntry();
            Integer col = WeerHelper.WeerTypeToWeerKleur(this, stats.get(i).getWeerType());
            colors.add(col);
            //noinspection ConstantConditions
            le.formColor = col;
            le.label = stats.get(i).getWeerTypeOmschrijving();
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
        chart.setEntryLabelColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        chart.setDrawEntryLabels(true);

        Legend legend = chart.getLegend();
        legend.setCustom(legendEntries);
        legend.setEnabled(true);
        legend.setXEntrySpace(20f);
        legend.setTextSize(12f);
        legend.setWordWrapEnabled(true);

        PieDataSet dsT = new PieDataSet(dataT, "");
        dsT.setColors(colors);
        dsT.setDrawValues(false);

        PieData data = new PieData(dsT);
        data.setValueTextSize(14f);
        data.setValueTextColor(ContextCompat.getColor(this, R.color.colorTekst));

        chart.setData(data);
        chart.animateXY(500, 500);
        if (stats.size() == 0) {
            chart.setVisibility(View.GONE);
            tvGeenGegevens.setVisibility(View.VISIBLE);
        } else {
            tvGeenGegevens.setVisibility(View.GONE);
            chart.setVisibility(View.VISIBLE);
        }
        chart.invalidate();

    }

    private static class AsyncGetStatistiekWeerTypeTask extends AsyncTask<Context, Void, ArrayList<StatistiekWeertype>> {
        private WeakReference<StatsWeerTypeActivity> activityWeakReference;

        AsyncGetStatistiekWeerTypeTask(StatsWeerTypeActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<StatistiekWeertype> doInBackground(Context... params) {
            Context context = params[0];
            DatabaseHelper dh = DatabaseHelper.getInstance(context);
            return dh.GetStatistiekWeerType(mJaar, mMaand);
        }

        @Override
        protected void onPostExecute(ArrayList<StatistiekWeertype> stats) {
            StatsWeerTypeActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonStatistiekWeerType(stats);
        }
    }
}