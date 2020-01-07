package nl.whitedove.washetdroogofniet

import android.app.Activity
import android.os.Bundle

class SetPreferenceActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentManager.beginTransaction().replace(android.R.id.content,
                PrefsFragment()).commit()
    }

}