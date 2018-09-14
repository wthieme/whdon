package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.widget.TextView

import org.joda.time.DateTime
import org.joda.time.Days

import java.lang.ref.WeakReference
import java.util.Locale

class StatsRecordsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.records_statistieken)

        val fabTerug = findViewById<FloatingActionButton>(R.id.btnTerug)
        fabTerug.setOnClickListener { terug() }
        toondataBackground()
    }

    private fun terug() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun toondataBackground() {
        val context = applicationContext
        AsyncGetWeerRecords(this).execute(context)
    }

    @SuppressLint("DefaultLocale")
    private fun toonStatistiekRecords(stat: StatistiekRecords?) {
        if (stat == null) {
            return
        }

        val tvMinTemperatuur = findViewById<TextView>(R.id.tvMinTemperatuur)
        tvMinTemperatuur.text = String.format(getString(R.string.MinMaxTemp), stat.minTemp, Helper.dFormat.print(stat.minTempDatum), stat.minTempLocatie)

        val tvMaxTemperatuur = findViewById<TextView>(R.id.tvMaxTemperatuur)
        tvMaxTemperatuur.text = String.format(getString(R.string.MinMaxTemp), stat.maxTemp, Helper.dFormat.print(stat.maxTempDatum), stat.maxTempLocatie)

        val tvMaxWind = findViewById<TextView>(R.id.tvMaxWind)
        tvMaxWind.text = String.format(getString(R.string.MaxWind),
                WeerHelper.windDirectionToOmschrijving(stat.maxWindRichting!!),
                stat.maxWind,
                Helper.dFormat.print(stat.maxWindDatum),
                stat.maxWindLocatie)

        val tvNatsteMaandTxt = findViewById<TextView>(R.id.tvNatsteMaandTxt)
        tvNatsteMaandTxt.text = String.format(getString(R.string.NatsteMaandTxt),
                stat.percentNat,
                "%",
                stat.natsteMaand!!.toString("MMM", Locale.getDefault()).replace(".", ""),
                Integer.toString(stat.natsteMaand!!.year))

        val tvDroogsteMaandTxt = findViewById<TextView>(R.id.tvDroogsteMaandTxt)
        tvDroogsteMaandTxt.text = String.format(getString(R.string.DroogsteMaandTxt),
                stat.percentDroog,
                "%",
                stat.droogsteMaand!!.toString("MMM", Locale.getDefault()).replace(".", ""),
                Integer.toString(stat.droogsteMaand!!.year))

        val tvNatstePeriodeTxt = findViewById<TextView>(R.id.tvNatstePeriodeTxt)

        val beginNat = DateTime(stat.langstePeriodeNatVanaf!!.year,
                stat.langstePeriodeNatVanaf!!.monthOfYear,
                stat.langstePeriodeNatVanaf!!.dayOfMonth, 0, 0, 0)

        val eindNat = DateTime(stat.langstePeriodeNatTm!!.year,
                stat.langstePeriodeNatTm!!.monthOfYear,
                stat.langstePeriodeNatTm!!.dayOfMonth, 0, 0, 0)

        tvNatstePeriodeTxt.text = String.format(getString(R.string.NatstePeriodeTxt),
                Days.daysBetween(beginNat, eindNat).days + 1,
                Helper.dFormat.print(stat.langstePeriodeNatVanaf),
                Helper.dFormat.print(stat.langstePeriodeNatTm))

        val tvDroogstePeriodeTxt = findViewById<TextView>(R.id.tvDroogstePeriodeTxt)

        val beginDroog = DateTime(stat.langstePeriodeDroogVanaf!!.year,
                stat.langstePeriodeDroogVanaf!!.monthOfYear,
                stat.langstePeriodeDroogVanaf!!.dayOfMonth, 0, 0, 0)

        val eindDroog = DateTime(stat.langstePeriodeDroogTm!!.year,
                stat.langstePeriodeDroogTm!!.monthOfYear,
                stat.langstePeriodeDroogTm!!.dayOfMonth, 0, 0, 0)

        tvDroogstePeriodeTxt.text = String.format(getString(R.string.DroogstePeriodeTxt),
                Days.daysBetween(beginDroog, eindDroog).days + 1,
                Helper.dFormat.print(stat.langstePeriodeDroogVanaf),
                Helper.dFormat.print(stat.langstePeriodeDroogTm))
    }

    private class AsyncGetWeerRecords internal constructor(context: StatsRecordsActivity) : AsyncTask<Context, Void, StatistiekRecords>() {
        private val activityWeakReference: WeakReference<StatsRecordsActivity> = WeakReference(context)

        override fun doInBackground(vararg params: Context): StatistiekRecords {
            val context = params[0]
            val dh = DatabaseHelper.getInstance(context)
            return dh.GetStatistiekRecords()
        }

        override fun onPostExecute(stat: StatistiekRecords) {
            val activity = activityWeakReference.get()
            activity?.toonStatistiekRecords(stat)
        }
    }
}