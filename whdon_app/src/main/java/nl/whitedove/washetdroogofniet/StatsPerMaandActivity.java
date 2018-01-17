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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.joda.time.DateTime;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatsPerMaandActivity extends Activity {
    static int mJaar = DateTime.now().getYear();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.per_maand_statistieken);

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
        OnSwipeTouchListener sl = new OnSwipeTouchListener(StatsPerMaandActivity.this) {
            public void onSwipeLeft() {
                mJaar++;
                ToondataBackground();
            }

            public void onSwipeRight() {
                mJaar--;
                ToondataBackground();
            }
        };

        final RelativeLayout rlPerMaand = findViewById(R.id.rlPerMaand);
        rlPerMaand.setOnTouchListener(sl);

        final BarChart bChart = findViewById(R.id.bcPerMaand);
        bChart.setOnTouchListener(sl);

        final LineChart lChart = findViewById(R.id.lcPerMaand);
        lChart.setOnTouchListener(sl);

        Helper.ShowMessage(StatsPerMaandActivity.this, getString(R.string.SwipeLinksOfRechts));
    }

    private void Terug() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void ToondataBackground() {
        Context context = getApplicationContext();
        new AsyncGetStatistiekenPerMaandTask(this).execute(context);
    }

    @SuppressLint("SetTextI18n")
    private void ToonStatistiekenPerMaand(ArrayList<Statistiek1Maand> stats) {
        if (stats == null || stats.size() == 0) {
            return;
        }
        final TextView tvMaandTitel = findViewById(R.id.tvMaandTitel);
        int maand = DateTime.now().getMonthOfYear();
        DateTime vanaf;

        if (maand == 12)
            vanaf = new DateTime(mJaar, 1, 1, 0, 0);
        else
            vanaf = new DateTime(mJaar - 1, maand + 1, 1, 0, 0);

        String maandva = vanaf.toString("MMM", Locale.getDefault()).replace(".","");
        String maandtm = vanaf.plusMonths(11).toString("MMM", Locale.getDefault()).replace(".","");

        tvMaandTitel.setText(String.format(getString(R.string.per_maand_titel),
                maandva,
                Integer.toString(mJaar - 1),
                maandtm,
                Integer.toString(mJaar)));
        final BarChart bChart = findViewById(R.id.bcPerMaand);
        bChart.setHighlightPerTapEnabled(false);
        bChart.setHighlightPerDragEnabled(false);
        bChart.setAutoScaleMinMaxEnabled(true);
        Description bDesc = new Description();
        bDesc.setText("");
        bChart.setDescription(bDesc);
        bChart.setNoDataText(getString(R.string.nodata));
        bChart.setScaleEnabled(false);
        XAxis bXAs = bChart.getXAxis();
        bXAs.setDrawGridLines(false);
        bXAs.setDrawLabels(true);
        bXAs.setDrawAxisLine(false);
        bXAs.setYOffset(5.0f);
        bXAs.setTextSize(10.0f);
        bXAs.setLabelCount(12);

        YAxis leftAxis = bChart.getAxisLeft();
        leftAxis.setLabelCount(6, true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);

        YAxis rightAxis = bChart.getAxisRight();
        rightAxis.setLabelCount(6, true);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(100f);

        ArrayList<BarEntry> bDataT = new ArrayList<>();
        ArrayList<String> bLabels = new ArrayList<>();

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
            bDataT.add(new BarEntry(i, new float[]{percNat, percDroog}));
            bLabels.add(datum.toString("MMM", Locale.getDefault()));
        }

        bXAs.setValueFormatter(new IndexAxisValueFormatter(bLabels));

        BarDataSet bDsT = new BarDataSet(bDataT, "");
        bDsT.setStackLabels(new String[]{this.getString(R.string.NatTxt), this.getString(R.string.DroogTxt)});
        bDsT.setColors(ContextCompat.getColor(this, R.color.colorNatStart), ContextCompat.getColor(this, R.color.colorDroogStart));

        IValueFormatter bValueFormat = new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "";
            }
        };

        bDsT.setValueFormatter(bValueFormat);
        bDsT.setAxisDependency(YAxis.AxisDependency.LEFT);

        ArrayList<IBarDataSet> bDataSets = new ArrayList<>();
        bDataSets.add(bDsT);
        BarData data = new BarData(bDataSets);
        bChart.setData(data);
        bChart.animateXY(500, 500);
        bChart.invalidate();

        // Gemmiddelde temperatuur per maand
        final LineChart lChart = findViewById(R.id.lcPerMaand);
        lChart.setHighlightPerTapEnabled(false);
        lChart.setHighlightPerDragEnabled(false);
        lChart.setAutoScaleMinMaxEnabled(true);
        Description lDesc = new Description();
        lDesc.setText("");
        lChart.setDescription(lDesc);
        lChart.setScaleEnabled(false);
        lChart.setNoDataText(getString(R.string.nodata));

        XAxis lXAs = lChart.getXAxis();
        lXAs.setDrawGridLines(false);
        lXAs.setTextSize(10.0f);
        lXAs.setLabelCount(12, true);

        int minVal = 50;
        int maxVal = -999;

        List<Entry> lDataTMin = new ArrayList<>();
        List<Entry> lDataTMax = new ArrayList<>();
        List<String> lLabels = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            float tempMin = stats.get(i).getMinTemperatuur();
            float tempMax = stats.get(i).getMaxTemperatuur();
            if (tempMin != 999 && tempMin < minVal) minVal = Math.round(tempMin);
            if (tempMax != -999 && tempMax > maxVal) maxVal = Math.round(tempMax);
            lDataTMin.add(new Entry(i, tempMin == 999 ? 0 : tempMin));
            lDataTMax.add(new Entry(i, tempMax == -999 ? 0 : tempMax));
            DateTime datum = new DateTime(2000, stats.get(i).getMaand(), 1, 0, 0);
            lLabels.add(datum.toString("MMM", Locale.getDefault()));
        }

        lXAs.setValueFormatter(new IndexAxisValueFormatter(lLabels));

        minVal = minVal - 1;

        if (maxVal == -999) {
            maxVal = 9;
            minVal = 0;
        }

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

        IValueFormatter lValueFormat = new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "";
            }
        };

        LineDataSet lDsMin = new LineDataSet(lDataTMin, "Minimum temperatuur");
        lDsMin.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
        lDsMin.setCircleColor(ContextCompat.getColor(this, R.color.colorTekst));
        lDsMin.setCircleColorHole(ContextCompat.getColor(this, R.color.colorTekst));
        lDsMin.setCircleRadius(2.5f);
        lDsMin.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lDsMin.setCubicIntensity(0.2f);
        lDsMin.setValueFormatter(lValueFormat);
        lDsMin.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineDataSet lDsMax = new LineDataSet(lDataTMax, "Maximum temperatuur");
        lDsMax.setColor(ContextCompat.getColor(this, R.color.colorTemperatuurDark));
        lDsMax.setCircleColor(ContextCompat.getColor(this, R.color.colorTemperatuur));
        lDsMax.setCircleColorHole(ContextCompat.getColor(this, R.color.colorTemperatuur));
        lDsMax.setCircleRadius(2.5f);
        lDsMax.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lDsMax.setCubicIntensity(0.2f);
        lDsMax.setValueFormatter(lValueFormat);
        lDsMax.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineData lData = new LineData(lDsMin,lDsMax);
        lChart.setData(lData);
        lChart.animateXY(500, 500);
        lChart.invalidate();
    }

    private static class AsyncGetStatistiekenPerMaandTask extends AsyncTask<Context, Void, ArrayList<Statistiek1Maand>> {
        private WeakReference<StatsPerMaandActivity> activityWeakReference;

        AsyncGetStatistiekenPerMaandTask(StatsPerMaandActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<Statistiek1Maand> doInBackground(Context... params) {
            Context context = params[0];
            DatabaseHelper dh = DatabaseHelper.getInstance(context);
            int maand = DateTime.now().getMonthOfYear();
            return dh.GetStatistiek12Maanden(mJaar, maand);
        }

        @Override
        protected void onPostExecute(ArrayList<Statistiek1Maand> stats) {
            StatsPerMaandActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonStatistiekenPerMaand(stats);
        }
    }
}