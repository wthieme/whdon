package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.FragmentActivity
import android.util.Pair

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import java.lang.ref.WeakReference
import java.util.ArrayList

class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val fabTerug = findViewById<FloatingActionButton>(R.id.btnTerug)
        fabTerug.setOnClickListener { terug() }

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun terug() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
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
        toondataBackground()
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

        for (stat in stats) {
            val context = applicationContext
            AsyncDoeEenLocatie(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Pair.create(context, stat))
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
            return dh.GetPersoonlijkeStatsPerPlaats(id)
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
            val ll = LocationHelper.getLocationFromAddress(context, stat.locatie + ", " + stat.land)
            return Pair.create(stat, ll)
        }

        override fun onPostExecute(stat_ll: Pair<Statistiek, LatLng>) {
            val activity = activityWeakReference.get()
            activity?.toon1Locatie(stat_ll.first, stat_ll.second)
        }
    }

}
