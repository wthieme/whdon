package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet

import org.joda.time.DateTime

import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.Locale

class StatsPerUurActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.per_uur_statistieken)
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
        val sl = object : OnSwipeTouchListener(this@StatsPerUurActivity) {
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

        val rlPerUurVdDag = findViewById<RelativeLayout>(R.id.rlPerUurVdDag)
        rlPerUurVdDag.setOnTouchListener(sl)

        val bcPerUurVdDag = findViewById<BarChart>(R.id.bcPerUurVdDag)
        bcPerUurVdDag.setOnTouchListener(sl)

        Helper.showMessage(this@StatsPerUurActivity, getString(R.string.SwipeLinksOfRechts))
    }

    private fun terug() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun toondataBackground() {
        val context = applicationContext
        AsyncGetStatistiekenPerUurTask(this).execute(context)
    }

    private fun toonStatistiekenPerUur(stats: ArrayList<Statistiek1Uur>) {
        val tvUurTitel = findViewById<TextView>(R.id.tvUurTitel)

        if (mAllesJaarMaand === Helper.Periode.Alles) {
            tvUurTitel.text = String.format("%s", getString(R.string.AantalPerUur))
        }

        if (mAllesJaarMaand === Helper.Periode.Jaar) {
            tvUurTitel.text = String.format("%s %s", getString(R.string.AantalPerUur), String.format(getString(R.string.Jaartal), Integer.toString(mJaar)))
        }

        if (mAllesJaarMaand === Helper.Periode.Maand) {
            val dat = DateTime(mJaar, mMaand, 1, 0, 0)
            val mnd = dat.toString("MMM", Locale.getDefault()).replace(".", "")
            tvUurTitel.text = String.format("%s %s", getString(R.string.AantalPerUur), String.format(getString(R.string.JaartalEnMaand), mnd, Integer.toString(mJaar)))
        }

        val chart = findViewById<BarChart>(R.id.bcPerUurVdDag)
        chart.isHighlightPerTapEnabled = false
        chart.isHighlightPerDragEnabled = false
        chart.isAutoScaleMinMaxEnabled = true
        val desc = Description()
        desc.text = ""
        chart.description = desc
        chart.setNoDataText(getString(R.string.nodata))
        chart.setScaleEnabled(false)
        val xAs = chart.xAxis
        xAs.setDrawGridLines(false)
        xAs.setDrawLabels(true)
        xAs.setDrawAxisLine(false)
        xAs.yOffset = 5.0f
        xAs.textSize = 10.0f
        xAs.labelCount = 24

        var max = 0
        for (i in stats.indices) {
            val aantal = stats[i].aantalNat + stats[i].aantalDroog
            if (aantal > max) max = aantal
        }

        val bLAs = chart.axisLeft
        bLAs.axisMinimum = 0f
        bLAs.axisMaximum = (max + 1).toFloat()
        if (max < 8)
            bLAs.labelCount = max + 1
        else
            bLAs.labelCount = 6

        val bRAs = chart.axisRight
        bRAs.axisMinimum = 0f
        bRAs.axisMaximum = (max + 1).toFloat()
        if (max < 8)
            bRAs.labelCount = max + 1
        else
            bRAs.labelCount = 6

        val dataT = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        //Initialize with zero's
        for (i in 0..23) {
            dataT.add(BarEntry(i.toFloat(), floatArrayOf(0f, 0f)))
            labels.add(Integer.toString(i))
        }

        for (i in stats.indices) {
            val uur = stats[i].uur
            val aantalNat = stats[i].aantalNat
            val aantalDroog = stats[i].aantalDroog
            dataT[uur].setVals(floatArrayOf(aantalNat.toFloat(), aantalDroog.toFloat()))
        }

        xAs.valueFormatter = IndexAxisValueFormatter(labels)

        val dsT = BarDataSet(dataT, "")
        dsT.stackLabels = arrayOf(this.getString(R.string.NatTxt), this.getString(R.string.DroogTxt))
        dsT.setColors(ContextCompat.getColor(this, R.color.colorNatStart), ContextCompat.getColor(this, R.color.colorDroogStart))

        val myValueFormat = IValueFormatter { _, _, _, _ -> "" }

        dsT.valueFormatter = myValueFormat
        dsT.axisDependency = YAxis.AxisDependency.LEFT

        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(dsT)

        val data = BarData(dataSets)
        chart.data = data
        chart.animateXY(500, 500)
        chart.invalidate()
    }

    private class AsyncGetStatistiekenPerUurTask internal constructor(context: StatsPerUurActivity) : AsyncTask<Context, Void, ArrayList<Statistiek1Uur>>() {

        private val activityWeakReference: WeakReference<StatsPerUurActivity> = WeakReference(context)

        override fun doInBackground(vararg params: Context): ArrayList<Statistiek1Uur> {
            val context = params[0]
            val dh = DatabaseHelper.getInstance(context)
            return dh.GetStatistiek24Uur(mAllesJaarMaand, mJaar, mMaand)
        }

        override fun onPostExecute(stats: ArrayList<Statistiek1Uur>) {
            val activity = activityWeakReference.get()
            activity?.toonStatistiekenPerUur(stats)
        }
    }

    companion object {
        internal var mJaar = DateTime.now().year
        internal var mMaand = DateTime.now().monthOfYear
        internal var mAllesJaarMaand: Helper.Periode = Helper.Periode.Maand
    }
}