package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

import org.joda.time.DateTime

import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.Locale

class StatsWeerTypeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weertype_statistieken)
        initFab()
        initRadio()
        initSwipes()
        toondataBackground()
    }

    private fun initFab() {
        val fabTerug = findViewById<FloatingActionButton>(R.id.btnTerug)
        fabTerug.setOnClickListener { terug() }
    }

    private fun initRadio() {
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
            toondataBackground()
        }
        rgAllesJaarMaand.setOnCheckedChangeListener(cl)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSwipes() {
        val sl = object : OnSwipeTouchListener(this@StatsWeerTypeActivity) {
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
                toondataBackground()
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
                toondataBackground()
            }
        }

        val rlPerMaand = findViewById<RelativeLayout>(R.id.rlPerWeertype)
        rlPerMaand.setOnTouchListener(sl)

        val pChart = findViewById<PieChart>(R.id.pcPerWeertype)
        pChart.setOnTouchListener(sl)

        Helper.showMessage(this@StatsWeerTypeActivity, getString(R.string.SwipeLinksOfRechts))
    }

    private fun terug() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun toondataBackground() {
        val context = applicationContext
        AsyncGetStatistiekWeerTypeTask(this).execute(context)
    }

    @SuppressLint("DefaultLocale")
    private fun toonStatistiekWeerType(stats: ArrayList<StatistiekWeertype>) {

        val tvWeertype = findViewById<TextView>(R.id.tvWeertype)
        val tvGeenGegevens = findViewById<TextView>(R.id.tvGeenGegevens)

        if (mAllesJaarMaand === Helper.Periode.Alles) {
            tvWeertype.text = String.format("%s", getString(R.string.PerWeerType))
        }

        if (mAllesJaarMaand === Helper.Periode.Jaar) {
            tvWeertype.text = String.format("%s %s", getString(R.string.PerWeerType), String.format(getString(R.string.Jaartal), Integer.toString(mJaar)))
        }

        if (mAllesJaarMaand === Helper.Periode.Maand) {
            val dat = DateTime(mJaar, mMaand, 1, 0, 0)
            val mnd = dat.toString("MMM", Locale.getDefault()).replace(".", "")
            tvWeertype.text = String.format("%s %s", getString(R.string.PerWeerType), String.format(getString(R.string.JaartalEnMaand), mnd, Integer.toString(mJaar)))
        }

        val colors = ArrayList<Int>()
        val dataT = ArrayList<PieEntry>()
        val legendEntries = ArrayList<LegendEntry>()

        for (i in stats.indices) {
            val perc = stats[i].percentage
            val rPerc = Math.round(perc)
            dataT.add(PieEntry(perc, if (rPerc <= 2) "" else String.format("%d%%", rPerc)))
            val le = LegendEntry()
            val col = WeerHelper.weerTypeToWeerKleur(this, stats[i].weerType!!)
            colors.add(col)

            le.formColor = col
            le.label = stats[i].weerTypeOmschrijving
            le.form = Legend.LegendForm.SQUARE
            le.formSize = 10f
            legendEntries.add(le)
        }

        val chart = findViewById<PieChart>(R.id.pcPerWeertype)

        val desc = Description()
        desc.text = ""
        chart.description = desc

        chart.setTouchEnabled(false)
        chart.setNoDataText(getString(R.string.nodata))
        chart.legend.isEnabled = false
        chart.setEntryLabelColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        chart.setDrawEntryLabels(true)

        val legend = chart.legend
        legend.setCustom(legendEntries)
        legend.isEnabled = true
        legend.xEntrySpace = 20f
        legend.textSize = 12f
        legend.isWordWrapEnabled = true

        val dsT = PieDataSet(dataT, "")
        dsT.colors = colors
        dsT.setDrawValues(false)

        val data = PieData(dsT)
        data.setValueTextSize(14f)
        data.setValueTextColor(ContextCompat.getColor(this, R.color.colorTekst))

        chart.data = data
        chart.animateXY(500, 500)
        if (stats.size == 0) {
            chart.visibility = View.GONE
            tvGeenGegevens.visibility = View.VISIBLE
        } else {
            tvGeenGegevens.visibility = View.GONE
            chart.visibility = View.VISIBLE
        }
        chart.invalidate()

    }

    private class AsyncGetStatistiekWeerTypeTask internal constructor(context: StatsWeerTypeActivity) : AsyncTask<Context, Void, ArrayList<StatistiekWeertype>>() {
        private val activityWeakReference: WeakReference<StatsWeerTypeActivity> = WeakReference(context)

        override fun doInBackground(vararg params: Context): ArrayList<StatistiekWeertype> {
            val context = params[0]
            val dh = DatabaseHelper.getInstance(context)
            return dh.getStatistiekWeerType(mAllesJaarMaand, mJaar, mMaand)
        }

        override fun onPostExecute(stats: ArrayList<StatistiekWeertype>) {
            val activity = activityWeakReference.get()
            activity?.toonStatistiekWeerType(stats)
        }
    }

    companion object {
        internal var mJaar = DateTime.now().year
        internal var mMaand = DateTime.now().monthOfYear
        internal var mAllesJaarMaand: Helper.Periode = Helper.Periode.Maand
    }
}