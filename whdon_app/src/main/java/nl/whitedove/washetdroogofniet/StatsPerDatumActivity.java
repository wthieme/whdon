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
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class StatsPerDatumActivity extends Activity {
    DatabaseHelper mDH;
    static DateTime datum = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(), 0, 0).minusDays(29);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.per_datum_statistieken);

        FloatingActionButton fabTerug = findViewById(R.id.btnTerug);
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
        OnSwipeTouchListener sl =
                new OnSwipeTouchListener(StatsPerDatumActivity.this) {
                    public void onSwipeLeft() {
                        datum = datum.plusDays(30);
                        ToondataBackground();
                    }

                    public void onSwipeRight() {
                        datum = datum.minusDays(30);
                        ToondataBackground();
                    }
                };

        final RelativeLayout rlPerDatum = findViewById(R.id.rlPerDatum);
        rlPerDatum.setOnTouchListener(sl);

        final BarChart bChart = findViewById(R.id.bcPerDatum);
        bChart.setOnTouchListener(sl);

        final LineChart lChart = findViewById(R.id.lcPerDatum);
        lChart.setOnTouchListener(sl);

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
        final TextView tvDatumSubtitel = findViewById(R.id.tvDatumSubtitel);
        String vanaf = Helper.dFormat.print(datum);
        String tm = Helper.dFormat.print(datum.plusDays(29));
        tvDatumSubtitel.setText(String.format(getString(R.string.vantmdatum), vanaf, tm));

        final BarChart bChart = findViewById(R.id.bcPerDatum);
        bChart.setHighlightPerTapEnabled(false);
        bChart.setHighlightPerDragEnabled(false);
        bChart.setAutoScaleMinMaxEnabled(true);
        Description bDesc = new Description();
        bDesc.setText("");
        bChart.setDescription(bDesc);
        bChart.setNoDataText(getString(R.string.nodata));
        bChart.setScaleEnabled(false);
        bChart.setFitBars(true);
        int max = 0;
        for (int i = 0; i < 30; i++) {
            int aantal = stats.get(i).getAantalNat() + stats.get(i).getAantalDroog();
            if (aantal > max) max = aantal;
        }

        YAxis bLAs = bChart.getAxisLeft();
        bLAs.setAxisMinimum(0);
        bLAs.setAxisMaximum(max + 1);
        if (max < 8) bLAs.setLabelCount(max + 1);

        YAxis bRAs = bChart.getAxisRight();
        bRAs.setAxisMinimum(0);
        bRAs.setAxisMaximum(max + 1);
        if (max < 8) bRAs.setLabelCount(max + 1);

        XAxis bXAs = bChart.getXAxis();
        bXAs.setDrawGridLines(false);
        bXAs.setTextSize(10.0f);
        bXAs.setLabelCount(30);

        ArrayList<BarEntry> bDataT = new ArrayList<>();
        ArrayList<String> bLabels = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            bDataT.add(new BarEntry(i, new float[]{stats.get(i).getAantalNat(), stats.get(i).getAantalDroog()}));
            String sDatum = (i == 0 || i == 10 || i == 20 || i == 29) ? Helper.dmFormat.print(stats.get(i).getDatum()) : "";
            bLabels.add(sDatum);
        }

        bXAs.setValueFormatter(new IndexAxisValueFormatter(bLabels));

        BarDataSet bDsT = new BarDataSet(bDataT, "");
        bDsT.setStackLabels(new String[]{"Nat", "Droog"});

        bDsT.setColors(ContextCompat.getColor(this, R.color.colorNatStart), ContextCompat.getColor(this, R.color.colorDroogStart));
        IValueFormatter myValueFormat = new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "";
            }
        };

        bDsT.setValueFormatter(myValueFormat);
        bDsT.setAxisDependency(YAxis.AxisDependency.LEFT);

        BarData bData = new BarData(bDsT);
        bChart.setData(bData);
        bChart.animateXY(500, 500);
        bChart.invalidate();

        // Gemiddelde temperatuur per dag
        final LineChart lChart = findViewById(R.id.lcPerDatum);
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
        lXAs.setLabelCount(30);

        int minVal = 50;
        int maxVal = -999;

        List<Entry> lDataT = new ArrayList<>();
        List<String> lLabels = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            int aantalTemp = stats.get(i).getAantalTemperatuur();
            float tempGemm = 0;
            if (aantalTemp > 0) {
                int tempSom = stats.get(i).getSomTemperatuur();
                tempGemm = (1.0f * tempSom) / (1.0f * aantalTemp);
                if (tempGemm < minVal) minVal = Math.round(tempGemm);
                if (tempGemm > maxVal) maxVal = Math.round(tempGemm);
            }
            Entry e = new Entry(i, tempGemm);
            lDataT.add(e);
            String sDatum = (i == 0 || i == 10 || i == 20 || i == 29) ? Helper.dmFormat.print(stats.get(i).getDatum()) : "";
            lLabels.add(sDatum);
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

        LineDataSet lDs = new LineDataSet(lDataT, "Gemiddelde temperatuur");
        lDs.setColor(ContextCompat.getColor(this, R.color.colorTemperatuurDark));
        lDs.setCircleColor(ContextCompat.getColor(this, R.color.colorTemperatuur));
        lDs.setCircleColorHole(ContextCompat.getColor(this, R.color.colorTemperatuur));
        lDs.setCircleRadius(2.5f);
        lDs.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lDs.setCubicIntensity(0.2f);

        IValueFormatter lValueFormat = new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "";
            }
        };

        lDs.setValueFormatter(lValueFormat);
        lDs.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineData lData = new LineData(lDs);
        lChart.setData(lData);
        lChart.animateXY(500, 500);
        lChart.invalidate();

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