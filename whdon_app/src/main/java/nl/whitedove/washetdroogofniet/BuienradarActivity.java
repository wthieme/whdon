package nl.whitedove.washetdroogofniet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class BuienradarActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buienradar);

        FloatingActionButton fabTerug = findViewById(R.id.btnTerug);
        fabTerug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Terug();
            }
        });
        ToondataBackground();
    }

    private void Terug() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void ToondataBackground() {
        Context cxt = getApplicationContext();
        if (!Helper.TestInternet(cxt)) {
            return;
        }
        new AsyncGetWeerVoorspellingTask(this).execute(cxt);
    }

    @SuppressLint("DefaultLocale")
    private void ToonBuienData(BuienData weerData) {
        if (weerData == null || weerData.getRegenData() == null || weerData.getRegenData().size() == 0) {
            return;
        }
        LineChart chart = findViewById(R.id.lcBuienRadar);
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.setVisibleYRangeMaximum(255, YAxis.AxisDependency.LEFT);
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.setScaleEnabled(false);
        chart.setNoDataText(getString(R.string.nodata));

        IAxisValueFormatter myYFormat = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value >= 0 && value < 40) return "0";
                else if (value >= 40 && value < 80) return "1";
                else if (value >= 80 && value < 120) return "2";
                else if (value >= 120 && value < 160) return "3";
                else if (value >= 160 && value < 200) return "4";
                else if (value >= 200 && value < 240) return "5";
                else return "";
            }
        };

        YAxis yAs1 = chart.getAxisLeft();
        yAs1.setAxisMaximum(255);
        yAs1.setAxisMinimum(0);
        yAs1.setLabelCount(7, true);
        yAs1.setValueFormatter(myYFormat);

        YAxis yAs2 = chart.getAxisRight();
        yAs2.setDrawLabels(false);
        yAs2.setAxisMaximum(255);
        yAs2.setAxisMinimum(0);
        yAs2.setLabelCount(0, true);

        XAxis xAs = chart.getXAxis();
        xAs.setDrawGridLines(false);

        TextView tvDroogBr = findViewById(R.id.tvDroogBr);
        String sBr = WeerHelper.BepaalBrDataTxt(this, weerData);
        tvDroogBr.setText(sBr);

        // De markeerlijn voor nu
        float xPos = WeerHelper.BerekenNuXPositie(weerData);
        LimitLine ll = new LimitLine(xPos, "Nu");
        ll.setLineColor(Color.RED);
        ll.setLineWidth(1f);
        ll.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        ll.setTextSize(12f);
        xAs.addLimitLine(ll);
        xAs.setDrawLimitLinesBehindData(true);

        ArrayList<Entry> dataT = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        double mm = 0;
        int som = 0;
        for (int i = 0; i < weerData.getRegenData().size(); i++) {
            int regen = weerData.getRegenData().get(i).getRegen();
            som += regen;
            double peruur;
            if (regen == 0) {
                peruur = 0;
            } else {
                peruur = Math.pow(10, (regen - 109.0f) / 32.0f);
            }

            mm += peruur / 12.0f;
            dataT.add(new Entry(i, regen));
            labels.add(weerData.getRegenData().get(i).getTijd());
        }

        if (som == 0) {
            tvDroogBr.setVisibility(View.VISIBLE);
            yAs1.setDrawGridLines(false);
            yAs2.setDrawGridLines(false);
        } else {
            tvDroogBr.setVisibility(View.GONE);
        }

        xAs.setValueFormatter(new IndexAxisValueFormatter(labels));

        LineDataSet dsT = new LineDataSet(dataT, "Intensiteit: 1 (lichte regen) t/m 5 (tropische regen)");
        dsT.setColor(ContextCompat.getColor(this, R.color.colorNatStart));
        dsT.setDrawFilled(true);
        dsT.setFillColor(ContextCompat.getColor(this, R.color.colorNatStart));
        dsT.setDrawCircles(false);
        dsT.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dsT.setCubicIntensity(0.2f);
        IValueFormatter myValueFormat = new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "";
            }
        };

        dsT.setValueFormatter(myValueFormat);
        dsT.setAxisDependency(YAxis.AxisDependency.LEFT);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dsT);

        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.animateXY(500, 500);
        chart.invalidate();

        TextView tvNeerslag = findViewById(R.id.tvNeerslag);
        tvNeerslag.setText(String.format("%.2f mm", mm));
    }

    private static class AsyncGetWeerVoorspellingTask extends AsyncTask<Context, Void, BuienData> {
        private WeakReference<BuienradarActivity> activityWeakReference;

        AsyncGetWeerVoorspellingTask(BuienradarActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected BuienData doInBackground(Context... params) {

            BuienData weer = null;
            try {
                weer = WeerHelper.BepaalBuien();
            } catch (Exception ignored) {
            }
            return weer;
        }

        @Override
        protected void onPostExecute(BuienData result) {
            BuienradarActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonBuienData(result);
        }

    }
}