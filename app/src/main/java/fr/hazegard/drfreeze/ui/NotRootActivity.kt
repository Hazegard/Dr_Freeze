package fr.hazegard.drfreeze.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import fr.hazegard.drfreeze.PreferencesHelper
import fr.hazegard.drfreeze.R
import kotlinx.android.synthetic.main.activity_not_root.*

class NotRootActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_not_root)
        setSupportActionBar(findViewById(R.id.toolbar))
        button_exit.setOnClickListener {
            finishAffinity()
            System.exit(0)
        }
        button_continue.setOnClickListener {
            PreferencesHelper.setBypassRootNeeded(this)
            finish()
        }
    }

    override fun onBackPressed() {
        finishAffinity()
        System.exit(0)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, NotRootActivity::class.java)
        }
    }
}
