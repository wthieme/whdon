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

class EigenMeldingenActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.eigen_meldingen)

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
        val cxt = applicationContext
        AsyncGetEigenMeldingenTask(this).execute(cxt)
    }

    @SuppressLint("DefaultLocale")
    private fun toonEigenMeldingen(meldingen: ArrayList<Melding>?) {
        if (meldingen == null || meldingen.size == 0) {
            return
        }

        val tvPsSinds = findViewById<TextView>(R.id.tvPsSinds)
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
            aantalDroog += if (rMeld.droog!!) 1 else 0
            aantalNat += if (rMeld.nat!!) 1 else 0
            if (rMeld.temperatuur != 999) {
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

        tvPsSinds.text = Helper.dFormat.print(datum)
        tvPsAantalDroog.text = String.format("%d", aantalDroog)
        tvPsAantalNat.text = String.format("%d", aantalNat)
        tvPsGemm.text = String.format("%.1f", aantalGemm)
        if (aantalTemp > 0) {
            tempGemm = tempSom / (1.0f * aantalTemp)
            tvPsGemmTemp.text = String.format("%.1f", tempGemm)
        }

        val lvEigenMeldingen = findViewById<ListView>(R.id.lvEigenMeldingen)
        lvEigenMeldingen.adapter = CustomListAdapterMeldingen(this, meldingen)
    }

    private class AsyncGetEigenMeldingenTask internal constructor(context: EigenMeldingenActivity) : AsyncTask<Context, Void, ArrayList<Melding>>() {
        private val activityWeakReference: WeakReference<EigenMeldingenActivity> = WeakReference(context)

        override fun doInBackground(vararg params: Context): ArrayList<Melding> {
            val context = params[0]
            val id = Helper.getGuid(context)
            val dh = DatabaseHelper.getInstance(context)
            return dh.getMeldingen(id)
        }

        override fun onPostExecute(meldingen: ArrayList<Melding>) {
            val activity = activityWeakReference.get()
            activity?.toonEigenMeldingen(meldingen)
        }
    }
}