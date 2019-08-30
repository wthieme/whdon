package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.content.ContextCompat
import android.util.Pair
import android.widget.TextView

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

import org.joda.time.DateTime
import org.joda.time.Days

import java.lang.ref.WeakReference
import java.util.ArrayList

class Stats1PlaatsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.een_plaats_statistieken)

        val fabTerug = findViewById<FloatingActionButton>(R.id.btnTerug)
        fabTerug.setOnClickListener { terug() }

        val myIntent = intent // gets the previously created intent
        val locatie = myIntent.getStringExtra("Locatie")

        toondataBackground(locatie)
    }

    private fun terug() {
        val intent = Intent(this, StatsPerPlaatsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun toondataBackground(locatie: String) {
        val context = applicationContext
        AsyncGetStatistiekLocatieTask(this).execute(Pair.create(context, locatie))
    }

    @SuppressLint("DefaultLocale")
    private fun toonStatistiekLocatie(stat: Statistiek1Plaats?) {
        if (stat?.locatie == null) {
            return
        }

        val tvLocatie = findViewById<TextView>(R.id.tvLocatie)
        val tvPsDatumStart = findViewById<TextView>(R.id.tvPsDatumStart)
        val tvPsDatumEnd = findViewById<TextView>(R.id.tvPsDatumEnd)
        val tvPsAantalDroog = findViewById<TextView>(R.id.tvPsAantalDroog)
        val tvPsAantalNat = findViewById<TextView>(R.id.tvPsAantalNat)
        val tvPsGemm = findViewById<TextView>(R.id.tvPsGemm)
        val tvPsGemmTemp = findViewById<TextView>(R.id.tvPsGemmTemp)

        val aantalDroog = stat.aantalDroog
        val aantalNat = stat.aantalNat
        val totaal = aantalDroog + aantalNat

        val aantalGemm: Float
        val datumStart = DateTime(stat.datumStart)
        val datumEnd = DateTime(stat.datumEnd)

        val aantalDagen = Days.daysBetween(datumStart, datumEnd).days + 1

        aantalGemm = (aantalNat + aantalDroog) / (1.0f * aantalDagen)
        val percDroog = Math.round(100.0f * aantalDroog / totaal)
        val percNat = 100 - percDroog

        tvLocatie.text = stat.locatie
        tvPsDatumStart.text = Helper.dFormat.print(datumStart)
        tvPsDatumEnd.text = Helper.dFormat.print(datumEnd)
        tvPsAantalDroog.text = String.format("%d", aantalDroog)
        tvPsAantalNat.text = String.format("%d", aantalNat)
        tvPsGemm.text = String.format("%.1f", aantalGemm)

        val aantalTemp = stat.aantalTemperatuur

        if (aantalTemp > 0) {
            val tempSom = stat.somTemperatuur
            val tempGemm = 1.0f * tempSom / (1.0f * aantalTemp)
            tvPsGemmTemp.text = String.format("%.1f", tempGemm)
        }

        val chart = findViewById<PieChart>(R.id.pcPerdag)

        val desc = Description()
        desc.text = ""
        chart.description = desc

        chart.setTouchEnabled(false)
        chart.setNoDataText(getString(R.string.nodata))
        chart.legend.isEnabled = false
        chart.setEntryLabelColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        chart.setDrawEntryLabels(true)

        val dataT = ArrayList<PieEntry>()

        dataT.add(PieEntry(percDroog.toFloat(), String.format("%d%%", percDroog)))
        dataT.add(PieEntry(percNat.toFloat(), String.format("%d%%", percNat)))

        val legend = chart.legend
        val le1 = LegendEntry()
        le1.formColor = ContextCompat.getColor(this, R.color.colorNatStart)
        le1.label = this.getString(R.string.NatTxt)
        le1.form = Legend.LegendForm.SQUARE
        le1.formSize = 10f
        val le2 = LegendEntry()
        le2.formColor = ContextCompat.getColor(this, R.color.colorDroogStart)
        le2.label = this.getString(R.string.DroogTxt)
        le2.form = Legend.LegendForm.SQUARE
        le2.formSize = 10f

        legend.setCustom(arrayOf(le1, le2))
        legend.isEnabled = true
        legend.xEntrySpace = 20f
        legend.textSize = 12f
        legend.isWordWrapEnabled = true

        val dsT = PieDataSet(dataT, "")
        dsT.setColors(ContextCompat.getColor(this, R.color.colorDroogStart), ContextCompat.getColor(this, R.color.colorNatStart))
        dsT.setDrawValues(false)

        val data = PieData(dsT)
        data.setValueTextSize(14f)
        data.setValueTextColor(ContextCompat.getColor(this, R.color.colorTekst))

        chart.data = data
        chart.animateXY(500, 500)
        chart.invalidate()
    }

    private class AsyncGetStatistiekLocatieTask internal constructor(context: Stats1PlaatsActivity) : AsyncTask<Pair<Context, String>, Void, Statistiek1Plaats>() {
        private val activityWeakReference: WeakReference<Stats1PlaatsActivity> = WeakReference(context)

        override fun doInBackground(vararg params: Pair<Context, String>): Statistiek1Plaats {
            val context = params[0].first
            val locatie = params[0].second
            val dh = DatabaseHelper.getInstance(context)
            return dh.getStatistiekLocatie(locatie)
        }

        override fun onPostExecute(stat: Statistiek1Plaats) {
            val activity = activityWeakReference.get()
            activity?.toonStatistiekLocatie(stat)
        }
    }
}