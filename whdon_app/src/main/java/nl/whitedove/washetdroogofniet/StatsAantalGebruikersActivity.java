package nl.whitedove.washetdroogofniet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;

public class StatsAantalGebruikersActivity extends Activity {
    DatabaseHelper mDH;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aantal_gebruikers_statistieken);

        FloatingActionButton fabTerug = (FloatingActionButton) findViewById(R.id.btnTerug);
        fabTerug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Terug();
            }
        });
        InitDb();
        ToondataBackground();
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
        new AsyncGetAantalGebruikersStatistiekenTask().execute();
    }

    @SuppressLint("DefaultLocale")
    private void ToonGetAantalGebruikersStatistieken(ArrayList<StatistiekAantalGebruikers> stats) {
        if (stats == null || stats.size() == 0) {
            return;
        }
        final LineChart chart = (LineChart) findViewById(R.id.lcAantalGebruikers);
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.setAutoScaleMinMaxEnabled(true);
        Description desc = new Description();
        desc.setText("");
        chart.setDescription(desc);
        chart.setScaleEnabled(false);
        chart.setNoDataText(getString(R.string.nodata));

        ArrayList<Entry> dataY = new ArrayList<>();

        for (int i = 0; i < 31; i++) {
            Entry e = new Entry(-1 * stats.get(i).getDag(), stats.get(i).getAantalGebruikers());
            dataY.add(e);
        }

        LineDataSet ds = new LineDataSet(dataY, "Cumulatief aantal gebruikers in de afgelopen 30 dagen");
        ds.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        ds.setCircleColor(ContextCompat.getColor(this, R.color.colorNatStart));
        IValueFormatter myValueFormat = new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "";
            }
        };

        ds.setValueFormatter(myValueFormat);
        ds.setAxisDependency(YAxis.AxisDependency.LEFT);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(ds);
        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.animateXY(500, 500);
        chart.invalidate();
    }

    private class AsyncGetAantalGebruikersStatistiekenTask extends AsyncTask<Void, Void, ArrayList<StatistiekAantalGebruikers>> {

        @Override
        protected ArrayList<StatistiekAantalGebruikers> doInBackground(Void... params) {
            return mDH.GetAantalGebruikers30Dagen();
        }

        @Override
        protected void onPostExecute(ArrayList<StatistiekAantalGebruikers> stats) {
            ToonGetAantalGebruikersStatistieken(stats);
        }
    }
}