package nl.whitedove.washetdroogofniet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import nl.whitedove.washetdroogofniet.backend.whdonApi.model.Melding;

public class EigenMeldingenActivity extends Activity {
    static DatabaseHelper mDH;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eigen_meldingen);

        FloatingActionButton fabTerug = findViewById(R.id.btnTerug);
        fabTerug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Terug();
            }
        });
        InitDb();
        ToondataBackground();
    }

    private void InitDb() {
        mDH = new DatabaseHelper(getApplicationContext());
    }

    private void Terug() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void ToondataBackground() {
        Context cxt = getApplicationContext();
        new AsyncGetEigenMeldingenTask(this).execute(cxt);
    }

    @SuppressLint("DefaultLocale")
    private void ToonEigenMeldingen(ArrayList<Melding> meldingen) {
        if (meldingen == null || meldingen.size() == 0) {
            return;
        }

        TextView tvPsSinds = findViewById(R.id.tvPsSinds);
        TextView tvPsAantalDroog = findViewById(R.id.tvPsAantalDroog);
        TextView tvPsAantalNat = findViewById(R.id.tvPsAantalNat);
        TextView tvPsGemm = findViewById(R.id.tvPsGemm);
        TextView tvPsGemmTemp = findViewById(R.id.tvPsGemmTemp);

        int aantalDroog = 0;
        int aantalNat = 0;
        long tempSom = 0;
        float aantalGemm;
        float tempGemm;
        int aantalTemp = 0;
        DateTime datum = DateTime.now();


        for (Melding rMeld : meldingen) {
            aantalDroog += rMeld.getDroog() ? 1 : 0;
            aantalNat += rMeld.getNat() ? 1 : 0;
            if (rMeld.getTemperatuur() != 999) {
                tempSom += rMeld.getTemperatuur();
                aantalTemp++;
            }
            DateTime melddat = new DateTime(rMeld.getDatum());
            if (melddat.isBefore(datum)) {
                datum = new DateTime(rMeld.getDatum());
            }
        }

        int aantalDagen = Days.daysBetween(datum, DateTime.now()).getDays() + 1;
        aantalGemm = (aantalNat + aantalDroog) / (1.0f * aantalDagen);

        tvPsSinds.setText(Helper.dFormat.print(datum));
        tvPsAantalDroog.setText(String.format("%d", aantalDroog));
        tvPsAantalNat.setText(String.format("%d", aantalNat));
        tvPsGemm.setText(String.format("%.1f", aantalGemm));
        if (aantalTemp > 0) {
            tempGemm = (tempSom) / (1.0f * aantalTemp);
            tvPsGemmTemp.setText(String.format("%.1f", tempGemm));
        }

        final ListView lvEigenMeldingen = findViewById(R.id.lvEigenMeldingen);
        lvEigenMeldingen.setAdapter(new CustomListAdapterMeldingen(this, meldingen));
    }

    private static class AsyncGetEigenMeldingenTask extends AsyncTask<Context, Void, ArrayList<Melding>> {
        private WeakReference<EigenMeldingenActivity> activityWeakReference;

        AsyncGetEigenMeldingenTask(EigenMeldingenActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<Melding> doInBackground(Context... params) {
            Context context = params[0];
            String id = Helper.GetGuid(context);
            return mDH.GetMeldingen(id);
        }

        @Override
        protected void onPostExecute(ArrayList<Melding> meldingen) {
            EigenMeldingenActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonEigenMeldingen(meldingen);
        }
    }
}