package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.widget.RelativeLayout
import android.widget.TextView

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

import org.joda.time.DateTime

import java.lang.ref.WeakReference
import java.util.ArrayList

class StatsAantalGebruikersActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.aantal_gebruikers_statistieken)

        val fabTerug = findViewById<FloatingActionButton>(R.id.btnTerug)
        fabTerug.setOnClickListener { terug() }
        initSwipes()
        toondataBackground()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSwipes() {
        val sl = object : OnSwipeTouchListener(this@StatsAantalGebruikersActivity) {
            override fun onSwipeLeft() {
                datum = datum.plusDays(30)
                toondataBackground()
            }

            override fun onSwipeRight() {
                datum = datum.minusDays(30)
                toondataBackground()
            }
        }

        val rlAantalGebruikers = findViewById<RelativeLayout>(R.id.rlAantalGebruikers)
        rlAantalGebruikers.setOnTouchListener(sl)

        val chart = findViewById<LineChart>(R.id.lcAantalGebruikers)
        chart.setOnTouchListener(sl)

        Helper.showMessage(this@StatsAantalGebruikersActivity, getString(R.string.SwipeLinksOfRechts))
    }

    private fun terug() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun toondataBackground() {
        val context = applicationContext
        AsyncGetAantalGebruikersStatistiekenTask(this).execute(context)
    }

    @SuppressLint("DefaultLocale")
    private fun toonGetAantalGebruikersStatistieken(stats: ArrayList<StatistiekAantalGebruikers>?) {
        if (stats == null || stats.size == 0) {
            return
        }
        val tvAantalGebruikersSubtitel = findViewById<TextView>(R.id.tvAantalGebruikersSubtitel)
        val vanaf = Helper.dFormat.print(datum)
        val tm = Helper.dFormat.print(datum.plusDays(29))
        tvAantalGebruikersSubtitel.text = String.format(getString(R.string.vantmdatum), vanaf, tm)

        val lChart = findViewById<LineChart>(R.id.lcAantalGebruikers)
        lChart.isHighlightPerTapEnabled = false
        lChart.isHighlightPerDragEnabled = false
        lChart.isAutoScaleMinMaxEnabled = true
        val desc = Description()
        desc.text = ""
        lChart.description = desc
        lChart.setScaleEnabled(false)
        lChart.setNoDataText(getString(R.string.nodata))

        var minVal = Integer.MAX_VALUE
        var maxVal = Integer.MIN_VALUE

        for (i in 0..29) {
            val aantal = stats[i].aantalGebruikers
            if (aantal < minVal) minVal = aantal
            if (aantal > maxVal) maxVal = aantal
        }

        minVal -= 1
        if (minVal < 0) minVal = 0
        var labelCount = maxVal - minVal + 2
        while (labelCount > 10) labelCount /= 2

        val yAsL = lChart.axisLeft
        yAsL.setLabelCount(labelCount, true)
        yAsL.axisMinimum = minVal.toFloat()
        yAsL.axisMaximum = (maxVal + 1).toFloat()
        val yAsR = lChart.axisRight
        yAsR.axisMinimum = minVal.toFloat()
        yAsR.axisMaximum = (maxVal + 1).toFloat()
        yAsR.setLabelCount(labelCount, true)

        val xAs = lChart.xAxis
        xAs.setDrawGridLines(false)
        xAs.textSize = 10.0f
        xAs.labelCount = 30

        val dataY = ArrayList<Entry>()
        val labels = ArrayList<String>()

        for (i in 0..29) {
            val e = Entry(i.toFloat(), stats[i].aantalGebruikers.toFloat())
            val sDatum = if (i == 0 || i == 10 || i == 20 || i == 29) Helper.dmFormat.print(stats[i].datum) else ""
            labels.add(sDatum)
            dataY.add(e)
        }

        xAs.valueFormatter = IndexAxisValueFormatter(labels)

        val ds = LineDataSet(dataY, "Cumulatief aantal gebruikers")
        ds.color = ContextCompat.getColor(this, R.color.colorTemperatuurDark)
        ds.setCircleColor(ContextCompat.getColor(this, R.color.colorTemperatuur))
        ds.setCircleColorHole(ContextCompat.getColor(this, R.color.colorTemperatuur))
        ds.circleRadius = 2.5f
        ds.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        ds.cubicIntensity = 0.2f
        val myValueFormat = IValueFormatter { _, _, _, _ -> "" }

        ds.valueFormatter = myValueFormat
        ds.axisDependency = YAxis.AxisDependency.LEFT

        val data = LineData(ds)
        lChart.data = data
        lChart.animateXY(500, 500)
        lChart.invalidate()
    }

    private class AsyncGetAantalGebruikersStatistiekenTask internal constructor(context: StatsAantalGebruikersActivity) : AsyncTask<Context, Void, ArrayList<StatistiekAantalGebruikers>>() {
        private val activityWeakReference: WeakReference<StatsAantalGebruikersActivity> = WeakReference(context)

        override fun doInBackground(vararg params: Context): ArrayList<StatistiekAantalGebruikers> {
            val context = params[0]
            val dh = DatabaseHelper.getInstance(context)
            return dh.GetAantalGebruikers30Dagen(datum)
        }

        override fun onPostExecute(stats: ArrayList<StatistiekAantalGebruikers>) {
            val activity = activityWeakReference.get()
            activity?.toonGetAantalGebruikersStatistieken(stats)
        }
    }

    companion object {
        internal var datum = DateTime(DateTime.now().year, DateTime.now().monthOfYear, DateTime.now().dayOfMonth, 0, 0).minusDays(29)
    }
}