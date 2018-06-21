package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import org.joda.time.DateTime

import java.util.ArrayList

import nl.whitedove.washetdroogofniet.backend.whdonApi.model.Melding

internal class CustomListAdapterMeldingen(private val context: Context, listData: ArrayList<Melding>) : BaseAdapter() {

    private val listData: List<Melding>
    private val layoutInflater: LayoutInflater

    init {

        this.listData = listData
        layoutInflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return listData.size
    }

    override fun getItem(position: Int): Any {
        return listData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("DefaultLocale")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cv = convertView
        val holder: ViewHolder
        if (cv == null) {
            cv = layoutInflater.inflate(R.layout.meldingen_list_layout, parent, false)
            holder = ViewHolder()
            holder.tvPsLocatie = cv!!.findViewById(R.id.tvPsLocatie)
            holder.tvPsDatum = cv.findViewById(R.id.tvPsDatum)
            holder.tvPsDroogNat = cv.findViewById(R.id.tvPsDroogNat)
            holder.tvPsTemperatuur = cv.findViewById(R.id.tvPsTemperatuur)
            holder.imWeer = cv.findViewById(R.id.imWeer)
            cv.tag = holder
        } else {
            holder = cv.tag as ViewHolder
        }

        val melding = listData[position]

        val datum = DateTime(melding.datum)
        holder.tvPsDatum!!.text = Helper.dtFormat.print(datum)
        holder.tvPsLocatie!!.text = melding.locatie
        holder.tvPsDroogNat!!.text = if (melding.droog) "Droog" else "Nat"

        if (melding.droog!!) {
            holder.tvPsDroogNat!!.setTextColor(ContextCompat.getColor(context, R.color.colorDroogStart))
        } else {
            holder.tvPsDroogNat!!.setTextColor(ContextCompat.getColor(context, R.color.colorTekst))
        }

        val temperatuur = melding.temperatuur!!
        if (temperatuur == 999L) {
            holder.tvPsTemperatuur!!.text = ""
        } else {
            holder.tvPsTemperatuur!!.text = String.format("%d °C", temperatuur)
        }

        val weerType = WeerHelper.WeerType.valueOf(melding.weerType!!)
        val icon = WeerHelper.weerTypeToWeerIcoon(weerType)
        if (icon == null) {
            holder.imWeer!!.visibility = View.GONE
        } else {
            holder.imWeer!!.visibility = View.VISIBLE
            val id = context.resources.getIdentifier(icon, "drawable", context.packageName)
            holder.imWeer!!.setImageResource(id)
        }

        if (temperatuur == 999L) {
            holder.tvPsTemperatuur!!.text = ""
        } else {
            holder.tvPsTemperatuur!!.text = String.format("%d °C", temperatuur)
        }

        return cv
    }

    private class ViewHolder {
        internal var tvPsLocatie: TextView? = null
        internal var tvPsDatum: TextView? = null
        internal var tvPsDroogNat: TextView? = null
        internal var tvPsTemperatuur: TextView? = null
        internal var imWeer: ImageView? = null
    }
}