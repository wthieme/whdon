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
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet

import org.joda.time.DateTime

import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.Locale

class StatsPerMaandActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.per_maand_statistieken)

        val fabTerug = findViewById<FloatingActionButton>(R.id.btnTerug)
        fabTerug.setOnClickListener { terug() }
        initSwipes()
        toondataBackground()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSwipes() {
        val sl = object : OnSwipeTouchListener(this@StatsPerMaandActivity) {
            override fun onSwipeLeft() {
                mJaar++
                toondataBackground()
            }

            override fun onSwipeRight() {
                mJaar--
                toondataBackground()
            }
        }

        val rlPerMaand = findViewById<RelativeLayout>(R.id.rlPerMaand)
        rlPerMaand.setOnTouchListener(sl)

        val bChart = findViewById<BarChart>(R.id.bcPerMaand)
        bChart.setOnTouchListener(sl)

        val lChart = findViewById<LineChart>(R.id.lcPerMaand)
        lChart.setOnTouchListener(sl)

        Helper.showMessage(this@StatsPerMaandActivity, getString(R.string.SwipeLinksOfRechts))
    }

    private fun terug() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun toondataBackground() {
        val context = applicationContext
        AsyncGetStatistiekenPerMaandTask(this).execute(context)
    }

    @SuppressLint("SetTextI18n")
    private fun toonStatistiekenPerMaand(stats: ArrayList<Statistiek1Maand>?) {
        if (stats == null || stats.size == 0) {
            return
        }
        val tvMaandTitel = findViewById<TextView>(R.id.tvMaandTitel)
        val maand = DateTime.now().monthOfYear
        val vanaf: DateTime

        vanaf = if (maand == 12)
            DateTime(mJaar, 1, 1, 0, 0)
        else
            DateTime(mJaar - 1, maand + 1, 1, 0, 0)

        val maandva = vanaf.toString("MMM", Locale.getDefault()).replace(".", "")
        val maandtm = vanaf.plusMonths(11).toString("MMM", Locale.getDefault()).replace(".", "")

        tvMaandTitel.text = String.format(getString(R.string.per_maand_titel),
                maandva,
                Integer.toString(mJaar - 1),
                maandtm,
                Integer.toString(mJaar))
        val bChart = findViewById<BarChart>(R.id.bcPerMaand)
        bChart.isHighlightPerTapEnabled = false
        bChart.isHighlightPerDragEnabled = false
        bChart.isAutoScaleMinMaxEnabled = true
        val bDesc = Description()
        bDesc.text = ""
        bChart.description = bDesc
        bChart.setNoDataText(getString(R.string.nodata))
        bChart.setScaleEnabled(false)
        val bXAs = bChart.xAxis
        bXAs.setDrawGridLines(false)
        bXAs.setDrawLabels(true)
        bXAs.setDrawAxisLine(false)
        bXAs.yOffset = 5.0f
        bXAs.textSize = 10.0f
        bXAs.labelCount = 12

        val leftAxis = bChart.axisLeft
        leftAxis.setLabelCount(6, true)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 100f

        val rightAxis = bChart.axisRight
        rightAxis.setLabelCount(6, true)
        rightAxis.axisMinimum = 0f
        rightAxis.axisMaximum = 100f

        val bDataT = ArrayList<BarEntry>()
        val bLabels = ArrayList<String>()

        for (i in 0..11) {
            val aantalNat = stats[i].aantalNat
            val aantalDroog = stats[i].aantalDroog
            val totaal = aantalNat + aantalDroog
            var percDroog = 0
            var percNat = 0

            if (totaal > 0) {
                percDroog = Math.round(100.0f * aantalDroog / totaal)
                percNat = 100 - percDroog
            }

            val datum = DateTime(2000, stats[i].maand, 1, 0, 0)
            bDataT.add(BarEntry(i.toFloat(), floatArrayOf(percNat.toFloat(), percDroog.toFloat())))
            bLabels.add(datum.toString("MMM", Locale.getDefault()))
        }

        bXAs.valueFormatter = IndexAxisValueFormatter(bLabels)

        val bDsT = BarDataSet(bDataT, "")
        bDsT.stackLabels = arrayOf(this.getString(R.string.NatTxt), this.getString(R.string.DroogTxt))
        bDsT.setColors(ContextCompat.getColor(this, R.color.colorNatStart), ContextCompat.getColor(this, R.color.colorDroogStart))

        val bValueFormat = IValueFormatter { _, _, _, _ -> "" }

        bDsT.valueFormatter = bValueFormat
        bDsT.axisDependency = YAxis.AxisDependency.LEFT

        val bDataSets = ArrayList<IBarDataSet>()
        bDataSets.add(bDsT)
        val data = BarData(bDataSets)
        bChart.data = data
        bChart.animateXY(500, 500)
        bChart.invalidate()

        // Gemmiddelde temperatuur per maand
        val lChart = findViewById<LineChart>(R.id.lcPerMaand)
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
        lXAs.setLabelCount(12, true)

        var minVal = 50
        var maxVal = -999

        val lDataTMin = ArrayList<Entry>()
        val lDataTAvg = ArrayList<Entry>()
        val lDataTMax = ArrayList<Entry>()
        val lLabels = ArrayList<String>()

        for (i in 0..11) {
            val tempMin = stats[i].minTemperatuur
            val tempAvg = stats[i].avgTemperatuur
            val tempMax = stats[i].maxTemperatuur
            if (tempMin != 999f && tempMin < minVal) minVal = Math.round(tempMin)
            if (tempAvg != 999f && tempAvg < minVal) minVal = Math.round(tempAvg)
            if (tempMax != -999f && tempMax > maxVal) maxVal = Math.round(tempMax)
            lDataTMin.add(Entry(i.toFloat(), if (tempMin == 999f) 0f else tempMin))
            lDataTAvg.add(Entry(i.toFloat(), if (tempAvg == 999f) 0f else tempAvg))
            lDataTMax.add(Entry(i.toFloat(), if (tempMax == -999f) 0f else tempMax))
            val datum = DateTime(2000, stats[i].maand, 1, 0, 0)
            lLabels.add(datum.toString("MMM", Locale.getDefault()))
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

        val lDsMin = LineDataSet(lDataTMin, "Minimum")
        lDsMin.color = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        lDsMin.setCircleColor(ContextCompat.getColor(this, R.color.colorTekst))
        lDsMin.setCircleColorHole(ContextCompat.getColor(this, R.color.colorTekst))
        lDsMin.circleRadius = 2.5f
        lDsMin.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        lDsMin.cubicIntensity = 0.2f
        lDsMin.valueFormatter = lValueFormat
        lDsMin.axisDependency = YAxis.AxisDependency.LEFT

        val lDsAvg = LineDataSet(lDataTAvg, "Gemiddeld")
        lDsAvg.color = ContextCompat.getColor(this, R.color.colorAvgDark)
        lDsAvg.setCircleColor(ContextCompat.getColor(this, R.color.colorAvg))
        lDsAvg.setCircleColorHole(ContextCompat.getColor(this, R.color.colorAvg))
        lDsAvg.circleRadius = 2.5f
        lDsAvg.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        lDsAvg.cubicIntensity = 0.2f
        lDsAvg.valueFormatter = lValueFormat
        lDsAvg.axisDependency = YAxis.AxisDependency.LEFT

        val lDsMax = LineDataSet(lDataTMax, "Maximum temperatuur")
        lDsMax.color = ContextCompat.getColor(this, R.color.colorTemperatuurDark)
        lDsMax.setCircleColor(ContextCompat.getColor(this, R.color.colorTemperatuur))
        lDsMax.setCircleColorHole(ContextCompat.getColor(this, R.color.colorTemperatuur))
        lDsMax.circleRadius = 2.5f
        lDsMax.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        lDsMax.cubicIntensity = 0.2f
        lDsMax.valueFormatter = lValueFormat
        lDsMax.axisDependency = YAxis.AxisDependency.LEFT

        val lData = LineData(lDsMin, lDsAvg, lDsMax)
        lChart.data = lData
        lChart.animateXY(500, 500)
        lChart.invalidate()
    }

    private class AsyncGetStatistiekenPerMaandTask internal constructor(context: StatsPerMaandActivity) : AsyncTask<Context, Void, ArrayList<Statistiek1Maand>>() {
        private val activityWeakReference: WeakReference<StatsPerMaandActivity> = WeakReference(context)

        override fun doInBackground(vararg params: Context): ArrayList<Statistiek1Maand> {
            val context = params[0]
            val dh = DatabaseHelper.getInstance(context)
            val maand = DateTime.now().monthOfYear
            return dh.getStatistiek12Maanden(mJaar, maand)
        }

        override fun onPostExecute(stats: ArrayList<Statistiek1Maand>) {
            val activity = activityWeakReference.get()
            activity?.toonStatistiekenPerMaand(stats)
        }
    }

    companion object {
        internal var mJaar = DateTime.now().year
    }
}