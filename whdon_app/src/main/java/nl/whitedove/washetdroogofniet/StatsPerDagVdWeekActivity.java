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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class StatsPerDagVdWeekActivity extends Activity {
    static int mJaar = DateTime.now().getYear();
    static int mMaand = DateTime.now().getMonthOfYear();
    static Helper.Periode mAllesJaarMaand = Helper.Periode.Maand;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.per_dag_vd_week_statistieken);
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
        OnSwipeTouchListener sl = new OnSwipeTouchListener(StatsPerDagVdWeekActivity.this) {
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

        final RelativeLayout rlPerDagVdWeek = findViewById(R.id.rlPerDagVdWeek);
        rlPerDagVdWeek.setOnTouchListener(sl);

        final BarChart bcPerDagVdWeek = findViewById(R.id.bcPerDagVdWeek);
        bcPerDagVdWeek.setOnTouchListener(sl);

        Helper.ShowMessage(StatsPerDagVdWeekActivity.this, getString(R.string.SwipeLinksOfRechts));
    }

    private void Terug() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void ToondataBackground() {
        Context context = getApplicationContext();
        new AsyncGetStatistiekenPerDagVdWeekTask(this).execute(context);
    }

    private void ToonStatistiekenPerDagVdWeek(ArrayList<StatistiekDagVdWeek> stats) {
        final TextView tvDagVdWeekTitel = findViewById(R.id.tvDagVdWeekTitel);

        if (mAllesJaarMaand == Helper.Periode.Alles) {
            tvDagVdWeekTitel.setText(String.format("%s", getString(R.string.AantalPerDagVdWeek)));
        }

        if (mAllesJaarMaand == Helper.Periode.Jaar) {
            tvDagVdWeekTitel.setText(String.format("%s %s", getString(R.string.AantalPerDagVdWeek), String.format(getString(R.string.Jaartal), Integer.toString(mJaar))));
        }

        if (mAllesJaarMaand == Helper.Periode.Maand) {
            DateTime dat = new DateTime(mJaar, mMaand, 1, 0, 0);
            String mnd = dat.toString("MMM", Locale.getDefault()).replace(".", "");
            tvDagVdWeekTitel.setText(String.format("%s %s", getString(R.string.AantalPerDagVdWeek), String.format(getString(R.string.JaartalEnMaand), mnd, Integer.toString(mJaar))));
        }

        final BarChart chart = findViewById(R.id.bcPerDagVdWeek);
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
        xAs.setLabelCount(7);

        int max = 0;
        for (int i = 0; i < stats.size(); i++) {
            int aantal = stats.get(i).getAantalNat() + stats.get(i).getAantalDroog();
            if (aantal > max) max = aantal;
        }

        YAxis bLAs = chart.getAxisLeft();
        bLAs.setAxisMinimum(0);
        bLAs.setAxisMaximum(max + 1);
        if (max < 8) bLAs.setLabelCount(max + 1);
        else bLAs.setLabelCount(6);

        YAxis bRAs = chart.getAxisRight();
        bRAs.setAxisMinimum(0);
        bRAs.setAxisMaximum(max + 1);
        if (max < 8) bRAs.setLabelCount(max + 1);
        else bRAs.setLabelCount(6);

        ArrayList<BarEntry> dataT = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>(Arrays.asList("Ma", "Di", "Wo", "Do", "Vr", "Za", "Zo"));
        //Initialize with zero's
        for (int i = 0; i < 7; i++) {
            dataT.add(new BarEntry(i, new float[]{0, 0}));
        }

        for (int i = 0; i < stats.size(); i++) {
            int dag = stats.get(i).getDag();
            int aantalNat = stats.get(i).getAantalNat();
            int aantalDroog = stats.get(i).getAantalDroog();
            dataT.get(dag).setVals(new float[]{aantalNat, aantalDroog});
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

    private static class AsyncGetStatistiekenPerDagVdWeekTask extends AsyncTask<Context, Void, ArrayList<StatistiekDagVdWeek>> {

        private WeakReference<StatsPerDagVdWeekActivity> activityWeakReference;

        AsyncGetStatistiekenPerDagVdWeekTask(StatsPerDagVdWeekActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<StatistiekDagVdWeek> doInBackground(Context... params) {
            Context context = params[0];
            DatabaseHelper dh = DatabaseHelper.getInstance(context);
            return dh.GetStatistiekDagVdWeek(mAllesJaarMaand, mJaar, mMaand);
        }

        @Override
        protected void onPostExecute(ArrayList<StatistiekDagVdWeek> stats) {
            StatsPerDagVdWeekActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonStatistiekenPerDagVdWeek(stats);
        }
    }
}