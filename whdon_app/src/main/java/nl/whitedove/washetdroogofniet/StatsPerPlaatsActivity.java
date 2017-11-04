package nl.whitedove.washetdroogofniet;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class StatsPerPlaatsActivity extends Activity {
    static DatabaseHelper mDH;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.per_plaats_statistieken);
        InitFab();
        InitDb();
        ToondataBackground();
    }

    private void InitFab()
    {
        FloatingActionButton fabTerug = findViewById(R.id.btnTerug);
        fabTerug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Terug();
            }
        });
    }

    private void InitDb() {
        mDH = new DatabaseHelper(getApplicationContext());
    }

    private void Stat1Plaats(String locatie) {
        Intent intent = new Intent(this, Stats1PlaatsActivity.class);
        intent.putExtra("Locatie", locatie);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void Terug() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void ToondataBackground() {
        new AsyncGetStatistiekenPerPlaatsTask(this).execute();
    }

    private void ToonPerPlaatsStatistieken(ArrayList<Statistiek> stats) {
        if (stats == null || stats.size() <= 1) {
            return;
        }
        final ListView lvStats = findViewById(R.id.lvStats);

        final CustomListAdapterTotStats adapter = new CustomListAdapterTotStats(this, stats);
        lvStats.setAdapter(adapter);

        EditText etZoek = findViewById(R.id.etZoek);
        etZoek.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        lvStats.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Statistiek stat = (Statistiek) parent.getItemAtPosition(position);
                Stat1Plaats(stat.getLocatie());
            }
        });

    }

    private static class AsyncGetStatistiekenPerPlaatsTask extends AsyncTask<Void, Void, ArrayList<Statistiek>> {
        private WeakReference<StatsPerPlaatsActivity> activityWeakReference;

        AsyncGetStatistiekenPerPlaatsTask(StatsPerPlaatsActivity context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<Statistiek> doInBackground(Void... params) {
            return mDH.GetStatistieken();
        }

        @Override
        protected void onPostExecute(ArrayList<Statistiek> stats) {

            StatsPerPlaatsActivity activity = activityWeakReference.get();
            if (activity != null) activity.ToonPerPlaatsStatistieken(stats);
        }

    }
}