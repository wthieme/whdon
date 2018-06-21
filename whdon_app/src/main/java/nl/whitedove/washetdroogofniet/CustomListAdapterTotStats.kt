package nl.whitedove.washetdroogofniet

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ProgressBar
import android.widget.TextView

import java.util.ArrayList

internal class CustomListAdapterTotStats(context: Context, listData: ArrayList<Statistiek>) : BaseAdapter(), Filterable {

    private val listData: List<Statistiek>
    private var filteredData: List<Statistiek>? = null
    private val mFilter = ItemFilter()

    private val layoutInflater: LayoutInflater

    init {

        this.listData = listData
        this.filteredData = listData
        layoutInflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return filteredData!!.size
    }

    override fun getItem(position: Int): Any {
        return filteredData!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("DefaultLocale")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cv = convertView
        val holder: ViewHolder
        if (cv == null) {
            cv = layoutInflater.inflate(R.layout.totstat_list_layout, parent, false)
            holder = ViewHolder()
            holder.tvLocatie = cv!!.findViewById(R.id.tvLocatie)
            holder.tvLocatieAantal = cv.findViewById(R.id.tvLocatieAantal)
            holder.tvDroog = cv.findViewById(R.id.tvDroog)
            holder.tvNat = cv.findViewById(R.id.tvNat)
            holder.pbProgress = cv.findViewById(R.id.pbDroogNatStat)
            cv.tag = holder
        } else {
            holder = cv.tag as ViewHolder
        }

        val stat = filteredData!![position]
        val aantalDroog = stat.aantalDroog
        val aantalNat = stat.aantalNat
        val totaal = aantalDroog + aantalNat

        holder.tvLocatie!!.text = stat.locatie
        holder.tvLocatieAantal!!.text = String.format("(%d)", totaal)
        holder.pbProgress!!.progress = 100 * aantalDroog / totaal
        val percDroog = Math.round(100.0f * aantalDroog / totaal)
        val percNat = 100 - percDroog

        holder.tvDroog!!.text = String.format("%d%%", percDroog)
        holder.tvNat!!.text = String.format("%d%%", percNat)
        return cv
    }

    private class ViewHolder {
        internal var tvLocatie: TextView? = null
        internal var tvLocatieAantal: TextView? = null
        internal var tvDroog: TextView? = null
        internal var tvNat: TextView? = null
        internal var pbProgress: ProgressBar? = null
    }

    override fun getFilter(): Filter {
        return mFilter
    }

    private inner class ItemFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): Filter.FilterResults {

            val filterString = constraint.toString().toLowerCase()

            val results = Filter.FilterResults()

            val list = listData

            val count = list.size
            val nlist = ArrayList<Statistiek>(count)

            var filterableStat: Statistiek

            for (i in 0 until count) {
                filterableStat = list[i]
                if (filterableStat.locatie!!.toLowerCase().contains(filterString)) {
                    nlist.add(filterableStat)
                }
            }

            results.values = nlist
            results.count = nlist.size

            return results
        }

        override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
            @Suppress("UNCHECKED_CAST")
            filteredData = results.values as ArrayList<Statistiek>
            notifyDataSetChanged()
        }
    }
}

