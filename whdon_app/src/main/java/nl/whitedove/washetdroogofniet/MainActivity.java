package nl.whitedove.washetdroogofniet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.joda.time.DateTime;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.whitedove.washetdroogofniet.backend.whdonApi.model.Melding;
import nl.whitedove.washetdroogofniet.backend.whdonApi.model.MeldingCollection;

public class MainActivity extends Activity {

    DatabaseHelper mDH;
    ProgressDialog mProgress;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fabJa = (FloatingActionButton) findViewById(R.id.btnJa);
        fabJa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VerwerkJa();
            }
        });

        FloatingActionButton fabNee = (FloatingActionButton) findViewById(R.id.btnNee);
        fabNee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VerwerkNee();
            }
        });

        FloatingActionButton fabMenu = (FloatingActionButton) findViewById(R.id.btnMenu);
        fabMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewMenu();
            }
        });

        BarChart bcBuienRadar = (BarChart) findViewById(R.id.bcBuienRadar);
        bcBuienRadar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BuienRadar();
            }
        });

        FrameLayout flPersStats = (FrameLayout) findViewById(R.id.flPersStats);
        flPersStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EigenMeldingen();
            }
        });

        ToonSpreukVdDag();
        Init();
        ToondataBackground();
    }

    @SuppressLint("InflateParams")
    public void NewMenu() {

        List<ContextMenuItem> contextMenuItems;
        final Dialog customDialog = new Dialog(this);

        LayoutInflater inflater;
        View child;
        ListView listView;
        ContextMenuAdapter adapter;

        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        child = inflater.inflate(R.layout.listview_context_menu, null);
        listView = (ListView) child.findViewById(R.id.listView_context_menu);

        contextMenuItems = new ArrayList<>();

        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.user), getString(R.string.eigen_meldingen)));
        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.staafhor), getString(R.string.per_plaats)));
        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.staafvert), getString(R.string.aantal_meldingen_per_dag)));
        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.staafvert2), getString(R.string.per_maand)));
        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.lijn1), getString(R.string.aantal_gebruikers)));
        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.list25), getString(R.string.laatste25)));

        adapter = new ContextMenuAdapter(this, contextMenuItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                customDialog.dismiss();
                switch (position) {

                    case 0:
                        EigenMeldingen();
                        return;

                    case 1:
                        GrafiekPlaats();
                        return;

                    case 2:
                        GrafiekDatum();
                        return;

                    case 3:
                        GrafiekMaand();
                        return;

                    case 4:
                        GrafiekAantalGebruikers();
                        return;

                    case 5:
                        Laatste25();
                }
            }
        });

        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(child);
        customDialog.show();
    }

    public void moreClick(View oView) {
        PopupMenu popup = new PopupMenu(this, oView);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.cmenu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.clear_cache:
                        ResetCache();
                        return true;
                }
                return true;
            }
        });

        popup.show();
    }

    private void ResetCache() {
        ClearCache();
        SyncLocalDb(false);
    }

    private void ClearCache() {
        mDH.DeleteMeldingen();
        Context cxt = getApplicationContext();
        Helper.SetLastSyncDate(cxt, new DateTime(2000, 1, 1, 0, 0));
    }

    private void VerwerkJa() {
        Context cxt = getApplicationContext();
        if (!Helper.TestInternet(cxt)) {
            return;
        }
        SlaMeldingOp(true);
    }

    private void VerwerkNee() {
        Context cxt = getApplicationContext();
        if (!Helper.TestInternet(cxt)) {
            return;
        }
        SlaMeldingOp(false);
    }

    private void BuienRadar() {
        Intent intent = new Intent(this, BuienradarActivity.class);
        startActivity(intent);
    }

    private void GrafiekPlaats() {
        Intent intent = new Intent(this, StatsPerPlaatsActivity.class);
        startActivity(intent);
    }

    private void GrafiekAantalGebruikers() {
        Intent intent = new Intent(this, StatsAantalGebruikersActivity.class);
        startActivity(intent);
    }

    private void Laatste25() {
        Intent intent = new Intent(this, Laatste25Activity.class);
        startActivity(intent);
    }

    private void GrafiekDatum() {
        Intent intent = new Intent(this, StatsPerDatumActivity.class);
        startActivity(intent);
    }

    private void GrafiekMaand() {
        Intent intent = new Intent(this, StatsPerMaandActivity.class);
        startActivity(intent);
    }

    private void EigenMeldingen() {
        Intent intent = new Intent(this, EigenMeldingenActivity.class);
        startActivity(intent);
    }

    private void ToonSpreukVdDag() {
        TextView tvSpreuk = (TextView) findViewById(R.id.tvSpreuk);
        DateTime nu = DateTime.now();
        int maand = nu.getMonthOfYear();
        int dag = nu.getDayOfMonth();
        String spreuk = SpreukenHelper.GeefSpreuk(maand, dag);
        tvSpreuk.setText(spreuk);
    }

    @SuppressLint("DefaultLocale")
    private void ToonWeerdata(Weer weerData) {
        String locatie = LocationHelper.GetLocatieVoorWeer(this);
        if (weerData == null) {
            String weeronbekend = this.getString(R.string.WeerOnbekend);
            Helper.ShowMessage(this, weeronbekend);
            TextView tvWeerkop = (TextView) findViewById(R.id.tvWeerkop);
            tvWeerkop.setText(String.format(this.getString(R.string.Weer3uur), locatie));
            InitWeerViews(false);
            return;
        }

        InitWeerViews(true);
        TextView tvWeerkop = (TextView) findViewById(R.id.tvWeerkop);
        tvWeerkop.setText(String.format(this.getString(R.string.Weer3uur), weerData.getPlaats()));

        TextView tvGrad = (TextView) findViewById(R.id.tvGrad);
        tvGrad.setText(String.format("%d °C", weerData.getGraden()));

        TextView tvWind = (TextView) findViewById(R.id.tvWind);
        tvWind.setText(String.format("%d km/h", weerData.getWind()));

        ImageView imWeer = (ImageView) findViewById(R.id.imWeer);
        Context context = imWeer.getContext();
        int id = context.getResources().getIdentifier("i" + weerData.getIcon(), "drawable", context.getPackageName());
        imWeer.setImageResource(id);

        ImageView imWind = (ImageView) findViewById(R.id.imWind);
        int richting = weerData.getWindRichting();

        if (richting > 0 && richting <= 22.5) {
            imWind.setImageResource(R.drawable.noord);
        } else if (richting > 22.5 && richting <= 67.5) {
            imWind.setImageResource(R.drawable.noordoost);
        } else if (richting > 67.5 && richting <= 112.5) {
            imWind.setImageResource(R.drawable.oost);
        } else if (richting > 112.5 && richting <= 157.5) {
            imWind.setImageResource(R.drawable.zuidoost);
        } else if (richting > 157.5 && richting <= 202.5) {
            imWind.setImageResource(R.drawable.zuid);
        } else if (richting > 202.5 && richting <= 247.5) {
            imWind.setImageResource(R.drawable.zuidwest);
        } else if (richting > 247.5 && richting <= 292.5) {
            imWind.setImageResource(R.drawable.west);
        } else if (richting > 292.5 && richting <= 337.5) {
            imWind.setImageResource(R.drawable.noordwest);
        } else if (richting > 337.5 && richting <= 360) {
            imWind.setImageResource(R.drawable.noord);
        }
    }

    private void ToonBuiendata(BuienData weerData) {

        InitBrViews(true);

        BarChart chart = (BarChart) findViewById(R.id.bcBuienRadar);
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.setVisibleYRangeMaximum(255, YAxis.AxisDependency.LEFT);
        chart.setDescription(getString(R.string.buienradar2u));
        chart.setDescriptionPosition(Utils.convertDpToPixel(88), Utils.convertDpToPixel(12));

        chart.setDescriptionTextSize(12);
        chart.setDescriptionColor(ContextCompat.getColor(this, R.color.colorPrimary));
        chart.setScaleEnabled(false);
        chart.setGridBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        chart.setNoDataText("");
        chart.setDrawBorders(true);
        chart.setBorderColor(ContextCompat.getColor(this, R.color.colorTekst));

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        YAxis yAs1 = chart.getAxisLeft();
        yAs1.setAxisMaxValue(255);
        yAs1.setAxisMinValue(0);
        yAs1.setLabelCount(0, true);
        yAs1.setDrawGridLines(false);
        yAs1.setDrawLabels(false);
        yAs1.setDrawAxisLine(false);

        YAxis yAs2 = chart.getAxisRight();
        yAs2.setAxisMaxValue(255);
        yAs2.setAxisMinValue(0);
        yAs2.setLabelCount(0, true);
        yAs2.setDrawGridLines(false);
        yAs2.setDrawLabels(false);
        yAs2.setDrawAxisLine(false);

        XAxis xAs = chart.getXAxis();
        xAs.setDrawGridLines(false);
        xAs.setDrawLabels(false);
        xAs.setDrawAxisLine(false);

        TextView tvDroogBr = (TextView) findViewById(R.id.tvDroogBr);
        String sBr = WeerHelper.BepaalBrDataTxt(this, weerData);
        tvDroogBr.setText(sBr);

        float xPos = WeerHelper.BerekenNuXPositie(weerData);
        LimitLine ll = new LimitLine(xPos, "");
        ll.setLineColor(Color.RED);
        ll.setLineWidth(0.5f);
        xAs.removeAllLimitLines();
        xAs.addLimitLine(ll);
        xAs.setDrawLimitLinesBehindData(true);

        ArrayList<BarEntry> dataT = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();

        int som = 0;
        for (int i = 0; i < weerData.getRegenData().size(); i++) {
            int regen = weerData.getRegenData().get(i).getRegen();
            som += regen;
            dataT.add(new BarEntry(regen, i));
            xVals.add(weerData.getRegenData().get(i).getTijd());
        }

        BarDataSet dsT = new BarDataSet(dataT, "");

        dsT.setColors(new int[]{ContextCompat.getColor(this, R.color.colorNatStart)});
        ValueFormatter myformat = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "";
            }
        };

        dsT.setValueFormatter(myformat);
        dsT.setAxisDependency(YAxis.AxisDependency.LEFT);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dsT);

        BarData data = new BarData(xVals, dataSets);
        chart.setData(data);
        chart.invalidate();

        if (som == 0) {
            tvDroogBr.setVisibility(View.VISIBLE);
        } else {
            tvDroogBr.setVisibility(View.GONE);
        }
    }

    private void ToondataBackground() {
        Context cxt = getApplicationContext();
        String id = Helper.GetGuid(cxt);
        new AsyncGetLaatsteMeldingTask().execute(id);
        new AsyncGetPersoonlijkeStatsTask().execute(id);
        if (!Helper.TestInternet(cxt)) {
            return;
        }
        new AsyncGetWeerVoorspelling().execute(cxt);
        new AsyncGetBuienData().execute(cxt);
    }

    private void SlaMeldingOp(Boolean droog) {
        Context cxt = getApplicationContext();
        String locatie = LocationHelper.GetLocatieVoorWeer(cxt);
        Melding melding = new Melding();
        melding.setDroog(droog);
        melding.setLocatie(locatie);
        melding.setId(Helper.GetGuid(cxt));
        melding.setNat(!droog);

        //noinspection unchecked
        new AsyncSlaMeldingOpTask().execute(Pair.create(cxt, melding));
    }

    private void ToonHuidigeLocatie() {
        TextView tvHuidigeLoc = (TextView) findViewById(R.id.tvHuidigeLocatie);
        tvHuidigeLoc.setText(Helper.mLocatie);
    }

    private void ToonLaatsteMelding(Melding melding) {

        Context cxt = getApplicationContext();
        // Alleen als we geen echte locatie hebben bewaren we de locatie van de laatste melding
        if ((Helper.mLocatie == null) || Helper.mLocatie.equalsIgnoreCase("Onbekend")) {
            LocationHelper.BewaarLocatie(cxt, melding.getLocatie());
        }

        if (Helper.DEBUG) {
            TextView tvLaatste = (TextView) findViewById(R.id.tvLaatste);
            tvLaatste.setTextColor(Color.RED);
        }

        if (melding == null) {
            Helper.ShowMessage(cxt, "Onverwachte fout tijdens ophalen van laatste melding");
            return;
        }

        TextView tvDt = (TextView) findViewById(R.id.tvDatumtijd);
        TextView tvLocatie = (TextView) findViewById(R.id.tvLocatie);
        TextView tvDroogNat = (TextView) findViewById(R.id.tvDroogNat);

        String er = melding.getError();
        if (er != null && !er.isEmpty()) {
            tvDt.setText(er);
            tvLocatie.setText("");
            tvDroogNat.setText("");
            return;
        }

        DateTime lastDate = new DateTime(melding.getDatum());
        String locatie = melding.getLocatie();
        String lastDroogNat = melding.getDroog() ? "Droog" : "Nat";

        tvDt.setText(Helper.dtFormat.print(lastDate));
        tvLocatie.setText(locatie);
        tvDroogNat.setText(lastDroogNat);

        if (melding.getDroog()) {
            tvDroogNat.setTextColor(ContextCompat.getColor(this, R.color.colorDroogStart));
        } else {
            tvDroogNat.setTextColor(ContextCompat.getColor(this, R.color.colorTekst));
        }
    }

    @SuppressLint("DefaultLocale")
    private void ToonPersoonlijkeStat(Statistiek stat) {
        Context cxt = getApplicationContext();
        if (stat == null) {
            Helper.ShowMessage(cxt, "Onverwachte fout tijdens ophalen statistiek");
            return;
        }

        TextView tvDroog = (TextView) findViewById(R.id.tvDroog);
        TextView tvNat = (TextView) findViewById(R.id.tvNat);
        ProgressBar pbStat = (ProgressBar) findViewById(R.id.pbDroogNatStat);
        int aantalDroog = stat.getAantalDroog();
        int aantalNat = stat.getAantalNat();
        int totaal = aantalDroog + aantalNat;

        InitViews(totaal > 0);
        if (totaal > 0) {
            pbStat.setProgress(100 * aantalDroog / totaal);
            int percDroog = Math.round(100.0F * aantalDroog / totaal);
            int percNat = 100 - percDroog;
            tvDroog.setText(String.format("%d%%", percDroog));
            tvNat.setText(String.format("%d%%", percNat));
        }
    }

    private void InitViews(Boolean visible) {
        ToonHuidigeLocatie();
        TextView tvPersStats = (TextView) findViewById(R.id.tvPersStats);
        TextView tvDroog = (TextView) findViewById(R.id.tvDroog);
        TextView tvNat = (TextView) findViewById(R.id.tvNat);
        ProgressBar pbStat = (ProgressBar) findViewById(R.id.pbDroogNatStat);

        if (visible) {
            tvPersStats.setVisibility(View.VISIBLE);
            pbStat.setVisibility(View.VISIBLE);
            tvDroog.setVisibility(View.VISIBLE);
            tvNat.setVisibility(View.VISIBLE);
        } else {
            tvPersStats.setVisibility(View.GONE);
            pbStat.setVisibility(View.GONE);
            tvDroog.setVisibility(View.GONE);
            tvNat.setVisibility(View.GONE);
        }
    }

    private void InitWeerViews(Boolean visible) {

        TextView tvWeerOnBekend = (TextView) findViewById(R.id.tvWeeronbekend);
        TextView tvGrad = (TextView) findViewById(R.id.tvGrad);
        TextView tvWind = (TextView) findViewById(R.id.tvWind);
        ImageView imWeer = (ImageView) findViewById(R.id.imWeer);
        ImageView imWind = (ImageView) findViewById(R.id.imWind);

        if (visible) {
            tvWeerOnBekend.setVisibility(View.GONE);
            tvGrad.setVisibility(View.VISIBLE);
            tvWind.setVisibility(View.VISIBLE);
            imWeer.setVisibility(View.VISIBLE);
            imWind.setVisibility(View.VISIBLE);
        } else {
            tvWeerOnBekend.setVisibility(View.VISIBLE);
            tvGrad.setVisibility(View.GONE);
            tvWind.setVisibility(View.GONE);
            imWeer.setVisibility(View.GONE);
            imWind.setVisibility(View.GONE);
        }
    }

    private void InitBrViews(Boolean visible) {
        BarChart bcBuienRadar = (BarChart) findViewById(R.id.bcBuienRadar);
        TextView tvDroogBr = (TextView) findViewById(R.id.tvDroogBr);

        if (visible) {
            tvDroogBr.setVisibility(View.VISIBLE);
            bcBuienRadar.setVisibility(View.VISIBLE);
        } else {
            tvDroogBr.setVisibility(View.GONE);
            bcBuienRadar.setVisibility(View.GONE);
        }
    }

    private void InitLocation() {

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Helper.ONE_MINUTE, Helper.ONE_KM, locationListener);

        String locationProvider = LocationManager.NETWORK_PROVIDER;

        Location lastLocation = locationManager.getLastKnownLocation(locationProvider);
        makeUseOfNewLocation(lastLocation);
    }

    private void Init() {
        mDH = new DatabaseHelper(getApplicationContext());
        SyncLocalDb(false);
        InitViews(false);
        InitWeerViews(false);
        InitBrViews(false);
        InitLocation();
    }

    private void makeUseOfNewLocation(Location location) {
        Context cxt = getApplicationContext();
        Helper.mCurrentBestLocation = location;
        LocationHelper.BepaalLocatie(this);
        ToonHuidigeLocatie();
        new AsyncGetWeerVoorspelling().execute(cxt);
    }

    private void SetDbSyncDate() {
        Context cxt = getApplicationContext();
        Helper.SetLastSyncDate(cxt, DateTime.now());
    }

    private void SyncLocalDb(Boolean force) {
        Context cxt = getApplicationContext();
        DateTime last = Helper.GetLastSyncDate(cxt);

        if (force) {
            if (!Helper.TestInternet(cxt)) {
                return;
            }
            ShowDbProgress();
            new AsyncSyncLocalDbTask().execute(last);
        } else {
            DateTime nu = DateTime.now();
            if (last.plusHours(1).isBefore(nu)) {
                if (!Helper.TestInternet(cxt)) {
                    return;
                }
                ShowDbProgress();
                new AsyncSyncLocalDbTask().execute(last);
            }
        }
    }

    private void ShowDbProgress() {
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Lokale cache bijwerken ...");
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgress.setCancelable(false);
        mProgress.show();
    }

    private class AsyncSyncLocalDbTask extends AsyncTask<DateTime, Void, Void> {

        @Override
        protected Void doInBackground(DateTime... params) {

            DateTime last = params[0];
            try {
                // We vragen 15 minuten extra op, want als de server tijd wat afwijkt is er een kans dat je uiteindelijk meldingen mist
                MeldingCollection meldingen = Helper.myApiService.getAlleMeldingenVanaf(last.minusMinutes(15).getMillis()).execute();
                if (meldingen == null || meldingen.size() == 0 || meldingen.getItems() == null || meldingen.getItems().size() == 0) {
                    return null;
                }

                mDH.addMeldingen(meldingen.getItems());

            } catch (IOException ignored) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            SetDbSyncDate();
            mProgress.hide();
        }
    }

    private class AsyncGetLaatsteMeldingTask extends AsyncTask<String, Void, Melding> {

        @Override
        protected Melding doInBackground(String... params) {
            String id = params[0];
            return mDH.GetLaatsteMelding(id);
        }

        @Override
        protected void onPostExecute(Melding melding) {
            ToonLaatsteMelding(melding);
        }
    }

    private class AsyncGetWeerVoorspelling extends AsyncTask<Context, Void, Weer> {

        @Override
        protected Weer doInBackground(Context... params) {

            Context context = params[0];
            Weer weer = null;
            try {
                weer = WeerHelper.BepaalWeer(context);
            } catch (JSONException ignored) {
            }
            return weer;
        }

        @Override
        protected void onPostExecute(Weer result) {
            ToonWeerdata(result);
        }
    }

    private class AsyncGetBuienData extends AsyncTask<Context, Void, BuienData> {

        @Override
        protected BuienData doInBackground(Context... params) {

            BuienData buienData = null;
            try {
                buienData = WeerHelper.BepaalBuien();
            } catch (Exception ignored) {
            }
            return buienData;
        }

        @Override
        protected void onPostExecute(BuienData result) {
            ToonBuiendata(result);
        }
    }

    private class AsyncSlaMeldingOpTask extends AsyncTask<Pair<Context, Melding>, Void, Pair<Context, Melding>> {

        @SafeVarargs
        @Override
        protected final Pair<Context, Melding> doInBackground(Pair<Context, Melding>... params) {

            Context context = params[0].first;
            Melding melding = params[0].second;

            Melding meld = null;
            try {
                meld = Helper.myApiService.meldingOpslaan(melding).execute();
            } catch (IOException ignored) {
            }
            return Pair.create(context, meld);
        }

        @Override
        protected void onPostExecute(Pair<Context, Melding> result) {
            Melding melding = result.second;
            if (melding == null) return;
            String err = melding.getError();
            if (err != null && !err.isEmpty()) {
                Helper.ShowMessage(result.first, err);
            } else {
                Helper.ShowMessage(result.first, "Bedankt voor je melding");
                SyncLocalDb(true);
            }
            ToondataBackground();
        }
    }

    private class AsyncGetPersoonlijkeStatsTask extends AsyncTask<String, Void, Statistiek> {

        @Override
        protected Statistiek doInBackground(String... params) {
            String id = params[0];
            return mDH.GetPersoonlijkeStatistiek(id);
        }

        @Override
        protected void onPostExecute(Statistiek stat) {
            ToonPersoonlijkeStat(stat);
        }
    }
}