package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.ref.WeakReference
import java.util.*


class StatsPerPlaatsActivity : Activity() {

    private var touchPositionX: Int = -1

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

    private fun stat1Plaats(stat: Statistiek) {
        val intent = Intent(this, Stats1PlaatsActivity::class.java)
        intent.putExtra("Locatie", stat.locatie)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun map1Plaats(stat: Statistiek) {
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("Locatie", stat.locatie)
        intent.putExtra("Land", stat.land)
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

    @SuppressLint("ClickableViewAccessibility")
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

        lvStats.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                touchPositionX = event.x.toInt()
                return false
            }
        })

        lvStats.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val stat = parent.getItemAtPosition(position) as Statistiek
            val x = touchPositionX.toFloat() / parent.width.toFloat()
            if (x > 0.9f)
                map1Plaats(stat)
            else
                stat1Plaats(stat)
        }
    }

    private class AsyncGetStatistiekenPerPlaatsTask internal constructor(context: StatsPerPlaatsActivity) : AsyncTask<Context, Void, ArrayList<Statistiek>>() {
        private val activityWeakReference: WeakReference<StatsPerPlaatsActivity> = WeakReference(context)

        override fun doInBackground(vararg params: Context): ArrayList<Statistiek> {
            val context = params[0]
            val dh = DatabaseHelper.getInstance(context)
            return dh.getStatistieken()
        }

        override fun onPostExecute(stats: ArrayList<Statistiek>) {

            val activity = activityWeakReference.get()
            activity?.toonPerPlaatsStatistieken(stats)
        }
    }
}