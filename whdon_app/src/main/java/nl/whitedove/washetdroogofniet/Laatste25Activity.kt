package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.widget.ListView
import android.widget.TextView

import org.joda.time.DateTime
import org.joda.time.Days

import java.lang.ref.WeakReference
import java.util.ArrayList

import nl.whitedove.washetdroogofniet.backend.whdonApi.model.Melding

class Laatste25Activity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.laatste25)

        val fabTerug = findViewById<FloatingActionButton>(R.id.btnTerug)
        fabTerug.setOnClickListener { terug() }
        toondataBackground()
    }

    private fun terug() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun toondataBackground() {
        val context = applicationContext
        AsyncGetLaatste25Task(this).execute(context)
    }

    @SuppressLint("DefaultLocale")
    private fun toonLaatste25(meldingen: ArrayList<Melding>?) {
        if (meldingen == null || meldingen.size == 0) {
            return
        }

        val tvPsAantalDroog = findViewById<TextView>(R.id.tvPsAantalDroog)
        val tvPsAantalNat = findViewById<TextView>(R.id.tvPsAantalNat)
        val tvPsGemm = findViewById<TextView>(R.id.tvPsGemm)
        val tvPsGemmTemp = findViewById<TextView>(R.id.tvPsGemmTemp)

        var aantalDroog = 0
        var aantalNat = 0
        var tempSom: Long = 0
        val aantalGemm: Float
        val tempGemm: Float
        var aantalTemp = 0
        var datum = DateTime.now()

        for (rMeld in meldingen) {
            aantalDroog += if (rMeld.droog) 1 else 0
            aantalNat += if (rMeld.nat) 1 else 0
            if (rMeld.temperatuur != 999L) {
                tempSom += rMeld.temperatuur!!
                aantalTemp++
            }
            val melddat = DateTime(rMeld.datum)
            if (melddat.isBefore(datum)) {
                datum = DateTime(rMeld.datum)
            }
        }

        val aantalDagen = Days.daysBetween(datum, DateTime.now()).days + 1
        aantalGemm = (aantalNat + aantalDroog) / (1.0f * aantalDagen)

        tvPsAantalDroog.text = String.format("%d", aantalDroog)
        tvPsAantalNat.text = String.format("%d", aantalNat)
        tvPsGemm.text = String.format("%.1f", aantalGemm)

        if (aantalTemp > 0) {
            tempGemm = tempSom / (1.0f * aantalTemp)
            tvPsGemmTemp.text = String.format("%.1f", tempGemm)
        }

        val lvLaatste25 = findViewById<ListView>(R.id.lvLaatste25)
        lvLaatste25.adapter = CustomListAdapterMeldingen(this, meldingen)
    }

    private class AsyncGetLaatste25Task internal constructor(context: Laatste25Activity) : AsyncTask<Context, Void, ArrayList<Melding>>() {
        private val activityWeakReference: WeakReference<Laatste25Activity> = WeakReference(context)

        override fun doInBackground(vararg params: Context): ArrayList<Melding> {
            val context = params[0]
            val dh = DatabaseHelper.getInstance(context)
            return dh.GetLaatste25Meldingen()
        }

        override fun onPostExecute(meldingen: ArrayList<Melding>) {
            val activity = activityWeakReference.get()
            activity?.toonLaatste25(meldingen)
        }
    }
}