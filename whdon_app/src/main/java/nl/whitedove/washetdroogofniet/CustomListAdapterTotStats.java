package nl.whitedove.washetdroogofniet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class CustomListAdapterTotStats extends BaseAdapter implements Filterable {

    private List<Statistiek> listData;
    private List<Statistiek>filteredData = null;
    private ItemFilter mFilter = new ItemFilter();

    private LayoutInflater layoutInflater;

    CustomListAdapterTotStats(Context context, ArrayList<Statistiek> listData) {

        this.listData = listData;
        this.filteredData = listData ;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("DefaultLocale")
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.totstat_list_layout, parent, false);
            holder = new ViewHolder();
            holder.tvLocatie = convertView.findViewById(R.id.tvLocatie);
            holder.tvLocatieAantal = convertView.findViewById(R.id.tvLocatieAantal);
            holder.tvDroog = convertView.findViewById(R.id.tvDroog);
            holder.tvNat = convertView.findViewById(R.id.tvNat);
            holder.pbProgress = convertView.findViewById(R.id.pbDroogNatStat);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Statistiek stat = filteredData.get(position);
        int aantalDroog = stat.getAantalDroog();
        int aantalNat = stat.getAantalNat();
        int totaal = aantalDroog + aantalNat;

        holder.tvLocatie.setText(stat.getLocatie());
        holder.tvLocatieAantal.setText(String.format("(%d)", totaal));
        holder.pbProgress.setProgress(100 * aantalDroog / totaal);
        int percDroog = Math.round(100.0F * aantalDroog / totaal);
        int percNat = 100 - percDroog;

        holder.tvDroog.setText(String.format("%d%%", percDroog));
        holder.tvNat.setText(String.format("%d%%", percNat));
        return convertView;
    }

    private static class ViewHolder {
        TextView tvLocatie;
        TextView tvLocatieAantal;
        TextView tvDroog;
        TextView tvNat;
        ProgressBar pbProgress;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<Statistiek> list = listData;

            int count = list.size();
            final ArrayList<Statistiek> nlist = new ArrayList<>(count);

            Statistiek filterableStat ;

            for (int i = 0; i < count; i++) {
                filterableStat = list.get(i);
                if (filterableStat.getLocatie().toLowerCase().contains(filterString)) {
                    nlist.add(filterableStat);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<Statistiek>) results.values;
            notifyDataSetChanged();
        }
    }
}

