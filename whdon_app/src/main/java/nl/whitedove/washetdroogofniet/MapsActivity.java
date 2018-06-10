package nl.whitedove.washetdroogofniet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        FloatingActionButton fabTerug = findViewById(R.id.btnTerug);
        fabTerug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Terug();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void Terug() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng latlng = LocationHelper.INSTANCE.bepaalLatLng(this);
        if (latlng != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, Helper.ZOOM));
        }
        ToondataBackground();
    }

    private void ToondataBackground() {
        Context context = getApplicationContext();
        new MapsActivity.AsyncGetPersoonlijkeStatsTask(this).execute(context);
    }

    @SuppressWarnings("unchecked")
    @SuppressLint("MissingPermission")
    private void ToonLocaties(ArrayList<Statistiek> stats) {
        mMap.clear();
        mMap.setTrafficEnabled(false);
        mMap.setMyLocationEnabled(false);

        for (Statistiek stat : stats) {
            Context context = getApplicationContext();
            new AsyncDoeEenLocatie(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Pair.create(context, stat));
        }
    }

    private void Toon1Locatie(Statistiek stat, LatLng ll) {
        if (ll == null) return;
        mMap.addMarker(new MarkerOptions().position(ll).title(stat.getLocatie() +
                " (" + Integer.toString(stat.getAantalDroog()) + " " + getString(R.string.DroogTxt) +
                " " + Integer.toString(stat.getAantalNat()) + " " + getString(R.string.NatTxt) + ")"));
    }

    private static class AsyncGetPersoonlijkeStatsTask extends AsyncTask<Context, Void, ArrayList<Statistiek>> {
        private WeakReference<MapsActivity> activityWeakReference;

        AsyncGetPersoonlijkeStatsTask(MapsActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected final ArrayList<Statistiek> doInBackground(Context... params) {
            Context context = params[0];
            String id = Helper.INSTANCE.getGuid(context);
            DatabaseHelper dh = DatabaseHelper.Companion.getInstance(context);
            return dh.GetPersoonlijkeStatsPerPlaats(id);
        }

        @Override
        protected void onPostExecute(ArrayList<Statistiek> stats) {
            MapsActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonLocaties(stats);
        }
    }

    private static class AsyncDoeEenLocatie extends AsyncTask<Pair<Context, Statistiek>, Void, Pair<Statistiek, LatLng>> {
        private WeakReference<MapsActivity> activityWeakReference;

        AsyncDoeEenLocatie(MapsActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @SafeVarargs
        @Override
        protected final Pair<Statistiek, LatLng> doInBackground(Pair<Context, Statistiek>... params) {
            Context context = params[0].first;
            Statistiek stat = params[0].second;
            LatLng ll = LocationHelper.INSTANCE.getLocationFromAddress(context, stat.getLocatie() + ", " + stat.getLand());
            return Pair.create(stat, ll);
        }

        @Override
        protected void onPostExecute(Pair<Statistiek, LatLng> stat_ll) {
            MapsActivity activity = activityWeakReference.get();
            if (activity != null) activity.Toon1Locatie(stat_ll.first, stat_ll.second);
        }
    }

}
