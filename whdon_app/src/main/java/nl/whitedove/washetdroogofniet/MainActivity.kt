@file:Suppress("DEPRECATION")

package nl.whitedove.washetdroogofniet

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.Utils
import org.joda.time.DateTime
import org.json.JSONException
import java.lang.ref.WeakReference
import java.util.*

class MainActivity : Activity() {

    internal var mProgressSync: ProgressDialog? = null
    internal var mProgressSave: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fabJa = findViewById<FloatingActionButton>(R.id.btnJa)
        fabJa.setOnClickListener { verwerkJa() }

        val fabNee = findViewById<FloatingActionButton>(R.id.btnNee)
        fabNee.setOnClickListener { verwerkNee() }

        val fabMenu = findViewById<FloatingActionButton>(R.id.btnMenu)
        fabMenu.setOnClickListener { newMainMenu() }

        val lcBuienRadar = findViewById<LineChart>(R.id.lcBuienRadar)
        lcBuienRadar.setOnClickListener { buienRadar() }

        val flPersStats = findViewById<FrameLayout>(R.id.flPersStats)
        flPersStats.setOnClickListener { eigenMeldingen() }

        toonSpreukVdDag()
        init()
        toondataBackground()
        syncLocalDb()
    }

    @SuppressLint("InflateParams")
    private fun newMainMenu() {

        val contextMenuItems: MutableList<ContextMenuItem>
        val customDialog = Dialog(this)

        val inflater: LayoutInflater?
        val child: View
        val listView: ListView
        val adapter: ContextMenuAdapter

        inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        child = inflater.inflate(R.layout.listview_context_menu, null)
        listView = child.findViewById(R.id.listView_context_menu)

        contextMenuItems = ArrayList()

        contextMenuItems.add(ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.user), getString(R.string.eigen_meldingen)))
        contextMenuItems.add(ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.staafvert), getString(R.string.per)))
        contextMenuItems.add(ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.lijn1), getString(R.string.aantal_gebruikers)))
        contextMenuItems.add(ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.list25), getString(R.string.laatste25)))
        contextMenuItems.add(ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.i10d), getString(R.string.Weer)))
        contextMenuItems.add(ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.map), getString(R.string.Kaart)))

        adapter = ContextMenuAdapter(this, contextMenuItems)
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            customDialog.dismiss()
            when (position) {

                0 -> {
                    eigenMeldingen()
                    return@OnItemClickListener
                }

                1 -> {
                    newPerMenu()
                    return@OnItemClickListener
                }

                2 -> {
                    grafiekAantalGebruikers()
                    return@OnItemClickListener
                }

                3 -> {
                    laatste25()
                    return@OnItemClickListener
                }

                4 -> {
                    newWeerMenu()
                    return@OnItemClickListener
                }

                5 -> kaart()
            }
        }

        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customDialog.setContentView(child)
        customDialog.show()
    }

    @SuppressLint("InflateParams")
    private fun newPerMenu() {

        val contextMenuItems: MutableList<ContextMenuItem>
        val customDialog = Dialog(this)

        val inflater: LayoutInflater?
        val child: View
        val listView: ListView
        val adapter: ContextMenuAdapter

        inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        child = inflater.inflate(R.layout.listview_context_menu, null)
        listView = child.findViewById(R.id.listView_context_menu)

        contextMenuItems = ArrayList()

        contextMenuItems.add(ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.staafhor), getString(R.string.plaats)))
        contextMenuItems.add(ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.staafvert2), getString(R.string.maand)))
        contextMenuItems.add(ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.staafvert), getString(R.string.aantal_meldingen_per_dag)))
        contextMenuItems.add(ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.staafvert3), getString(R.string.AantalPerDagVdWeek)))
        contextMenuItems.add(ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.staafvert4), getString(R.string.UurVdDag)))

        adapter = ContextMenuAdapter(this, contextMenuItems)
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            customDialog.dismiss()
            when (position) {

                0 -> {
                    grafiekPlaats()
                    return@OnItemClickListener
                }

                1 -> {
                    grafiekMaand()
                    return@OnItemClickListener
                }

                2 -> {
                    grafiekDatum()
                    return@OnItemClickListener
                }

                3 -> {
                    grafiekDagVdWeek()
                    return@OnItemClickListener
                }

                4 -> grafiekUur()
            }
        }

        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customDialog.setContentView(child)
        customDialog.show()
    }

    @SuppressLint("InflateParams")
    private fun newWeerMenu() {

        val contextMenuItems: MutableList<ContextMenuItem>
        val customDialog = Dialog(this)

        val inflater: LayoutInflater?
        val child: View
        val listView: ListView
        val adapter: ContextMenuAdapter

        inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        child = inflater.inflate(R.layout.listview_context_menu, null)
        listView = child.findViewById(R.id.listView_context_menu)

        contextMenuItems = ArrayList()

        contextMenuItems.add(ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.pie), getString(R.string.WeerType)))
        contextMenuItems.add(ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.winddir), getString(R.string.Windrichting)))
        contextMenuItems.add(ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.windspeed), getString(R.string.Windsnelheid)))
        contextMenuItems.add(ContextMenuItem(ContextCompat.getDrawable(this, R.drawable.records), getString(R.string.Weerrecords)))

        adapter = ContextMenuAdapter(this, contextMenuItems)
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            customDialog.dismiss()
            when (position) {

                0 -> {
                    grafiekWeerType()
                    return@OnItemClickListener
                }

                1 -> {
                    grafiekWindRichting()
                    return@OnItemClickListener
                }

                2 -> {
                    grafiekWindSnelheid()
                    return@OnItemClickListener
                }

                3 -> weerRecords()
            }
        }

        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customDialog.setContentView(child)
        customDialog.show()
    }

    fun moreClick(oView: View) {
        val popup = PopupMenu(this, oView)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.cmenu, popup.menu)
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {

                R.id.action_settings -> {
                    val intent1 = Intent()
                    intent1.setClass(this@MainActivity, SetPreferenceActivity::class.java)
                    startActivityForResult(intent1, 0)
                    return@OnMenuItemClickListener true
                }

                R.id.clear_cache -> {
                    resetCache()
                    return@OnMenuItemClickListener true
                }
            }
            true
        })

        popup.show()
    }

    private fun resetCache() {
        val context = applicationContext
        Helper.setLastSyncDate(context, DateTime(2000, 1, 1, 0, 0))
        syncLocalDb()
    }

    private fun verwerkJa() {
        val cxt = applicationContext
        if (!Helper.testInternet(cxt)) return
        slaMeldingOp(true)
    }

    private fun verwerkNee() {
        val cxt = applicationContext
        if (!Helper.testInternet(cxt)) {
            return
        }
        slaMeldingOp(false)
    }

    private fun buienRadar() {
        val intent = Intent(this, BuienradarActivity::class.java)
        startActivity(intent)
    }

    private fun grafiekPlaats() {
        val intent = Intent(this, StatsPerPlaatsActivity::class.java)
        startActivity(intent)
    }

    private fun grafiekAantalGebruikers() {
        val intent = Intent(this, StatsAantalGebruikersActivity::class.java)
        startActivity(intent)
    }

    private fun laatste25() {
        val intent = Intent(this, Laatste25Activity::class.java)
        startActivity(intent)
    }

    private fun grafiekDatum() {
        val intent = Intent(this, StatsPerDatumActivity::class.java)
        startActivity(intent)
    }

    private fun grafiekMaand() {
        val intent = Intent(this, StatsPerMaandActivity::class.java)
        startActivity(intent)
    }

    private fun grafiekDagVdWeek() {
        val intent = Intent(this, StatsPerDagVdWeekActivity::class.java)
        startActivity(intent)
    }

    private fun grafiekUur() {
        val intent = Intent(this, StatsPerUurActivity::class.java)
        startActivity(intent)
    }

    private fun grafiekWeerType() {
        val intent = Intent(this, StatsWeerTypeActivity::class.java)
        startActivity(intent)
    }

    private fun grafiekWindRichting() {
        val intent = Intent(this, StatsWindRichtingActivity::class.java)
        startActivity(intent)
    }

    private fun grafiekWindSnelheid() {
        val intent = Intent(this, StatsWindSnelheidActivity::class.java)
        startActivity(intent)
    }

    private fun weerRecords() {
        val intent = Intent(this, StatsRecordsActivity::class.java)
        startActivity(intent)
    }

    private fun eigenMeldingen() {
        val intent = Intent(this, EigenMeldingenActivity::class.java)
        startActivity(intent)
    }

    private fun kaart() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    private fun toonSpreukVdDag() {
        val tvSpreuk = findViewById<TextView>(R.id.tvSpreuk)
        val nu = DateTime.now()
        val maand = nu.monthOfYear
        val dag = nu.dayOfMonth
        val spreuk = SpreukenHelper.geefSpreuk(maand, dag)
        tvSpreuk.text = spreuk
    }

    @SuppressLint("DefaultLocale")
    private fun toonWeerdata(weerData: Weer?) {
        val context = applicationContext

        val locatie = LocationHelper.getLocatieVoorWeer()
        if (weerData == null) {
            val weeronbekend = this.getString(R.string.WeerOnbekend)
            Helper.showMessage(this, weeronbekend)
            val tvWeerkop = findViewById<TextView>(R.id.tvWeerkop)
            tvWeerkop.text = String.format(this.getString(R.string.Weer3uur), locatie)
            initWeerViews(false)
            return
        }

        initWeerViews(true)
        val tvWeerkop = findViewById<TextView>(R.id.tvWeerkop)
        tvWeerkop.text = String.format(this.getString(R.string.Weer3uur), weerData.plaats)

        val tvGrad = findViewById<TextView>(R.id.tvGrad)
        val temperatuur = weerData.graden
        tvGrad.text = String.format("%d °C", temperatuur)
        WeerHelper.setHuidigeTemperatuur(temperatuur)

        val tvWind = findViewById<TextView>(R.id.tvWind)
        tvWind.text = String.format("%d km/h", weerData.wind)
        WeerHelper.huidigeWindSpeed = weerData.wind

        val imWeer = findViewById<ImageView>(R.id.imWeer)
        val id = context.resources.getIdentifier("i" + weerData.icon, "drawable", context.packageName)
        imWeer.setImageResource(id)
        WeerHelper.huidigeWeertype = WeerHelper.weerIcoonToWeerType(weerData.icon)

        val imWind = findViewById<ImageView>(R.id.imWind)
        val richting = weerData.windDir
        when (richting) {
            WeerHelper.WindDirection.Onbekend -> imWind.setImageResource(R.drawable.variabel)
            WeerHelper.WindDirection.Noord -> imWind.setImageResource(R.drawable.noord)
            WeerHelper.WindDirection.NoordOost -> imWind.setImageResource(R.drawable.noordoost)
            WeerHelper.WindDirection.Oost -> imWind.setImageResource(R.drawable.oost)
            WeerHelper.WindDirection.ZuidOost -> imWind.setImageResource(R.drawable.zuidoost)
            WeerHelper.WindDirection.Zuid -> imWind.setImageResource(R.drawable.zuid)
            WeerHelper.WindDirection.ZuidWest -> imWind.setImageResource(R.drawable.zuidwest)
            WeerHelper.WindDirection.West -> imWind.setImageResource(R.drawable.west)
            WeerHelper.WindDirection.NoordWest -> imWind.setImageResource(R.drawable.noordwest)
        }
        WeerHelper.huidigeWindDir = richting
    }

    private fun toonBuiendata(weerData: BuienData) {

        initBrViews(true)

        val chart = findViewById<LineChart>(R.id.lcBuienRadar)
        chart.isHighlightPerTapEnabled = false
        chart.isHighlightPerDragEnabled = false
        chart.setVisibleYRangeMaximum(255f, YAxis.AxisDependency.LEFT)

        val desc = Description()
        desc.setPosition(Utils.convertDpToPixel(88f), Utils.convertDpToPixel(12f))
        desc.textSize = 12f
        desc.textColor = ContextCompat.getColor(this, R.color.colorPrimary)
        desc.text = getString(R.string.buienradar2u)
        chart.description = desc

        chart.setScaleEnabled(false)
        chart.setGridBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
        chart.setNoDataText("")
        chart.setDrawBorders(true)
        chart.setBorderColor(ContextCompat.getColor(this, R.color.colorTekst))

        val legend = chart.legend
        legend.isEnabled = false

        val yAs1 = chart.axisLeft
        yAs1.axisMaximum = 255f
        yAs1.axisMinimum = 0f
        yAs1.setLabelCount(0, true)
        yAs1.setDrawGridLines(false)
        yAs1.setDrawLabels(false)
        yAs1.setDrawAxisLine(false)

        val yAs2 = chart.axisRight
        yAs2.axisMaximum = 255f
        yAs2.axisMinimum = 0f
        yAs2.setLabelCount(0, true)
        yAs2.setDrawGridLines(false)
        yAs2.setDrawLabels(false)
        yAs2.setDrawAxisLine(false)

        val xAs = chart.xAxis
        xAs.setDrawGridLines(false)
        xAs.setDrawLabels(false)
        xAs.setDrawAxisLine(false)

        val tvDroogBr = findViewById<TextView>(R.id.tvDroogBr)
        val sBr = WeerHelper.bepaalBrDataTxt(this, weerData)
        tvDroogBr.text = sBr

        val xPos = WeerHelper.berekenNuXPositie(weerData)
        val ll = LimitLine(xPos, "")
        ll.lineColor = Color.RED
        ll.lineWidth = 0.5f
        xAs.removeAllLimitLines()
        xAs.addLimitLine(ll)
        xAs.setDrawLimitLinesBehindData(true)

        val dataT = ArrayList<Entry>()

        var som = 0
        for (i in 0 until weerData.regenData!!.size) {
            val regen = weerData.regenData!![i].regen
            som += regen
            dataT.add(i, Entry(i.toFloat(), regen.toFloat()))
        }

        if (som == 0) {
            tvDroogBr.visibility = View.VISIBLE
        } else {
            tvDroogBr.visibility = View.GONE
        }

        val dsT = LineDataSet(dataT, "")

        dsT.color = ContextCompat.getColor(this, R.color.colorNatStart)
        dsT.setDrawFilled(true)
        dsT.fillColor = ContextCompat.getColor(this, R.color.colorNatStart)
        dsT.setDrawCircles(false)
        dsT.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        dsT.cubicIntensity = 0.2f

        val myValueFormat = IValueFormatter { _, _, _, _ -> "" }

        dsT.valueFormatter = myValueFormat
        dsT.axisDependency = YAxis.AxisDependency.LEFT

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dsT)

        val data = LineData(dataSets)
        chart.data = data
        chart.invalidate()
    }

    private fun toondataBackground() {
        val cxt = applicationContext
        val id = Helper.getGuid(cxt)

        AsyncGetLaatsteMeldingTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Pair.create(cxt, id))

        AsyncGetPersoonlijkeStatsTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Pair.create(cxt, id))

        if (!Helper.testInternet(cxt)) {
            return
        }
        AsyncGetWeerVoorspelling(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        AsyncGetBuienData(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun slaMeldingOp(droog: Boolean) {
        val context = applicationContext
        val dh = DatabaseHelper.getInstance(context)
        val id = Helper.getGuid(context)
        val laatste = dh.getLaatsteMelding(id)
        val dtNu = DateTime.now()
        if (laatste.datum!!.plusMinutes(15).isAfter(dtNu)) {
            Helper.showMessage(context, getString(R.string.max1perkwartier))
            return
        }

        val locatie = LocationHelper.getLocatieVoorWeer()
        val land = LocationHelper.getCountryVoorWeer()
        val melding = Melding()
        melding.datum = DateTime.now()
        melding.droog = droog
        melding.locatie = locatie
        melding.land = land
        melding.id = id
        melding.nat = !droog
        melding.temperatuur = WeerHelper.getHuidigeTemperatuur()
        melding.weerType = WeerHelper.huidigeWeertype
        melding.windSpeed = WeerHelper.huidigeWindSpeed
        melding.windDir = WeerHelper.huidigeWindDir
        Database.meldingOpslaan(melding)

        dh.addMeldingen(listOf(melding))
        Helper.showMessage(context, getString(R.string.BedanktMelding))
        toondataBackground()
    }

    @SuppressLint("DefaultLocale")
    private fun toonLaatsteMelding(melding: Melding?) {

        val cxt = applicationContext

        if (melding == null) {
            Helper.showMessage(cxt, getString(R.string.OnverwachteFoutOpslaanMelding))
            return
        }

        val tvDt = findViewById<TextView>(R.id.tvDatumtijd)
        val tvLocatie = findViewById<TextView>(R.id.tvLocatie)
        val tvDroogNat = findViewById<TextView>(R.id.tvDroogNat)
        val tvTemperatuur = findViewById<TextView>(R.id.tvTemperatuur)

        val er = melding.error
        if (er != null && !er.isEmpty()) {
            tvDt.text = er
            tvLocatie.text = ""
            tvDroogNat.text = ""
            tvTemperatuur.text = ""
            return
        }

        val lastDate = DateTime(melding.datum)
        val locatie = melding.locatie
        val lastDroogNat = if (melding.droog!!) "Droog" else "Nat"

        tvDt.text = Helper.dtFormat.print(lastDate)
        tvLocatie.text = locatie
        tvDroogNat.text = lastDroogNat

        if (melding.droog!!) {
            tvDroogNat.setTextColor(ContextCompat.getColor(this, R.color.colorDroogStart))
        } else {
            tvDroogNat.setTextColor(ContextCompat.getColor(this, R.color.colorTekst))
        }

        val temperatuur = melding.temperatuur!!
        if (temperatuur == 999) {
            tvTemperatuur.text = ""
        } else {
            tvTemperatuur.text = String.format("%d °C", temperatuur)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun toonPersoonlijkeStat(stat: Statistiek?) {
        val cxt = applicationContext
        if (stat == null) {
            Helper.showMessage(cxt, "Onverwachte fout tijdens ophalen statistiek")
            return
        }

        val tvDroog = findViewById<TextView>(R.id.tvDroog)
        val tvNat = findViewById<TextView>(R.id.tvNat)
        val pbStat = findViewById<ProgressBar>(R.id.pbDroogNatStat)
        val aantalDroog = stat.aantalDroog
        val aantalNat = stat.aantalNat
        val totaal = aantalDroog + aantalNat

        initViews(totaal > 0)
        if (totaal > 0) {
            pbStat.progress = 100 * aantalDroog / totaal
            val percDroog = Math.round(100.0f * aantalDroog / totaal)
            val percNat = 100 - percDroog
            tvDroog.text = String.format("%d%%", percDroog)
            tvNat.text = String.format("%d%%", percNat)
        }
    }

    private fun initViews(visible: Boolean) {
        val tvHuidigeLocatie = findViewById<TextView>(R.id.tvHuidigeLocatie)
        LocationHelper.toonHuidigeLocatie(this, tvHuidigeLocatie, LocationHelper.LocationType.Unknown)
        val tvPersStats = findViewById<TextView>(R.id.tvPersStats)
        val tvDroog = findViewById<TextView>(R.id.tvDroog)
        val tvNat = findViewById<TextView>(R.id.tvNat)
        val pbStat = findViewById<ProgressBar>(R.id.pbDroogNatStat)

        if (visible) {
            tvPersStats.visibility = View.VISIBLE
            pbStat.visibility = View.VISIBLE
            tvDroog.visibility = View.VISIBLE
            tvNat.visibility = View.VISIBLE
        } else {
            tvPersStats.visibility = View.GONE
            pbStat.visibility = View.GONE
            tvDroog.visibility = View.GONE
            tvNat.visibility = View.GONE
        }
    }

    private fun initWeerViews(visible: Boolean) {

        val tvWeerOnBekend = findViewById<TextView>(R.id.tvWeeronbekend)
        val tvGrad = findViewById<TextView>(R.id.tvGrad)
        val tvWind = findViewById<TextView>(R.id.tvWind)
        val imWeer = findViewById<ImageView>(R.id.imWeer)
        val imWind = findViewById<ImageView>(R.id.imWind)

        if (visible) {
            tvWeerOnBekend.visibility = View.GONE
            tvGrad.visibility = View.VISIBLE
            tvWind.visibility = View.VISIBLE
            imWeer.visibility = View.VISIBLE
            imWind.visibility = View.VISIBLE
        } else {
            tvWeerOnBekend.visibility = View.VISIBLE
            tvGrad.visibility = View.GONE
            tvWind.visibility = View.GONE
            imWeer.visibility = View.GONE
            imWind.visibility = View.GONE
        }
    }

    private fun initBrViews(visible: Boolean) {
        val lcBuienRadar = findViewById<LineChart>(R.id.lcBuienRadar)
        val tvDroogBr = findViewById<TextView>(R.id.tvDroogBr)

        if (visible) {
            tvDroogBr.visibility = View.VISIBLE
            lcBuienRadar.visibility = View.VISIBLE
        } else {
            tvDroogBr.visibility = View.GONE
            lcBuienRadar.visibility = View.GONE
        }
    }

    private fun initLocation() {
        // Acquire a reference to the system Location Manager
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION)
            return
        }

        // Define a listener that responds to location updates
        val locationListenerGps = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location, LocationHelper.LocationType.Gps)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}
        }

        var gpsLastLocation: Location? = null
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Helper.ONE_MINUTE.toLong(), Helper.ONE_KM.toFloat(), locationListenerGps)
            gpsLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }

        if (gpsLastLocation != null) {
            makeUseOfNewLocation(gpsLastLocation, LocationHelper.LocationType.Gps)
            return
        }

        // Define a listener that responds to location updates
        val locationListenerNet = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location, LocationHelper.LocationType.Net)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}
        }

        var netLastLocation: Location? = null
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Helper.ONE_MINUTE.toLong(), Helper.ONE_KM.toFloat(), locationListenerNet)
            netLastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }

        if (netLastLocation != null)
            makeUseOfNewLocation(netLastLocation, LocationHelper.LocationType.Net)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initLocation()
                } else {
                    Helper.showMessage(this, "Zonder locatietoegang is het niet mogelijk om meldingen te doen en de buienradar te bekijken")
                }
            }
        }
    }

    private fun init() {
        initViews(false)
        initWeerViews(false)
        initBrViews(false)
        initLocation()
    }

    private fun makeUseOfNewLocation(location: Location, loctype: LocationHelper.LocationType) {
        Helper.mCurrentBestLocation = location
        LocationHelper.bepaalLocatie(this)
        val tvHuidigeLocatie = findViewById<TextView>(R.id.tvHuidigeLocatie)
        LocationHelper.toonHuidigeLocatie(this, tvHuidigeLocatie, loctype)
        AsyncGetWeerVoorspelling(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun setDbSyncDate() {
        val cxt = applicationContext
        Helper.setLastSyncDate(cxt, DateTime.now())
    }

    private fun syncLocalDb() {
        val cxt = applicationContext
        val last = Helper.getLastSyncDate(cxt)
        val nu = DateTime.now()
        // We syncen max 1 keer per uur om gegevens van anderen op te halen
        if (last.plusHours(1).isBefore(nu)) {
            if (!Helper.testInternet(cxt)) {
                return
            }
            // We tonen een progressbar als het lang geleden was sinds de laatste sync
            if (last.isBefore(DateTime.now().minusDays(7))) showDbProgress()

            AsyncSyncLocalDbTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Pair.create(cxt, last))
        }
    }

    private fun showMeldingProgress() {
        mProgressSave = ProgressDialog(this)
        mProgressSave!!.setMessage(getString(R.string.MeldingVerwerken))
        mProgressSave!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mProgressSave!!.setCancelable(false)
        mProgressSave!!.show()
    }

    private fun showDbProgress() {
        mProgressSync = ProgressDialog(this)
        mProgressSync!!.setMessage(getString(R.string.CacheBijwerken))
        mProgressSync!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mProgressSync!!.setCancelable(false)
        mProgressSync!!.show()
    }

    private class AsyncSyncLocalDbTask internal constructor(context: MainActivity) : AsyncTask<Pair<Context, DateTime>, Void, Void>() {

        private val activityWeakReference: WeakReference<MainActivity> = WeakReference(context)
        private var mDbMeldingen = ArrayList<Melding>()

        @SafeVarargs
        override fun doInBackground(vararg params: Pair<Context, DateTime>): Void? {

            val context = params[0].first
            val last = params[0].second
            try {
                // We vragen 24 uur extra op, om de kans dat we gegevens missen kleiner te maken
                val dh = DatabaseHelper.getInstance(context)
                mDbMeldingen = dh.getAlleMeldingenVanaf(last.minusHours(24))

                Database.getAlleMeldingenVanaf(last.minusHours(24), Runnable { fbMeldingen(context) })
            } catch (ignored: Exception) {
            }
            return null
        }

        override fun onPostExecute(void: Void?) {
            val activity = activityWeakReference.get() ?: return
            activity.setDbSyncDate()
            if (activity.mProgressSync != null) activity.mProgressSync!!.hide()
        }

        private fun fbMeldingen(context: Context) {
            val fbMeldingen = Database.mMeldingen
            val dbMeldingen = mDbMeldingen
            val dh = DatabaseHelper.getInstance(context)

            for (fbMelding in fbMeldingen) {
                val bestaat = dbMeldingen.any { dbMelding -> dbMelding.id == fbMelding.id && dbMelding.datum == fbMelding.datum }
                if (!bestaat) {
                    // Voeg toe in database
                    dh.addMeldingen(listOf(fbMelding))
                }
            }
            for (dbMelding in dbMeldingen) {
                val bestaat = fbMeldingen.any { fbMelding -> fbMelding.id == dbMelding.id && fbMelding.datum == dbMelding.datum }
                if (!bestaat) {
                    // Voeg toe in firebase
                    Database.meldingOpslaan(dbMelding)
                }
            }
        }
    }

    private class AsyncGetLaatsteMeldingTask internal constructor(context: MainActivity) : AsyncTask<Pair<Context, String>, Void, Melding>() {

        private val activityWeakReference: WeakReference<MainActivity> = WeakReference(context)

        @SafeVarargs
        override fun doInBackground(vararg params: Pair<Context, String>): Melding {
            val context = params[0].first
            val id = params[0].second
            val dh = DatabaseHelper.getInstance(context)
            return dh.getLaatsteMelding(id)
        }

        override fun onPostExecute(melding: Melding) {
            val activity = activityWeakReference.get()
            activity?.toonLaatsteMelding(melding)
        }
    }

    private class AsyncGetWeerVoorspelling internal constructor(context: MainActivity) : AsyncTask<Void, Void, Weer>() {

        private val activityWeakReference: WeakReference<MainActivity> = WeakReference(context)

        override fun doInBackground(vararg params: Void): Weer? {
            var weer: Weer? = null
            try {
                weer = WeerHelper.bepaalWeer()
            } catch (ignored: JSONException) {
            }

            return weer
        }

        override fun onPostExecute(result: Weer?) {
            if (result == null) return;
            val activity = activityWeakReference.get()
            activity?.toonWeerdata(result)
        }
    }

    private class AsyncGetBuienData internal constructor(context: MainActivity) : AsyncTask<Void, Void, BuienData>() {

        private val activityWeakReference: WeakReference<MainActivity> = WeakReference(context)

        override fun doInBackground(vararg params: Void): BuienData? {

            var buienData: BuienData? = null
            try {
                buienData = WeerHelper.bepaalBuien()
            } catch (ignored: Exception) {
            }

            return buienData
        }

        override fun onPostExecute(result: BuienData) {
            val activity = activityWeakReference.get()
            activity?.toonBuiendata(result)
        }
    }

    private class AsyncGetPersoonlijkeStatsTask internal constructor(context: MainActivity) : AsyncTask<Pair<Context, String>, Void, Statistiek>() {
        private val activityWeakReference: WeakReference<MainActivity> = WeakReference(context)

        @SafeVarargs
        override fun doInBackground(vararg params: Pair<Context, String>): Statistiek {
            val context = params[0].first
            val id = params[0].second
            val dh = DatabaseHelper.getInstance(context)
            return dh.getPersoonlijkeStatistiek(id)
        }

        override fun onPostExecute(stat: Statistiek) {
            val activity = activityWeakReference.get()
            activity?.toonPersoonlijkeStat(stat)
        }
    }

    companion object {
        internal const val MY_PERMISSIONS_REQUEST_LOCATION = 1
    }
}