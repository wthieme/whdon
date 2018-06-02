package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.lang.ref.WeakReference
import java.util.*

class BuienradarActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.buienradar)

        val fabTerug = findViewById<FloatingActionButton>(R.id.btnTerug)
        fabTerug.setOnClickListener { Terug() }
        ToondataBackground()
    }

    private fun Terug() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun ToondataBackground() {
        val cxt = applicationContext
        if (!Helper.TestInternet(cxt)) {
            return
        }
        AsyncGetWeerVoorspellingTask(this).execute(cxt)
    }

    @SuppressLint("DefaultLocale")
    private fun ToonBuienData(weerData: BuienData?) {
        if (weerData == null || weerData.regenData == null || weerData.regenData!!.size == 0) {
            return
        }
        val chart = findViewById<LineChart>(R.id.lcBuienRadar)
        chart.isHighlightPerTapEnabled = false
        chart.isHighlightPerDragEnabled = false
        chart.setVisibleYRangeMaximum(255f, YAxis.AxisDependency.LEFT)
        val desc = Description()
        desc.text = ""
        chart.description = desc
        chart.setScaleEnabled(false)
        chart.setNoDataText(getString(R.string.nodata))

        val myYFormat = IAxisValueFormatter { value, axis ->
            if (value >= 0 && value < 40)
                "0"
            else if (value >= 40 && value < 80)
                "1"
            else if (value >= 80 && value < 120)
                "2"
            else if (value >= 120 && value < 160)
                "3"
            else if (value >= 160 && value < 200)
                "4"
            else if (value >= 200 && value < 240)
                "5"
            else
                ""
        }

        val yAs1 = chart.axisLeft
        yAs1.axisMaximum = 255f
        yAs1.axisMinimum = 0f
        yAs1.setLabelCount(7, true)
        yAs1.valueFormatter = myYFormat

        val yAs2 = chart.axisRight
        yAs2.setDrawLabels(false)
        yAs2.axisMaximum = 255f
        yAs2.axisMinimum = 0f
        yAs2.setLabelCount(0, true)

        val xAs = chart.xAxis
        xAs.setDrawGridLines(false)

        val tvDroogBr = findViewById<TextView>(R.id.tvDroogBr)
        val sBr = WeerHelper.BepaalBrDataTxt(this, weerData)
        tvDroogBr.text = sBr

        // De markeerlijn voor nu
        val xPos = WeerHelper.BerekenNuXPositie(weerData)
        val ll = LimitLine(xPos, "Nu")
        ll.lineColor = Color.RED
        ll.lineWidth = 1f
        ll.textColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        ll.textSize = 12f
        xAs.addLimitLine(ll)
        xAs.setDrawLimitLinesBehindData(true)

        val dataT = ArrayList<Entry>()
        val labels = ArrayList<String>()

        var mm = 0.0
        var som = 0
        for (i in 0 until weerData.regenData!!.size) {
            val regen = weerData.regenData!![i].regen
            som += regen
            val peruur: Double
            if (regen == 0) {
                peruur = 0.0
            } else {
                peruur = Math.pow(10.0, ((regen - 109.0f) / 32.0f).toDouble())
            }

            mm += peruur / 12.0f
            dataT.add(Entry(i.toFloat(), regen.toFloat()))
            labels.add(weerData.regenData!![i].tijd)
        }

        if (som == 0) {
            tvDroogBr.visibility = View.VISIBLE
            yAs1.setDrawGridLines(false)
            yAs2.setDrawGridLines(false)
        } else {
            tvDroogBr.visibility = View.GONE
        }

        xAs.valueFormatter = IndexAxisValueFormatter(labels)

        val dsT = LineDataSet(dataT, "Intensiteit: 1 (lichte regen) t/m 5 (tropische regen)")
        dsT.color = ContextCompat.getColor(this, R.color.colorNatStart)
        dsT.setDrawFilled(true)
        dsT.fillColor = ContextCompat.getColor(this, R.color.colorNatStart)
        dsT.setDrawCircles(false)
        dsT.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        dsT.cubicIntensity = 0.2f
        val myValueFormat = IValueFormatter { value, entry, dataSetIndex, viewPortHandler -> "" }

        dsT.valueFormatter = myValueFormat
        dsT.axisDependency = YAxis.AxisDependency.LEFT

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dsT)

        val data = LineData(dataSets)
        chart.data = data
        chart.animateXY(500, 500)
        chart.invalidate()

        val tvNeerslag = findViewById<TextView>(R.id.tvNeerslag)
        tvNeerslag.text = String.format("%.2f mm", mm)
    }

    private class AsyncGetWeerVoorspellingTask internal constructor(context: BuienradarActivity) : AsyncTask<Context, Void, BuienData>() {
        private val activityWeakReference: WeakReference<BuienradarActivity>

        init {
            activityWeakReference = WeakReference(context)
        }

        override fun doInBackground(vararg params: Context): BuienData? {

            var weer: BuienData? = null
            try {
                weer = WeerHelper.BepaalBuien()
            } catch (ignored: Exception) {
            }

            return weer
        }

        override fun onPostExecute(result: BuienData) {
            val activity = activityWeakReference.get()
            activity?.ToonBuienData(result)
        }

    }
}