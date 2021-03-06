package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.content.ContextCompat
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView

import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

import org.joda.time.DateTime

import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.Collections
import java.util.Locale

class StatsWindSnelheidActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.windsnelheid_statistieken)
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
        val sl = object : OnSwipeTouchListener(this@StatsWindSnelheidActivity) {
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

        val rlPerMaand = findViewById<RelativeLayout>(R.id.rlWindsnelheid)
        rlPerMaand.setOnTouchListener(sl)

        val rChart = findViewById<RadarChart>(R.id.rcWindsnelheid)
        rChart.setOnTouchListener(sl)

        Helper.showMessage(this@StatsWindSnelheidActivity,

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

    private fun ToonStatistiekWind(stats: ArrayList<StatistiekWind>) {
        val tvWind = findViewById<TextView>(R.id.tvWind)
        if (mAllesJaarMaand === Helper.Periode.Alles) {
            tvWind.text = String.format("%s", getString(R.string.Windsnelheid))
        }

        if (mAllesJaarMaand === Helper.Periode.Jaar) {
            tvWind.text = String.format("%s %s", getString(R.string.Windsnelheid), String.format(getString(R.string.Jaartal), Integer.toString(mJaar)))
        }

        if (mAllesJaarMaand === Helper.Periode.Maand) {
            val dat = DateTime(mJaar, mMaand, 1, 0, 0)
            val mnd = dat.toString("MMM", Locale.getDefault()).replace(".", "")
            tvWind.text = String.format("%s %s", getString(R.string.Windsnelheid), String.format(getString(R.string.JaartalEnMaand), mnd, Integer.toString(mJaar)))
        }

        Collections.sort(stats, StatsWindComparator.instance)

        val dataMn = ArrayList<RadarEntry>()
        val dataA = ArrayList<RadarEntry>()
        val dataMx = ArrayList<RadarEntry>()

        val labels = ArrayList<String>()
        for (i in stats.indices) {
            dataMn.add(RadarEntry(stats[i].minWindSpeed))
            dataA.add(RadarEntry(stats[i].avgWindSpeed))
            dataMx.add(RadarEntry(stats[i].maxWindSpeed))
            labels.add(stats[i].windOmschrijving)
        }

        val chart = findViewById<RadarChart>(R.id.rcWindsnelheid)

        val desc = Description()
        desc.text = ""
        chart.description = desc

        chart.setTouchEnabled(false)
        chart.setNoDataText(getString(R.string.nodata))
        val rYAs = chart.yAxis
        rYAs.setDrawLabels(true)
        rYAs.axisMinimum = 0f

        val rXAs = chart.xAxis
        rXAs.setDrawLabels(true)
        rXAs.valueFormatter = IndexAxisValueFormatter(labels)

        val dsMn = RadarDataSet(dataMn, getString(R.string.MinWindSnelheid))
        dsMn.color = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        dsMn.fillColor = ContextCompat.getColor(this, R.color.colorFillMin)
        dsMn.setDrawFilled(true)
        dsMn.lineWidth = 2f

        val dsA = RadarDataSet(dataA, getString(R.string.GemmWindSnelheid))
        dsA.color = ContextCompat.getColor(this, R.color.colorAvg)
        dsA.fillColor = ContextCompat.getColor(this, R.color.colorFillAvg)
        dsA.setDrawFilled(true)
        dsA.lineWidth = 2f

        val dsMx = RadarDataSet(dataMx, getString(R.string.MaxWindSnelheid))
        dsMx.color = ContextCompat.getColor(this, R.color.colorMax)
        dsMx.fillColor = ContextCompat.getColor(this, R.color.colorFillMax)
        dsMx.setDrawFilled(true)
        dsMx.lineWidth = 2f

        val data = RadarData(dsMn, dsA, dsMx)
        data.setValueTextSize(14f)
        data.setValueTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        data.setDrawValues(false)

        chart.data = data
        chart.animateXY(500, 500)
        chart.invalidate()
    }

    private class AsyncGetStatistiekWindTask internal constructor(context: StatsWindSnelheidActivity) : AsyncTask<Context, Void, ArrayList<StatistiekWind>>() {
        private val activityWeakReference: WeakReference<StatsWindSnelheidActivity>

        init {
            activityWeakReference = WeakReference(context)
        }

        override fun doInBackground(vararg params: Context): ArrayList<StatistiekWind> {
            val context = params[0]
            val dh = DatabaseHelper.getInstance(context)
            return dh.getStatistiekWind(mAllesJaarMaand, mJaar, mMaand)
        }

        override fun onPostExecute(stats: ArrayList<StatistiekWind>) {
            val activity = activityWeakReference.get()
            activity?.ToonStatistiekWind(stats)
        }
    }

    companion object {
        internal var mJaar = DateTime.now().year
        internal var mMaand = DateTime.now().monthOfYear
        internal var mAllesJaarMaand: Helper.Periode = Helper.Periode.Maand
    }
}