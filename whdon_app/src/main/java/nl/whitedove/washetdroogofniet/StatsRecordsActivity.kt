package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import org.joda.time.DateTime
import org.joda.time.Days
import java.lang.ref.WeakReference
import java.util.*

class StatsRecordsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.records_statistieken)

        val fabTerug = findViewById<FloatingActionButton>(R.id.btnTerug)
        fabTerug.setOnClickListener { terug() }
        initSwipes()
        initRadio()
        toondataBackground()
    }

    private fun initRadio() {
        val rbAlles = findViewById<RadioButton>(R.id.rbAlles)
        val rbJaar = findViewById<RadioButton>(R.id.rbJaar)
        val rgAllesJaar = findViewById<RadioGroup>(R.id.rgAllesJaar)
        rbAlles.isChecked = StatsRecordsActivity.mAllesJaar === Helper.Periode.Alles
        rbJaar.isChecked = StatsRecordsActivity.mAllesJaar === Helper.Periode.Jaar
        val cl = RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            val rb = radioGroup.findViewById<RadioButton>(checkedId)
            if (rb.id == R.id.rbAlles) {
                StatsRecordsActivity.mAllesJaar = Helper.Periode.Alles
            }

            if (rb.id == R.id.rbJaar) {
                StatsRecordsActivity.mAllesJaar = Helper.Periode.Jaar
                StatsRecordsActivity.mJaar = DateTime.now().year
            }

            toondataBackground()
        }
        rgAllesJaar.setOnCheckedChangeListener(cl)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSwipes() {
        val sl = object : OnSwipeTouchListener(this@StatsRecordsActivity) {
            override fun onSwipeLeft() {
                if (StatsRecordsActivity.mAllesJaar === Helper.Periode.Jaar) {
                    StatsRecordsActivity.mJaar++
                }
                toondataBackground()
            }

            override fun onSwipeRight() {
                if (StatsRecordsActivity.mAllesJaar === Helper.Periode.Jaar) {
                    StatsRecordsActivity.mJaar--
                }
                toondataBackground()
            }
        }

        val rlRecords = findViewById<RelativeLayout>(R.id.rlRecords)
        rlRecords.setOnTouchListener(sl)

        Helper.showMessage(this@StatsRecordsActivity,
                getString(R.string.SwipeLinksOfRechts))
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

        val tvRecords = findViewById<TextView>(R.id.tvRecords)

        if (StatsRecordsActivity.mAllesJaar === Helper.Periode.Alles) {
            tvRecords.text = String.format("%s", getString(R.string.Weerrecords))
        }

        if (StatsRecordsActivity.mAllesJaar === Helper.Periode.Jaar) {
            tvRecords.text = String.format("%s %s", getString(R.string.Weerrecords), String.format(getString(R.string.Jaartal), Integer.toString(StatsRecordsActivity.mJaar)))
        }

        val tvMinTemperatuur = findViewById<TextView>(R.id.tvMinTemperatuur)
        val datMin = stat.minTempDatum
        if (datMin != null && datMin.year > 2015)
            tvMinTemperatuur.text = String.format(getString(R.string.MinMaxTemp), stat.minTemp, Helper.dFormat.print(stat.minTempDatum), stat.minTempLocatie)
        else
            tvMinTemperatuur.text = "";

        val tvMaxTemperatuur = findViewById<TextView>(R.id.tvMaxTemperatuur)
        val datMax = stat.maxTempDatum
        if (datMax != null && datMax.year > 2015)
            tvMaxTemperatuur.text = String.format(getString(R.string.MinMaxTemp), stat.maxTemp, Helper.dFormat.print(stat.maxTempDatum), stat.maxTempLocatie)
        else
            tvMaxTemperatuur.text = "";

        val tvMaxWind = findViewById<TextView>(R.id.tvMaxWind)
        val datMaxWind = stat.maxWindDatum
        if (datMaxWind != null && datMaxWind.year > 2015)
            tvMaxWind.text = String.format(getString(R.string.MaxWind),
                    WeerHelper.windDirectionToOmschrijving(stat.maxWindRichting!!),
                    stat.maxWind,
                    Helper.dFormat.print(stat.maxWindDatum),
                    stat.maxWindLocatie)
        else
            tvMaxWind.text = "";

        val tvNatsteMaandTxt = findViewById<TextView>(R.id.tvNatsteMaandTxt)
        val datNatste = stat.natsteMaand
        if (datNatste != null && datNatste.year > 2015)
            tvNatsteMaandTxt.text = String.format(getString(R.string.NatsteMaandTxt),
                    stat.percentNat,
                    "%",
                    stat.natsteMaand!!.toString("MMM", Locale.getDefault()).replace(".", ""),
                    Integer.toString(stat.natsteMaand!!.year))
        else
            tvNatsteMaandTxt.text = "";

        val tvDroogsteMaandTxt = findViewById<TextView>(R.id.tvDroogsteMaandTxt)
        val datDroogste = stat.droogsteMaand
        if (datDroogste != null && datDroogste.year > 2015)
            tvDroogsteMaandTxt.text = String.format(getString(R.string.DroogsteMaandTxt),
                    stat.percentDroog,
                    "%",
                    stat.droogsteMaand!!.toString("MMM", Locale.getDefault()).replace(".", ""),
                    Integer.toString(stat.droogsteMaand!!.year))
        else
            tvDroogsteMaandTxt.text = "";

        val tvNatstePeriodeTxt = findViewById<TextView>(R.id.tvNatstePeriodeTxt)

        val beginNat = DateTime(stat.langstePeriodeNatVanaf!!.year,
                stat.langstePeriodeNatVanaf!!.monthOfYear,
                stat.langstePeriodeNatVanaf!!.dayOfMonth, 0, 0, 0)

        val eindNat = DateTime(stat.langstePeriodeNatTm!!.year,
                stat.langstePeriodeNatTm!!.monthOfYear,
                stat.langstePeriodeNatTm!!.dayOfMonth, 0, 0, 0)

        val datLangsteDroog = stat.langstePeriodeNatVanaf
        if (datLangsteDroog != null && datLangsteDroog.year > 2015)
            tvNatstePeriodeTxt.text = String.format(getString(R.string.NatstePeriodeTxt),
                    Days.daysBetween(beginNat, eindNat).days + 1,
                    Helper.dFormat.print(stat.langstePeriodeNatVanaf),
                    Helper.dFormat.print(stat.langstePeriodeNatTm))
        else
            tvNatstePeriodeTxt.text = ""

        val tvDroogstePeriodeTxt = findViewById<TextView>(R.id.tvDroogstePeriodeTxt)

        val beginDroog = DateTime(stat.langstePeriodeDroogVanaf!!.year,
                stat.langstePeriodeDroogVanaf!!.monthOfYear,
                stat.langstePeriodeDroogVanaf!!.dayOfMonth, 0, 0, 0)

        val eindDroog = DateTime(stat.langstePeriodeDroogTm!!.year,
                stat.langstePeriodeDroogTm!!.monthOfYear,
                stat.langstePeriodeDroogTm!!.dayOfMonth, 0, 0, 0)
        val datLangsteNat = stat.langstePeriodeDroogVanaf
        if (datLangsteNat != null && datLangsteNat.year > 2015)

            tvDroogstePeriodeTxt.text = String.format(getString(R.string.DroogstePeriodeTxt),
                    Days.daysBetween(beginDroog, eindDroog).days + 1,
                    Helper.dFormat.print(stat.langstePeriodeDroogVanaf),
                    Helper.dFormat.print(stat.langstePeriodeDroogTm))
        else
            tvDroogstePeriodeTxt.text = ""
    }

    private class AsyncGetWeerRecords internal constructor(context: StatsRecordsActivity) : AsyncTask<Context, Void, StatistiekRecords>() {
        private val activityWeakReference: WeakReference<StatsRecordsActivity> = WeakReference(context)

        override fun doInBackground(vararg params: Context): StatistiekRecords {
            val context = params[0]
            val dh = DatabaseHelper.getInstance(context)
            return dh.getStatistiekRecords(mAllesJaar, mJaar)
        }

        override fun onPostExecute(stat: StatistiekRecords) {
            val activity = activityWeakReference.get()
            activity?.toonStatistiekRecords(stat)
        }
    }

    companion object {
        internal var mJaar = DateTime.now().year
        internal var mAllesJaar: Helper.Periode = Helper.Periode.Jaar
    }
}