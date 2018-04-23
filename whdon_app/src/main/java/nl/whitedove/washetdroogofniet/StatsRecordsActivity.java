package nl.whitedove.washetdroogofniet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class StatsRecordsActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.records_statistieken);

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
        Context context = getApplicationContext();
        new AsyncGetWeerRecords(this).execute(context);
    }

    @SuppressLint("DefaultLocale")
    private void ToonStatistiekRecords(StatistiekRecords stat) {
        if (stat == null) {
            return;
        }

        TextView tvMinTemperatuur = findViewById(R.id.tvMinTemperatuur);
        tvMinTemperatuur.setText(String.format(getString(R.string.MinMaxTemp), stat.getMinTemp(), Helper.dFormat.print(stat.getMinTempDatum())));

        TextView tvMaxTemperatuur = findViewById(R.id.tvMaxTemperatuur);
        tvMaxTemperatuur.setText(String.format(getString(R.string.MinMaxTemp), stat.getMaxTemp(), Helper.dFormat.print(stat.getMaxTempDatum())));

        TextView tvMaxWind = findViewById(R.id.tvMaxWind);
        tvMaxWind.setText(String.format(getString(R.string.MaxWind),
                WeerHelper.WindDirectionToOmschrijving(stat.getMaxWindRichting()),
                stat.getMaxWind(),
                Helper.dFormat.print(stat.getMaxWindDatum())));

        TextView tvNatsteMaandTxt = findViewById(R.id.tvNatsteMaandTxt);
        tvNatsteMaandTxt.setText(String.format(getString(R.string.NatsteMaandTxt),
                stat.getPercentNat(),
                "%",
                stat.getNatsteMaand().toString("MMM", Locale.getDefault()).replace(".", ""),
                Integer.toString(stat.getNatsteMaand().getYear())));

        TextView tvDroogsteMaandTxt = findViewById(R.id.tvDroogsteMaandTxt);
        tvDroogsteMaandTxt.setText(String.format(getString(R.string.DroogsteMaandTxt),
                stat.getPercentDroog(),
                "%",
                stat.getDroogsteMaand().toString("MMM", Locale.getDefault()).replace(".", ""),
                Integer.toString(stat.getDroogsteMaand().getYear())));

        TextView tvNatstePeriodeTxt = findViewById(R.id.tvNatstePeriodeTxt);

        DateTime beginNat = new DateTime(stat.getLangstePeriodeNatVanaf().getYear(),
                stat.getLangstePeriodeNatVanaf().getMonthOfYear(),
                stat.getLangstePeriodeNatVanaf().getDayOfMonth(), 0, 0, 0);

        DateTime eindNat = new DateTime(stat.getLangstePeriodeNatTm().getYear(),
                stat.getLangstePeriodeNatTm().getMonthOfYear(),
                stat.getLangstePeriodeNatTm().getDayOfMonth(), 0, 0, 0);

        tvNatstePeriodeTxt.setText(String.format(getString(R.string.NatstePeriodeTxt),
                Days.daysBetween(beginNat, eindNat).getDays() + 1,
                Helper.dFormat.print(stat.getLangstePeriodeNatVanaf()),
                Helper.dFormat.print(stat.getLangstePeriodeNatTm())));

        TextView tvDroogstePeriodeTxt = findViewById(R.id.tvDroogstePeriodeTxt);

        DateTime beginDroog = new DateTime(stat.getLangstePeriodeDroogVanaf().getYear(),
                stat.getLangstePeriodeDroogVanaf().getMonthOfYear(),
                stat.getLangstePeriodeDroogVanaf().getDayOfMonth(), 0, 0, 0);

        DateTime eindDroog = new DateTime(stat.getLangstePeriodeDroogTm().getYear(),
                stat.getLangstePeriodeDroogTm().getMonthOfYear(),
                stat.getLangstePeriodeDroogTm().getDayOfMonth(), 0, 0, 0);

        tvDroogstePeriodeTxt.setText(String.format(getString(R.string.DroogstePeriodeTxt),
                Days.daysBetween(beginDroog, eindDroog).getDays() + 1,
                Helper.dFormat.print(stat.getLangstePeriodeDroogVanaf()),
                Helper.dFormat.print(stat.getLangstePeriodeDroogTm())));
    }

    private static class AsyncGetWeerRecords extends AsyncTask<Context, Void, StatistiekRecords> {
        private WeakReference<StatsRecordsActivity> activityWeakReference;

        AsyncGetWeerRecords(StatsRecordsActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected StatistiekRecords doInBackground(Context... params) {
            Context context = params[0];
            DatabaseHelper dh = DatabaseHelper.getInstance(context);
            return dh.GetStatistiekRecords();
        }

        @Override
        protected void onPostExecute(StatistiekRecords stat) {
            StatsRecordsActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonStatistiekRecords(stat);
        }
    }
}