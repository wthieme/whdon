package nl.whitedove.washetdroogofniet;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import nl.whitedove.washetdroogofniet.backend.whdonApi.model.Melding;

class CustomListAdapterMeldingen extends BaseAdapter {

    private List<Melding> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    CustomListAdapterMeldingen(Context context, ArrayList<Melding> listData) {

        this.listData = listData;
        this.context = context;
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

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.meldingen_list_layout, parent, false);
            holder = new ViewHolder();
            holder.tvPsLocatie = (TextView) convertView.findViewById(R.id.tvPsLocatie);
            holder.tvPsDatum = (TextView) convertView.findViewById(R.id.tvPsDatum);
            holder.tvPsDroogNat = (TextView) convertView.findViewById(R.id.tvPsDroogNat);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Melding melding = listData.get(position);

        DateTime datum = new DateTime(melding.getDatum());
        holder.tvPsDatum.setText(Helper.dtFormat.print(datum));
        holder.tvPsLocatie.setText(melding.getLocatie());
        holder.tvPsDroogNat.setText(melding.getDroog() ? "Droog" : "Nat");

        if (melding.getDroog()) {
            holder.tvPsDroogNat.setTextColor(ContextCompat.getColor(context, R.color.colorDroogStart));
        } else {
            holder.tvPsDroogNat.setTextColor(ContextCompat.getColor(context, R.color.colorTekst));
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvPsLocatie;
        TextView tvPsDatum;
        TextView tvPsDroogNat;
    }
}