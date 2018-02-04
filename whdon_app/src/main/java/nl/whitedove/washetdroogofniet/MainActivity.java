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
import android.support.annotation.NonNull;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.joda.time.DateTime;
import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import nl.whitedove.washetdroogofniet.backend.whdonApi.model.Melding;
import nl.whitedove.washetdroogofniet.backend.whdonApi.model.MeldingCollection;

public class MainActivity extends Activity {

    ProgressDialog mProgressSync;
    ProgressDialog mProgressSave;
    static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fabJa = findViewById(R.id.btnJa);
        fabJa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VerwerkJa();
            }
        });

        FloatingActionButton fabNee = findViewById(R.id.btnNee);
        fabNee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VerwerkNee();
            }
        });

        FloatingActionButton fabMenu = findViewById(R.id.btnMenu);
        fabMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewMainMenu();
            }
        });

        LineChart lcBuienRadar = findViewById(R.id.lcBuienRadar);
        lcBuienRadar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BuienRadar();
            }
        });

        FrameLayout flPersStats = findViewById(R.id.flPersStats);
        flPersStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EigenMeldingen();
            }
        });

        ToonSpreukVdDag();
        Init();
        ToondataBackground();
        SyncLocalDb();
    }

    @SuppressLint("InflateParams")
    public void NewMainMenu() {

        List<ContextMenuItem> contextMenuItems;
        final Dialog customDialog = new Dialog(this);

        LayoutInflater inflater;
        View child;
        ListView listView;
        ContextMenuAdapter adapter;

        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        child = inflater.inflate(R.layout.listview_context_menu, null);
        listView = child.findViewById(R.id.listView_context_menu);

        contextMenuItems = new ArrayList<>();

        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.user), getString(R.string.eigen_meldingen)));
        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.staafvert), getString(R.string.per)));
        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.lijn1), getString(R.string.aantal_gebruikers)));
        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.list25), getString(R.string.laatste25)));
        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.i10d), getString(R.string.Weer)));
        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.map), getString(R.string.Kaart)));

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
                        NewPerMenu();
                        return;

                    case 2:
                        GrafiekAantalGebruikers();
                        return;

                    case 3:
                        Laatste25();
                        return;

                    case 4:
                        NewWeerMenu();
                        return;

                    case 5:
                        Kaart();
                }
            }
        });

        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(child);
        customDialog.show();
    }

    @SuppressLint("InflateParams")
    public void NewPerMenu() {

        List<ContextMenuItem> contextMenuItems;
        final Dialog customDialog = new Dialog(this);

        LayoutInflater inflater;
        View child;
        ListView listView;
        ContextMenuAdapter adapter;

        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        child = inflater.inflate(R.layout.listview_context_menu, null);
        listView = child.findViewById(R.id.listView_context_menu);

        contextMenuItems = new ArrayList<>();

        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.staafhor), getString(R.string.plaats)));
        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.staafvert2), getString(R.string.maand)));
        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.staafvert), getString(R.string.aantal_meldingen_per_dag)));
        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.staafvert3), getString(R.string.UurVdDag)));

        adapter = new ContextMenuAdapter(this, contextMenuItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                customDialog.dismiss();
                switch (position) {

                    case 0:
                        GrafiekPlaats();
                        return;

                    case 1:
                        GrafiekMaand();
                        return;

                    case 2:
                        GrafiekDatum();
                        return;

                    case 3:
                        GrafiekUur();
                }
            }
        });

        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(child);
        customDialog.show();
    }

    @SuppressLint("InflateParams")
    public void NewWeerMenu() {

        List<ContextMenuItem> contextMenuItems;
        final Dialog customDialog = new Dialog(this);

        LayoutInflater inflater;
        View child;
        ListView listView;
        ContextMenuAdapter adapter;

        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        child = inflater.inflate(R.layout.listview_context_menu, null);
        listView = child.findViewById(R.id.listView_context_menu);

        contextMenuItems = new ArrayList<>();

        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.pie), getString(R.string.WeerType)));
        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.winddir), getString(R.string.Windrichting)));
        contextMenuItems.add(new ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.windspeed), getString(R.string.Windsnelheid)));

        adapter = new ContextMenuAdapter(this, contextMenuItems);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                customDialog.dismiss();
                switch (position) {

                    case 0:
                        GrafiekWeerType();
                        return;

                    case 1:
                        GrafiekWindRichting();
                        return;

                    case 2:
                        GrafiekWindSnelheid();
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

                    case R.id.action_settings:
                        Intent intent1 = new Intent();
                        intent1.setClass(MainActivity.this, SetPreferenceActivity.class);
                        startActivityForResult(intent1, 0);
                        return true;

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
        SyncLocalDb();
    }

    private void ClearCache() {
        Context cxt = getApplicationContext();
        DatabaseHelper dh = DatabaseHelper.getInstance(cxt);
        dh.DeleteMeldingen();
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

    private void GrafiekUur() {
        Intent intent = new Intent(this, StatsPerUurActivity.class);
        startActivity(intent);
    }

    private void GrafiekWeerType() {
        Intent intent = new Intent(this, StatsWeerTypeActivity.class);
        startActivity(intent);
    }

    private void GrafiekWindRichting() {
        Intent intent = new Intent(this, StatsWindRichtingActivity.class);
        startActivity(intent);
    }

    private void GrafiekWindSnelheid() {
        Intent intent = new Intent(this, StatsWindSnelheidActivity.class);
        startActivity(intent);
    }

    private void EigenMeldingen() {
        Intent intent = new Intent(this, EigenMeldingenActivity.class);
        startActivity(intent);
    }

    private void Kaart() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private void ToonSpreukVdDag() {
        TextView tvSpreuk = findViewById(R.id.tvSpreuk);
        DateTime nu = DateTime.now();
        int maand = nu.getMonthOfYear();
        int dag = nu.getDayOfMonth();
        String spreuk = SpreukenHelper.GeefSpreuk(maand, dag);
        tvSpreuk.setText(spreuk);
    }

    @SuppressLint("DefaultLocale")
    private void ToonWeerdata(Weer weerData) {
        Context context = getApplicationContext();

        String locatie = LocationHelper.GetLocatieVoorWeer();
        if (weerData == null) {
            String weeronbekend = this.getString(R.string.WeerOnbekend);
            Helper.ShowMessage(this, weeronbekend);
            TextView tvWeerkop = findViewById(R.id.tvWeerkop);
            tvWeerkop.setText(String.format(this.getString(R.string.Weer3uur), locatie));
            InitWeerViews(false);
            return;
        }

        InitWeerViews(true);
        TextView tvWeerkop = findViewById(R.id.tvWeerkop);
        tvWeerkop.setText(String.format(this.getString(R.string.Weer3uur), weerData.getPlaats()));

        TextView tvGrad = findViewById(R.id.tvGrad);
        int temperatuur = weerData.getGraden();
        tvGrad.setText(String.format("%d °C", temperatuur));
        WeerHelper.SetHuidigeTemperatuur(temperatuur);

        TextView tvWind = findViewById(R.id.tvWind);
        tvWind.setText(String.format("%d km/h", weerData.getWind()));
        WeerHelper.setHuidigeWindSpeed(weerData.getWind());

        ImageView imWeer = findViewById(R.id.imWeer);
        int id = context.getResources().getIdentifier("i" + weerData.getIcon(), "drawable", context.getPackageName());
        imWeer.setImageResource(id);
        WeerHelper.setHuidigeWeertype(WeerHelper.WeerIcoonToWeerType(weerData.getIcon()));

        ImageView imWind = findViewById(R.id.imWind);
        WeerHelper.WindDirection richting = weerData.getWindDir();
        switch (richting) {
            case Onbekend:
                imWind.setImageResource(R.drawable.variabel);
                break;
            case Noord:
                imWind.setImageResource(R.drawable.noord);
                break;
            case NoordOost:
                imWind.setImageResource(R.drawable.noordoost);
                break;
            case Oost:
                imWind.setImageResource(R.drawable.oost);
                break;
            case ZuidOost:
                imWind.setImageResource(R.drawable.zuidoost);
                break;
            case Zuid:
                imWind.setImageResource(R.drawable.zuid);
                break;
            case ZuidWest:
                imWind.setImageResource(R.drawable.zuidwest);
                break;
            case West:
                imWind.setImageResource(R.drawable.west);
                break;
            case NoordWest:
                imWind.setImageResource(R.drawable.noordwest);
                break;
        }
        WeerHelper.setHuidigeWindDir(richting);
    }

    private void ToonBuiendata(BuienData weerData) {

        InitBrViews(true);

        LineChart chart = findViewById(R.id.lcBuienRadar);
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.setVisibleYRangeMaximum(255, YAxis.AxisDependency.LEFT);

        Description desc = new Description();
        desc.setPosition(Utils.convertDpToPixel(88), Utils.convertDpToPixel(12));
        desc.setTextSize(12);
        desc.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        desc.setText(getString(R.string.buienradar2u));
        chart.setDescription(desc);

        chart.setScaleEnabled(false);
        chart.setGridBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        chart.setNoDataText("");
        chart.setDrawBorders(true);
        chart.setBorderColor(ContextCompat.getColor(this, R.color.colorTekst));

        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        YAxis yAs1 = chart.getAxisLeft();
        yAs1.setAxisMaximum(255);
        yAs1.setAxisMinimum(0);
        yAs1.setLabelCount(0, true);
        yAs1.setDrawGridLines(false);
        yAs1.setDrawLabels(false);
        yAs1.setDrawAxisLine(false);

        YAxis yAs2 = chart.getAxisRight();
        yAs2.setAxisMaximum(255);
        yAs2.setAxisMinimum(0);
        yAs2.setLabelCount(0, true);
        yAs2.setDrawGridLines(false);
        yAs2.setDrawLabels(false);
        yAs2.setDrawAxisLine(false);

        XAxis xAs = chart.getXAxis();
        xAs.setDrawGridLines(false);
        xAs.setDrawLabels(false);
        xAs.setDrawAxisLine(false);

        TextView tvDroogBr = findViewById(R.id.tvDroogBr);
        String sBr = WeerHelper.BepaalBrDataTxt(this, weerData);
        tvDroogBr.setText(sBr);

        float xPos = WeerHelper.BerekenNuXPositie(weerData);
        LimitLine ll = new LimitLine(xPos, "");
        ll.setLineColor(Color.RED);
        ll.setLineWidth(0.5f);
        xAs.removeAllLimitLines();
        xAs.addLimitLine(ll);
        xAs.setDrawLimitLinesBehindData(true);

        ArrayList<Entry> dataT = new ArrayList<>();

        int som = 0;
        for (int i = 0; i < weerData.getRegenData().size(); i++) {
            int regen = weerData.getRegenData().get(i).getRegen();
            som += regen;
            dataT.add(i, new Entry(i, regen));
        }

        if (som == 0) {
            tvDroogBr.setVisibility(View.VISIBLE);
        } else {
            tvDroogBr.setVisibility(View.GONE);
        }

        LineDataSet dsT = new LineDataSet(dataT, "");

        dsT.setColor(ContextCompat.getColor(this, R.color.colorNatStart));
        dsT.setDrawFilled(true);
        dsT.setFillColor(ContextCompat.getColor(this, R.color.colorNatStart));
        dsT.setDrawCircles(false);
        dsT.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dsT.setCubicIntensity(0.2f);

        IValueFormatter myValueFormat = new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return "";
            }
        };

        dsT.setValueFormatter(myValueFormat);
        dsT.setAxisDependency(YAxis.AxisDependency.LEFT);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dsT);

        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.invalidate();
    }

    private void ToondataBackground() {
        Context cxt = getApplicationContext();
        String id = Helper.GetGuid(cxt);
        //noinspection unchecked
        new AsyncGetLaatsteMeldingTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Pair.create(cxt, id));
        //noinspection unchecked
        new AsyncGetPersoonlijkeStatsTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Pair.create(cxt, id));
        if (!Helper.TestInternet(cxt)) {
            return;
        }
        new AsyncGetWeerVoorspelling(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new AsyncGetBuienData(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void SlaMeldingOp(Boolean droog) {
        ShowMeldingProgress();
        Context context = getApplicationContext();
        String locatie = LocationHelper.GetLocatieVoorWeer();
        String land = LocationHelper.GetCountryVoorWeer();
        Melding melding = new Melding();
        melding.setDroog(droog);
        melding.setLocatie(locatie);
        melding.setLand(land);
        melding.setId(Helper.GetGuid(context));
        melding.setNat(!droog);
        melding.setTemperatuur((long) WeerHelper.GetHuidigeTemperatuur());
        melding.setWeerType(WeerHelper.getHuidigeWeertype().getValue());
        melding.setWindSpeed((long) WeerHelper.getHuidigeWindSpeed());
        melding.setWindDir(WeerHelper.getHuidigeWindDir().getValue());

        //noinspection unchecked
        new AsyncSlaMeldingOpTask(this).execute(Pair.create(context, melding));
    }

    @SuppressLint("DefaultLocale")
    private void ToonLaatsteMelding(Melding melding) {

        Context cxt = getApplicationContext();
        if (Helper.DEBUG) {
            TextView tvLaatste = findViewById(R.id.tvLaatste);
            tvLaatste.setTextColor(Color.RED);
        }

        if (melding == null) {
            Helper.ShowMessage(cxt, getString(R.string.OnverwachteFoutOpslaanMelding));
            return;
        }

        TextView tvDt = findViewById(R.id.tvDatumtijd);
        TextView tvLocatie = findViewById(R.id.tvLocatie);
        TextView tvDroogNat = findViewById(R.id.tvDroogNat);
        TextView tvTemperatuur = findViewById(R.id.tvTemperatuur);

        String er = melding.getError();
        if (er != null && !er.isEmpty()) {
            tvDt.setText(er);
            tvLocatie.setText("");
            tvDroogNat.setText("");
            tvTemperatuur.setText("");
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

        long temperatuur = melding.getTemperatuur();
        if (temperatuur == 999) {
            tvTemperatuur.setText("");
        } else {
            tvTemperatuur.setText(String.format("%d °C", temperatuur));
        }
    }

    @SuppressLint("DefaultLocale")
    private void ToonPersoonlijkeStat(Statistiek stat) {
        Context cxt = getApplicationContext();
        if (stat == null) {
            Helper.ShowMessage(cxt, "Onverwachte fout tijdens ophalen statistiek");
            return;
        }

        TextView tvDroog = findViewById(R.id.tvDroog);
        TextView tvNat = findViewById(R.id.tvNat);
        ProgressBar pbStat = findViewById(R.id.pbDroogNatStat);
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
        TextView tvHuidigeLocatie = findViewById(R.id.tvHuidigeLocatie);
        LocationHelper.ToonHuidigeLocatie(this, tvHuidigeLocatie, LocationHelper.LocationType.Unknown);
        TextView tvPersStats = findViewById(R.id.tvPersStats);
        TextView tvDroog = findViewById(R.id.tvDroog);
        TextView tvNat = findViewById(R.id.tvNat);
        ProgressBar pbStat = findViewById(R.id.pbDroogNatStat);

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

        TextView tvWeerOnBekend = findViewById(R.id.tvWeeronbekend);
        TextView tvGrad = findViewById(R.id.tvGrad);
        TextView tvWind = findViewById(R.id.tvWind);
        ImageView imWeer = findViewById(R.id.imWeer);
        ImageView imWind = findViewById(R.id.imWind);

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
        LineChart lcBuienRadar = findViewById(R.id.lcBuienRadar);
        TextView tvDroogBr = findViewById(R.id.tvDroogBr);

        if (visible) {
            tvDroogBr.setVisibility(View.VISIBLE);
            lcBuienRadar.setVisibility(View.VISIBLE);
        } else {
            tvDroogBr.setVisibility(View.GONE);
            lcBuienRadar.setVisibility(View.GONE);
        }
    }

    private void InitLocation() {
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }

        // Define a listener that responds to location updates
        LocationListener locationListenerGps = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location, LocationHelper.LocationType.Gps);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        Location gpsLastLocation = null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Helper.ONE_MINUTE, Helper.ONE_KM, locationListenerGps);
            gpsLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (gpsLastLocation != null) {
            makeUseOfNewLocation(gpsLastLocation, LocationHelper.LocationType.Gps);
            return;
        }

        // Define a listener that responds to location updates
        LocationListener locationListenerNet = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location, LocationHelper.LocationType.Net);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        Location netLastLocation = null;
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Helper.ONE_MINUTE, Helper.ONE_KM, locationListenerNet);
            netLastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (netLastLocation != null)
            makeUseOfNewLocation(netLastLocation, LocationHelper.LocationType.Net);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    InitLocation();
                } else {
                    Helper.ShowMessage(this, "Zonder locatietoegang is het niet mogelijk om meldingen te doen en de buienradar te bekijken");
                }
            }
        }
    }

    private void Init() {
        InitViews(false);
        InitWeerViews(false);
        InitBrViews(false);
        InitLocation();
    }

    private void makeUseOfNewLocation(Location location, LocationHelper.LocationType loctype) {
        Helper.mCurrentBestLocation = location;
        LocationHelper.BepaalLocatie(this);
        TextView tvHuidigeLocatie = findViewById(R.id.tvHuidigeLocatie);
        LocationHelper.ToonHuidigeLocatie(this, tvHuidigeLocatie, loctype);
        new AsyncGetWeerVoorspelling(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void SetDbSyncDate() {
        Context cxt = getApplicationContext();
        Helper.SetLastSyncDate(cxt, DateTime.now());
    }

    private void SyncLocalDb() {
        Context cxt = getApplicationContext();
        DateTime last = Helper.GetLastSyncDate(cxt);
        DateTime nu = DateTime.now();
        // We syncen max 1 keer per uur om gegevens van anderen op te halen
        if (last.plusHours(1).isBefore(nu)) {
            if (!Helper.TestInternet(cxt)) {
                return;
            }
            // We tonen een progressbar als het lang geleden was sinds de laatste sync
            if (last.isBefore(DateTime.now().minusDays(7))) ShowDbProgress();
            //noinspection unchecked
            new AsyncSyncLocalDbTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Pair.create(cxt, last));
        }
    }

    private void ShowMeldingProgress() {
        mProgressSave = new ProgressDialog(this);
        mProgressSave.setMessage(getString(R.string.MeldingVerwerken));
        mProgressSave.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressSave.setCancelable(false);
        mProgressSave.show();
    }

    private void ShowDbProgress() {
        mProgressSync = new ProgressDialog(this);
        mProgressSync.setMessage(getString(R.string.CacheBijwerken));
        mProgressSync.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressSync.setCancelable(false);
        mProgressSync.show();
    }

    private static class AsyncSyncLocalDbTask extends AsyncTask<Pair<Context, DateTime>, Void, Void> {

        private WeakReference<MainActivity> activityWeakReference;

        AsyncSyncLocalDbTask(MainActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(Pair<Context, DateTime>... params) {

            Context context = params[0].first;
            DateTime last = params[0].second;
            try {
                // We vragen 24 uur extra op, om de kans dat we gegevens missen kleiner te maken
                MeldingCollection meldingen = Helper.myApiService.getAlleMeldingenVanaf(last.minusHours(24).getMillis()).execute();
                if (meldingen == null || meldingen.size() == 0 || meldingen.getItems() == null || meldingen.getItems().size() == 0) {
                    return null;
                }
                DatabaseHelper dh = DatabaseHelper.getInstance(context);
                dh.addMeldingen(meldingen.getItems());

            } catch (IOException ignored) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing) {
            MainActivity activity = activityWeakReference.get();
            if (activity == null) return;
            activity.SetDbSyncDate();
            if (activity.mProgressSync != null) activity.mProgressSync.hide();
        }
    }

    private static class AsyncGetLaatsteMeldingTask extends AsyncTask<Pair<Context, String>, Void, Melding> {

        private WeakReference<MainActivity> activityWeakReference;

        AsyncGetLaatsteMeldingTask(MainActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @SafeVarargs
        @Override
        protected final Melding doInBackground(Pair<Context, String>... params) {
            Context context = params[0].first;
            String id = params[0].second;
            DatabaseHelper dh = DatabaseHelper.getInstance(context);
            return dh.GetLaatsteMelding(id);
        }

        @Override
        protected void onPostExecute(Melding melding) {
            MainActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonLaatsteMelding(melding);
        }
    }

    private static class AsyncGetWeerVoorspelling extends AsyncTask<Void, Void, Weer> {

        private WeakReference<MainActivity> activityWeakReference;

        AsyncGetWeerVoorspelling(MainActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Weer doInBackground(Void... params) {
            Weer weer = null;
            try {
                weer = WeerHelper.BepaalWeer();
            } catch (JSONException ignored) {
            }
            return weer;
        }

        @Override
        protected void onPostExecute(Weer result) {
            MainActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonWeerdata(result);
        }
    }

    private static class AsyncGetBuienData extends AsyncTask<Void, Void, BuienData> {

        private WeakReference<MainActivity> activityWeakReference;

        AsyncGetBuienData(MainActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected BuienData doInBackground(Void... params) {

            BuienData buienData = null;
            try {
                buienData = WeerHelper.BepaalBuien();
            } catch (Exception ignored) {
            }
            return buienData;
        }

        @Override
        protected void onPostExecute(BuienData result) {
            MainActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonBuiendata(result);
        }
    }

    private static class AsyncSlaMeldingOpTask extends AsyncTask<Pair<Context, Melding>, Void, Pair<Context, Melding>> {

        private WeakReference<MainActivity> activityWeakReference;

        AsyncSlaMeldingOpTask(MainActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

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
            MainActivity activity = activityWeakReference.get();
            if (activity == null) return;
            if (activity.mProgressSave != null) activity.mProgressSave.hide();

            Context context = result.first;
            Melding melding = result.second;
            if (melding == null) return;
            String err = melding.getError();
            if (err != null && !err.isEmpty()) {
                Helper.ShowMessage(context, err);
            } else {
                ArrayList<Melding> meldingen = new ArrayList<>();
                meldingen.add(melding);
                DatabaseHelper dh = DatabaseHelper.getInstance(context);
                dh.addMeldingen(meldingen);
                Helper.ShowMessage(context, activity.getString(R.string.BedanktMelding));
            }
            activity.ToondataBackground();
        }
    }

    private static class AsyncGetPersoonlijkeStatsTask extends AsyncTask<Pair<Context, String>, Void, Statistiek> {
        private WeakReference<MainActivity> activityWeakReference;

        AsyncGetPersoonlijkeStatsTask(MainActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @SafeVarargs
        @Override
        protected final Statistiek doInBackground(Pair<Context, String>... params) {
            Context context = params[0].first;
            String id = params[0].second;
            DatabaseHelper dh = DatabaseHelper.getInstance(context);
            return dh.GetPersoonlijkeStatistiek(id);
        }

        @Override
        protected void onPostExecute(Statistiek stat) {
            MainActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonPersoonlijkeStat(stat);
        }
    }
}