package nl.whitedove.washetdroogofniet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MainActivity.class);
        Intent scIntent = getIntent();
        String scaction = scIntent.getStringExtra(Helper.SCACTION);
        intent.putExtra(Helper.SCACTION, scaction);
        startActivity(intent);
        finish();
    }
}