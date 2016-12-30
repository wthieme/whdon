package nl.whitedove.washetdroogofniet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class CustomListAdapterTotStats extends BaseAdapter {

    private List<Statistiek> listData;
    private LayoutInflater layoutInflater;

    CustomListAdapterTotStats(Context context, ArrayList<Statistiek> listData) {

        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
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
            holder.tvLocatie = (TextView) convertView.findViewById(R.id.tvLocatie);
            holder.tvLocatieAantal = (TextView) convertView.findViewById(R.id.tvLocatieAantal);
            holder.tvDroog = (TextView) convertView.findViewById(R.id.tvDroog);
            holder.tvNat = (TextView) convertView.findViewById(R.id.tvNat);
            holder.pbProgress = (ProgressBar) convertView.findViewById(R.id.pbDroogNatStat);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Statistiek stat = listData.get(position);
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
}