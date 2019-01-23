package fr.hazegard.drfreeze.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.hazegard.drfreeze.FreezeApplication
import fr.hazegard.drfreeze.PreferencesHelper
import fr.hazegard.drfreeze.R
import kotlinx.android.synthetic.main.activity_not_root.*
import javax.inject.Inject

class NotRootActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        FreezeApplication.appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_not_root)
        setSupportActionBar(findViewById(R.id.toolbar))
        button_exit.setOnClickListener {
            finishAffinity()
            System.exit(0)
        }
        button_continue.setOnClickListener {
            preferencesHelper.setBypassRootNeeded()
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
