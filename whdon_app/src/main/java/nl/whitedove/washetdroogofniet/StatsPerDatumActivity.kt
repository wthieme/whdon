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

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

import org.joda.time.DateTime

import java.lang.ref.WeakReference
import java.util.ArrayList

class StatsPerDatumActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.per_datum_statistieken)

        val fabTerug = findViewById<FloatingActionButton>(R.id.btnTerug)
        fabTerug.setOnClickListener { terug() }
        initSwipes()
        toondataBackground()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSwipes() {
        val sl = object : OnSwipeTouchListener(this@StatsPerDatumActivity) {
            override fun onSwipeLeft() {
                datum = datum.plusDays(30)
                toondataBackground()
            }

            override fun onSwipeRight() {
                datum = datum.minusDays(30)
                toondataBackground()
            }
        }

        val rlPerDatum = findViewById<RelativeLayout>(R.id.rlPerDatum)
        rlPerDatum.setOnTouchListener(sl)

        val bChart = findViewById<BarChart>(R.id.bcPerDatum)
        bChart.setOnTouchListener(sl)

        val lChart = findViewById<LineChart>(R.id.lcPerDatum)
        lChart.setOnTouchListener(sl)

        Helper.showMessage(this@StatsPerDatumActivity, getString(R.string.SwipeLinksOfRechts))
    }

    private fun terug() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun toondataBackground() {
        val context = applicationContext
        AsyncGetStatistiekenTask(this).execute(context)
    }

    @SuppressLint("DefaultLocale")
    private fun toonStatistiekenPerDag(stats: ArrayList<Statistiek1Dag>?) {
        if (stats == null || stats.size == 0) {
            return
        }
        val tvDatumSubtitel = findViewById<TextView>(R.id.tvDatumSubtitel)
        val vanaf = Helper.dFormat.print(datum)
        val tm = Helper.dFormat.print(datum.plusDays(29))
        tvDatumSubtitel.text = String.format(getString(R.string.vantmdatum), vanaf, tm)

        val bChart = findViewById<BarChart>(R.id.bcPerDatum)
        bChart.isHighlightPerTapEnabled = false
        bChart.isHighlightPerDragEnabled = false
        bChart.isAutoScaleMinMaxEnabled = true
        val bDesc = Description()
        bDesc.text = ""
        bChart.description = bDesc
        bChart.setNoDataText(getString(R.string.nodata))
        bChart.setScaleEnabled(false)
        bChart.setFitBars(true)
        var max = 0
        for (i in 0..29) {
            val aantal = stats[i].aantalNat + stats[i].aantalDroog
            if (aantal > max) max = aantal
        }

        val bLAs = bChart.axisLeft
        bLAs.axisMinimum = 0f
        bLAs.axisMaximum = (max + 1).toFloat()
        if (max < 8)
            bLAs.labelCount = max + 1
        else
            bLAs.labelCount = 6

        val bRAs = bChart.axisRight
        bRAs.axisMinimum = 0f
        bRAs.axisMaximum = (max + 1).toFloat()
        if (max < 8)
            bRAs.labelCount = max + 1
        else
            bRAs.labelCount = 6

        val bXAs = bChart.xAxis
        bXAs.setDrawGridLines(false)
        bXAs.textSize = 10.0f
        bXAs.labelCount = 30

        val bDataT = ArrayList<BarEntry>()
        val bLabels = ArrayList<String>()

        for (i in 0..29) {
            bDataT.add(BarEntry(i.toFloat(), floatArrayOf(stats[i].aantalNat.toFloat(), stats[i].aantalDroog.toFloat())))
            val sDatum = if (i == 0 || i == 10 || i == 20 || i == 29) Helper.dmFormat.print(stats[i].datum) else ""
            bLabels.add(sDatum)
        }

        bXAs.valueFormatter = IndexAxisValueFormatter(bLabels)

        val bDsT = BarDataSet(bDataT, "")
        bDsT.stackLabels = arrayOf("Nat", "Droog")

        bDsT.setColors(ContextCompat.getColor(this, R.color.colorNatStart), ContextCompat.getColor(this, R.color.colorDroogStart))
        val myValueFormat = IValueFormatter { _, _, _, _ -> "" }

        bDsT.valueFormatter = myValueFormat
        bDsT.axisDependency = YAxis.AxisDependency.LEFT

        val bData = BarData(bDsT)
        bChart.data = bData
        bChart.animateXY(500, 500)
        bChart.invalidate()

        // Gemiddelde temperatuur per dag
        val lChart = findViewById<LineChart>(R.id.lcPerDatum)
        lChart.isHighlightPerTapEnabled = false
        lChart.isHighlightPerDragEnabled = false
        lChart.isAutoScaleMinMaxEnabled = true
        val lDesc = Description()
        lDesc.text = ""
        lChart.description = lDesc
        lChart.setScaleEnabled(false)
        lChart.setNoDataText(getString(R.string.nodata))

        val lXAs = lChart.xAxis
        lXAs.setDrawGridLines(false)
        lXAs.textSize = 10.0f
        lXAs.labelCount = 30

        var minVal = 50
        var maxVal = -999

        val lDataTMin = ArrayList<Entry>()
        val lDataTMax = ArrayList<Entry>()
        val lLabels = ArrayList<String>()

        for (i in 0..29) {
            val tempMin = stats[i].minTemperatuur.toFloat()
            val tempMax = stats[i].maxTemperatuur.toFloat()
            if (tempMin != 999f && tempMin < minVal) minVal = Math.round(tempMin)
            if (tempMax != -999f && tempMax > maxVal) maxVal = Math.round(tempMax)
            lDataTMin.add(Entry(i.toFloat(), if (tempMin == 999f) 0f else tempMin))
            lDataTMax.add(Entry(i.toFloat(), if (tempMax == -999f) 0f else tempMax))
            val sDatum = if (i == 0 || i == 10 || i == 20 || i == 29) Helper.dmFormat.print(stats[i].datum) else ""
            lLabels.add(sDatum)
        }

        lXAs.valueFormatter = IndexAxisValueFormatter(lLabels)

        minVal -= 1

        if (maxVal == -999) {
            maxVal = 9
            minVal = 0
        }

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

        val lValueFormat = IValueFormatter { _, _, _, _ -> "" }

        val lDsMin = LineDataSet(lDataTMin, getString(R.string.MinTemp))
        lDsMin.color = ContextCompat.getColor(this, R.color.colorPrimary)
        lDsMin.setCircleColor(ContextCompat.getColor(this, R.color.colorTekst))
        lDsMin.setCircleColorHole(ContextCompat.getColor(this, R.color.colorTekst))
        lDsMin.circleRadius = 2.5f
        lDsMin.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        lDsMin.cubicIntensity = 0.2f
        lDsMin.valueFormatter = lValueFormat
        lDsMin.axisDependency = YAxis.AxisDependency.LEFT

        val lDsMax = LineDataSet(lDataTMax, getString(R.string.MaxTemp))
        lDsMax.color = ContextCompat.getColor(this, R.color.colorTemperatuurDark)
        lDsMax.setCircleColor(ContextCompat.getColor(this, R.color.colorTemperatuur))
        lDsMax.setCircleColorHole(ContextCompat.getColor(this, R.color.colorTemperatuur))
        lDsMax.circleRadius = 2.5f
        lDsMax.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        lDsMax.cubicIntensity = 0.2f
        lDsMax.valueFormatter = lValueFormat
        lDsMax.axisDependency = YAxis.AxisDependency.LEFT

        val lData = LineData(lDsMin, lDsMax)
        lChart.data = lData
        lChart.animateXY(500, 500)
        lChart.invalidate()
    }

    private class AsyncGetStatistiekenTask internal constructor(context: StatsPerDatumActivity) : AsyncTask<Context, Void, ArrayList<Statistiek1Dag>>() {
        private val activityWeakReference: WeakReference<StatsPerDatumActivity> = WeakReference(context)

        override fun doInBackground(vararg params: Context): ArrayList<Statistiek1Dag> {
            val context = params[0]
            val dh = DatabaseHelper.getInstance(context)
            return dh.GetStatistiek30Dagen(datum)
        }

        override fun onPostExecute(stats: ArrayList<Statistiek1Dag>) {
            val activity = activityWeakReference.get()
            activity?.toonStatistiekenPerDag(stats)
        }

    }

    companion object {
        internal var datum = DateTime(DateTime.now().year, DateTime.now().monthOfYear, DateTime.now().dayOfMonth, 0, 0).minusDays(29)
    }
}