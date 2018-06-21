package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView

import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

import org.joda.time.DateTime

import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.Collections
import java.util.Locale

class StatsWindRichtingActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.windrichting_statistieken)
        InitFab()
        InitRadio()
        InitSwipes()
        ToondataBackground()
    }

    private fun InitFab() {
        val fabTerug = findViewById<FloatingActionButton>(R.id.btnTerug)
        fabTerug.setOnClickListener { Terug() }
    }

    private fun InitRadio() {
        val rbAlles = findViewById<RadioButton>(R.id.rbAlles)
        val rbJaar = findViewById<RadioButton>(R.id.rbJaar)
        val rbMaand = findViewById<RadioButton>(R.id.rbMaand)
        val rgAllesJaarMaand = findViewById<RadioGroup>(R.id.rgAllesJaarMaand)
        rbAlles.isChecked = mAllesJaarMaand === Helper.Periode.Alles
        rbJaar.isChecked = mAllesJaarMaand === Helper.Periode.Jaar
        rbMaand.isChecked = mAllesJaarMaand === Helper.Periode.Maand
        val cl = RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            val rb = radioGroup.findViewById<RadioButton>(checkedId)
            if (rb.id == R.id.rbAlles) {
                mAllesJaarMaand = Helper.Periode.Alles
            }

            if (rb.id == R.id.rbJaar) {
                mAllesJaarMaand = Helper.Periode.Jaar
                mJaar = DateTime.now().year
            }

            if (rb.id == R.id.rbMaand) {
                mAllesJaarMaand = Helper.Periode.Maand
                mMaand = DateTime.now().monthOfYear
            }
            ToondataBackground()
        }
        rgAllesJaarMaand.setOnCheckedChangeListener(cl)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun InitSwipes() {
        val sl = object : OnSwipeTouchListener(this@StatsWindRichtingActivity) {
            override fun onSwipeLeft() {
                if (mAllesJaarMaand === Helper.Periode.Jaar) {
                    mJaar++
                }

                if (mAllesJaarMaand === Helper.Periode.Maand) {
                    mMaand++
                    if (mMaand == 13) {
                        mJaar++
                        mMaand = 1
                    }
                }
                ToondataBackground()
            }

            override fun onSwipeRight() {
                if (mAllesJaarMaand === Helper.Periode.Jaar) {
                    mJaar--
                }

                if (mAllesJaarMaand === Helper.Periode.Maand) {
                    mMaand--
                    if (mMaand == 0) {
                        mJaar--
                        mMaand = 12
                    }
                }
                ToondataBackground()
            }
        }

        val rlPerMaand = findViewById<RelativeLayout>(R.id.rlWindrichting)
        rlPerMaand.setOnTouchListener(sl)

        val rChart = findViewById<RadarChart>(R.id.rcPerWindrichting)
        rChart.setOnTouchListener(sl)

        Helper.showMessage(this@StatsWindRichtingActivity,

                getString(R.string.SwipeLinksOfRechts))
    }

    private fun Terug() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun ToondataBackground() {
        val context = applicationContext
        AsyncGetStatistiekWindTask(this).execute(context)
    }

    private fun toonStatistiekWind(stats: ArrayList<StatistiekWind>) {
        val tvWind = findViewById<TextView>(R.id.tvWind)

        if (mAllesJaarMaand === Helper.Periode.Alles) {
            tvWind.text = String.format("%s", getString(R.string.PerWindRichting))
        }

        if (mAllesJaarMaand === Helper.Periode.Jaar) {
            tvWind.text = String.format("%s %s", getString(R.string.PerWindRichting), String.format(getString(R.string.Jaartal), Integer.toString(mJaar)))
        }

        if (mAllesJaarMaand === Helper.Periode.Maand) {
            val dat = DateTime(mJaar, mMaand, 1, 0, 0)
            val mnd = dat.toString("MMM", Locale.getDefault()).replace(".", "")
            tvWind.text = String.format("%s %s", getString(R.string.PerWindRichting), String.format(getString(R.string.JaartalEnMaand), mnd, Integer.toString(mJaar)))
        }

        Collections.sort(stats, StatsWindComparator.instance)

        val dataT = ArrayList<RadarEntry>()

        val labels = ArrayList<String>()
        for (i in stats.indices) {
            dataT.add(RadarEntry(stats[i].percentage))
            labels.add(stats[i].windOmschrijving)
        }

        val chart = findViewById<RadarChart>(R.id.rcPerWindrichting)

        val desc = Description()
        desc.text = ""
        chart.description = desc

        chart.setTouchEnabled(false)
        chart.setNoDataText(getString(R.string.nodata))
        chart.legend.isEnabled = false
        val rYAs = chart.yAxis
        rYAs.setDrawLabels(true)
        rYAs.axisMinimum = 0f

        val rXAs = chart.xAxis
        rXAs.setDrawLabels(true)
        rXAs.valueFormatter = IndexAxisValueFormatter(labels)

        val dsT = RadarDataSet(dataT, "")
        dsT.color = ContextCompat.getColor(this, R.color.colorTekst)
        dsT.setDrawFilled(true)
        dsT.lineWidth = 2f

        val data = RadarData(dsT)
        data.setValueTextSize(14f)
        data.setValueTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        data.setDrawValues(false)

        chart.data = data
        chart.animateXY(500, 500)
        chart.invalidate()
    }

    private class AsyncGetStatistiekWindTask internal constructor(context: StatsWindRichtingActivity) : AsyncTask<Context, Void, ArrayList<StatistiekWind>>() {
        private val activityWeakReference: WeakReference<StatsWindRichtingActivity>

        init {
            activityWeakReference = WeakReference(context)
        }

        override fun doInBackground(vararg params: Context): ArrayList<StatistiekWind> {
            val context = params[0]
            val dh = DatabaseHelper.getInstance(context)
            return dh.GetStatistiekWind(mAllesJaarMaand, mJaar, mMaand)
        }

        override fun onPostExecute(stats: ArrayList<StatistiekWind>) {
            val activity = activityWeakReference.get()
            activity?.toonStatistiekWind(stats)
        }
    }

    companion object {
        internal var mJaar = DateTime.now().year
        internal var mMaand = DateTime.now().monthOfYear
        internal var mAllesJaarMaand: Helper.Periode = Helper.Periode.Maand
    }
}