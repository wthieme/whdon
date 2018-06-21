package nl.whitedove.washetdroogofniet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView

import java.lang.ref.WeakReference
import java.util.ArrayList

class StatsPerPlaatsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.per_plaats_statistieken)
        initFab()
        toondataBackground()
    }

    private fun initFab() {
        val fabTerug = findViewById<FloatingActionButton>(R.id.btnTerug)
        fabTerug.setOnClickListener { terug() }
    }

    private fun stat1Plaats(locatie: String?) {
        val intent = Intent(this, Stats1PlaatsActivity::class.java)
        intent.putExtra("Locatie", locatie)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun terug() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun toondataBackground() {
        val context = applicationContext
        AsyncGetStatistiekenPerPlaatsTask(this).execute(context)
    }

    private fun toonPerPlaatsStatistieken(stats: ArrayList<Statistiek>?) {
        if (stats == null || stats.size <= 1) {
            return
        }
        val lvStats = findViewById<ListView>(R.id.lvStats)

        val adapter = CustomListAdapterTotStats(this, stats)
        lvStats.adapter = adapter

        val etZoek = findViewById<EditText>(R.id.etZoek)
        etZoek.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                           after: Int) {
            }

            override fun afterTextChanged(s: Editable) {}
        })

        lvStats.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val stat = parent.getItemAtPosition(position) as Statistiek
            stat1Plaats(stat.locatie)
        }

    }

    private class AsyncGetStatistiekenPerPlaatsTask internal constructor(context: StatsPerPlaatsActivity) : AsyncTask<Context, Void, ArrayList<Statistiek>>() {
        private val activityWeakReference: WeakReference<StatsPerPlaatsActivity> = WeakReference(context)

        override fun doInBackground(vararg params: Context): ArrayList<Statistiek> {
            val context = params[0]
            val dh = DatabaseHelper.getInstance(context)
            return dh.GetStatistieken()
        }

        override fun onPostExecute(stats: ArrayList<Statistiek>) {

            val activity = activityWeakReference.get()
            activity?.toonPerPlaatsStatistieken(stats)
        }
    }
}