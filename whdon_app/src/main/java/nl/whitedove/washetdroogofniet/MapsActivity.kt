package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Pair
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.Executors

class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        initFab()
        initRadio()
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun terug() {
        val myIntent = intent // gets the previously created intent
        val locatie = myIntent.getStringExtra("Locatie")

        if (locatie.isNullOrEmpty()) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else {
            val intent = Intent(this, StatsPerPlaatsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    private fun initFab() {
        val fabTerug = findViewById<FloatingActionButton>(R.id.btnTerug)
        fabTerug.setOnClickListener { terug() }
    }

    private fun initRadio() {
        val rbStandaard = findViewById<RadioButton>(R.id.rbStandaard)
        val rbSatelliet = findViewById<RadioButton>(R.id.rbSatelliet)
        val rgStandaardSatelliet = findViewById<RadioGroup>(R.id.rgStandaardSatelliet)
        rbStandaard.isChecked = mStandaardSatelliet === Helper.MapsDisplay.Standaard
        rbSatelliet.isChecked = mStandaardSatelliet === Helper.MapsDisplay.Satelliet
        val cl = RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            val rb = radioGroup.findViewById<RadioButton>(checkedId)
            if (rb.id == R.id.rbStandaard) {
                mStandaardSatelliet = Helper.MapsDisplay.Standaard
            }

            if (rb.id == R.id.rbSatelliet) {
                mStandaardSatelliet = Helper.MapsDisplay.Satelliet
            }
            toonmapsDisplay()
        }
        rgStandaardSatelliet.setOnCheckedChangeListener(cl)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        toonmapsDisplay()
        toondataBackground()
    }

    private fun toonmapsDisplay() {
        if (mStandaardSatelliet == Helper.MapsDisplay.Satelliet) {
            mMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID
        }

        if (mStandaardSatelliet == Helper.MapsDisplay.Standaard) {
            mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        }

    }

    private fun toondataBackground() {
        val context = applicationContext
        val myIntent = intent // gets the previously created intent
        val locatie = myIntent.getStringExtra("Locatie")
        val land = myIntent.getStringExtra("Land")
        if (locatie.isNullOrEmpty()) {
            val latlng = LocationHelper.bepaalLatLng(this)
            if (latlng != null) {
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, Helper.ZOOM))
            }
            AsyncGetPersoonlijkeStatsTask(this).execute(context)
        } else {
            val dh = DatabaseHelper.getInstance(context)
            val stat1 = dh.getStatistiekLocatie(locatie)
            val stat = Statistiek()
            stat.aantalDroog = stat1.aantalDroog
            stat.aantalNat = stat1.aantalNat
            stat.locatie = locatie
            stat.land = land
            val latlng = LocationHelper.getLocationFromAddress(context, stat.locatie, stat.land)
            if (latlng != null) {
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, Helper.ZOOM1Plaats))
            }
            AsyncDoeEenLocatie(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Pair.create(context, stat))
        }
    }

    @SuppressLint("MissingPermission")
    private fun toonLocaties(stats: ArrayList<Statistiek>) {
        mMap!!.clear()
        mMap!!.isTrafficEnabled = false
        mMap!!.isMyLocationEnabled = false

        val context = applicationContext
        for (stat in stats) {
            val runnableTask = {
                AsyncDoeEenLocatie(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Pair.create(context, stat))
            }
            mExecutor.submit(runnableTask)
        }
    }

    private fun toon1Locatie(stat: Statistiek, ll: LatLng?) {
        if (ll == null) return
        mMap!!.addMarker(MarkerOptions().position(ll).title(stat.locatie +
                " (" + stat.aantalDroog.toString() + " " + getString(R.string.DroogTxt) +
                " " + stat.aantalNat.toString() + " " + getString(R.string.NatTxt) + ")"))
    }

    private class AsyncGetPersoonlijkeStatsTask internal constructor(context: MapsActivity) : AsyncTask<Context, Void, ArrayList<Statistiek>>() {
        private val activityWeakReference: WeakReference<MapsActivity> = WeakReference(context)

        override fun doInBackground(vararg params: Context): ArrayList<Statistiek> {
            val context = params[0]
            val id = Helper.getGuid(context)
            val dh = DatabaseHelper.getInstance(context)
            return dh.getPersoonlijkeStatsPerPlaats(id)
        }

        override fun onPostExecute(stats: ArrayList<Statistiek>) {
            val activity = activityWeakReference.get()
            activity?.toonLocaties(stats)
        }
    }

    private class AsyncDoeEenLocatie internal constructor(context: MapsActivity) : AsyncTask<Pair<Context, Statistiek>, Void, Pair<Statistiek, LatLng>>() {
        private val activityWeakReference: WeakReference<MapsActivity> = WeakReference(context)

        @SafeVarargs
        override fun doInBackground(vararg params: Pair<Context, Statistiek>): Pair<Statistiek, LatLng> {
            val context = params[0].first
            val stat = params[0].second
            val ll = LocationHelper.getLocationFromAddress(context, stat.locatie, stat.land)
            return Pair.create(stat, ll)
        }

        override fun onPostExecute(stat_ll: Pair<Statistiek, LatLng>) {
            val activity = activityWeakReference.get()
            activity?.toon1Locatie(stat_ll.first, stat_ll.second)
        }
    }

    companion object {
        internal var mStandaardSatelliet: Helper.MapsDisplay = Helper.MapsDisplay.Standaard
        internal var mExecutor = Executors.newFixedThreadPool(10)
    }
}
