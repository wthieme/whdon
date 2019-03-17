package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.FragmentActivity
import android.util.Pair
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun initFab() {
        val fabTerug = findViewById<FloatingActionButton>(R.id.btnTerug)
        fabTerug.setOnClickListener { terug() }

    }

    private fun initRadio() {
        val rbStandaard = findViewById<RadioButton>(R.id.rbStandaard)
        val rbSatelliet = findViewById<RadioButton>(R.id.rbSatelliet)
        val rgStandaardSatelliet = findViewById<RadioGroup>(R.id.rgStandaardSatelliet)
        rbStandaard.isChecked = MapsActivity.mStandaardSatelliet === Helper.MapsDisplay.Standaard
        rbSatelliet.isChecked = MapsActivity.mStandaardSatelliet === Helper.MapsDisplay.Satelliet
        val cl = RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            val rb = radioGroup.findViewById<RadioButton>(checkedId)
            if (rb.id == R.id.rbStandaard) {
                MapsActivity.mStandaardSatelliet = Helper.MapsDisplay.Standaard
            }

            if (rb.id == R.id.rbSatelliet) {
                MapsActivity.mStandaardSatelliet = Helper.MapsDisplay.Satelliet
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
        val latlng = LocationHelper.bepaalLatLng(this)
        if (latlng != null) {
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, Helper.ZOOM))
        }
        toonmapsDisplay()
        toondataBackground()
    }

    private fun toonmapsDisplay() {
        if (mStandaardSatelliet == Helper.MapsDisplay.Satelliet) {
            mMap!!.setMapType(GoogleMap.MAP_TYPE_HYBRID)
        };

        if (mStandaardSatelliet == Helper.MapsDisplay.Standaard) {
            mMap!!.setMapType(GoogleMap.MAP_TYPE_NORMAL)
        };

    }

    private fun toondataBackground() {
        val context = applicationContext
        MapsActivity.AsyncGetPersoonlijkeStatsTask(this).execute(context)
    }

    @SuppressLint("MissingPermission")
    private fun toonLocaties(stats: ArrayList<Statistiek>) {
        mMap!!.clear()
        mMap!!.isTrafficEnabled = false
        mMap!!.isMyLocationEnabled = false

        val context = applicationContext
        //for (stat in stats) {
        //    AsyncDoeEenLocatie(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Pair.create(context, stat))
        //}

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
                " (" + Integer.toString(stat.aantalDroog) + " " + getString(R.string.DroogTxt) +
                " " + Integer.toString(stat.aantalNat) + " " + getString(R.string.NatTxt) + ")"))
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
