package nl.whitedove.washetdroogofniet;

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
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatsPerMaandActivity extends Activity {
    DatabaseHelper mDH;
    static int jaar = DateTime.now().getYear();

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
        InitDb();
        InitSwipes();
        ToondataBackground();
    }

    private void InitSwipes() {
        OnSwipeTouchListener sl = new OnSwipeTouchListener(StatsPerMaandActivity.this) {
            public void onSwipeLeft() {
                jaar++;
                ToondataBackground();
            }

            public void onSwipeRight() {
                jaar--;
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
        final TextView tvMaandTitel = findViewById(R.id.tvMaandTitel);
        tvMaandTitel.setText(getString(R.string.per_maand) + " " + Integer.toString(jaar));
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
        lXAs.setLabelCount(12,true);

        int maxVal = -999;

        List<Entry> lDataT = new ArrayList<>();
        List<String> lLabels = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            int aantalTemp = stats.get(i).getAantalTemperatuur();
            float tempGemm =0;
            if (aantalTemp > 0) {
                int tempSom = stats.get(i).getSomTemperatuur();
                tempGemm = (1.0f * tempSom) / (1.0f * aantalTemp);
                if (tempGemm > maxVal) maxVal = Math.round(tempGemm);
            }
            Entry e = new Entry(i,tempGemm);
            lDataT.add(e);

            DateTime datum = new DateTime(2000, stats.get(i).getMaand(), 1, 0, 0);
            lLabels.add(datum.toString("MMM", Locale.getDefault()));
        }

        lXAs.setValueFormatter(new IndexAxisValueFormatter(lLabels));

        if (maxVal == -999)
        {
            maxVal = 9;
        }

        int minVal = 0;
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