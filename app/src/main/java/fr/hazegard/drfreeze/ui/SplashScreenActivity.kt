package fr.hazegard.drfreeze.ui

import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import fr.hazegard.drfreeze.FreezeApplication
import fr.hazegard.drfreeze.PreferencesHelper
import fr.hazegard.drfreeze.R
import fr.hazegard.drfreeze.Su
import fr.hazegard.drfreeze.extensions.onAnimationEnd
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashScreenActivity : AppCompatActivity() {

    @Inject
    lateinit var su: Su

    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        with(icon.drawable) {
            (this as Animatable).start()
            onAnimationEnd {
                if (isVisible) {
                    runOnUiThread {
                        start()
                    }
                }
            }
        }
        FreezeApplication.appComponent.inject(this)
        GlobalScope.launch {
            val nextActivity = checkSu()
            val nextIntent: Intent = when (nextActivity) {
                RootState.ACCESS_DENIED -> {
                    NotRootActivity.newIntent(this@SplashScreenActivity)
                }
                RootState.ACCESS_DENIED_BYPASS -> {
                    runOnUiThread {
                        Toast.makeText(this@SplashScreenActivity, getString(R.string.no_root_warning), Toast.LENGTH_LONG).show()
                    }
                    ManageTrackedAppActivity.newIntent(this@SplashScreenActivity)
                }
                RootState.ACCESS_GRANTED -> {
                    ManageTrackedAppActivity.newIntent(this@SplashScreenActivity)
                }
            }
            startActivity(nextIntent)
            finish()
        }
    }

    private fun checkSu(): RootState {
        return if (!su.isRoot) {
            if (preferencesHelper.doBypassRootNeeded()) {
                RootState.ACCESS_DENIED_BYPASS
            } else {
                RootState.ACCESS_DENIED
                //                startActivity(NotRootActivity.newIntent(this))
            }
        } else {
            RootState.ACCESS_GRANTED
        }
    }

    private enum class RootState {
        ACCESS_DENIED_BYPASS,
        ACCESS_GRANTED,
        ACCESS_DENIED
    }
}
