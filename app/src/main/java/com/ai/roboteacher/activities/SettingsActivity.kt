package com.ai.roboteacher.activities

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ai.roboteacher.MyDeviceAdminReceiver
import com.ai.roboteacher.R

class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enterFullScreenMode(window)
        setContentView(R.layout.activity_settings)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        val toolbar:Toolbar = findViewById(R.id.settings_toolbar)

        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Settings"
            setDisplayHomeAsUpEnabled(true)
        }

// Title text color
        toolbar.setTitleTextColor(Color.WHITE)

// Back arrow (navigation icon) color
        toolbar.navigationIcon?.setTint(Color.WHITE)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val lockSwitch:Switch = findViewById(R.id.lock_switch)

        lockSwitch!!.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

                if(isChecked) {

                    startKiosk()

                    getSharedPreferences("app_prefs",Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("isLocked",true)
                        .commit()

                } else {

                    endKiosk()

                    getSharedPreferences("app_prefs",Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("isLocked",false)
                        .commit()
                }
            }


        })

        if (getSharedPreferences("app_prefs",Context.MODE_PRIVATE).getBoolean("isLocked",false)) {

            lockSwitch.isChecked = true

        } else {

            lockSwitch.isChecked = false


        }

    }

    private fun startKiosk() {

        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val cn = ComponentName(this, MyDeviceAdminReceiver::class.java)

        if (dpm.isDeviceOwnerApp(packageName)) {
            dpm.setLockTaskPackages(cn, arrayOf(packageName))

            dpm.setStatusBarDisabled(cn, true)
            dpm.setKeyguardDisabled(cn,true)
        }

        if (dpm.isLockTaskPermitted(packageName)) {
            startLockTask()
            Toast.makeText(this, "Lock mode on", Toast.LENGTH_SHORT).show()

        } else {


            //Toast.makeText(this, "Lock task not permitted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun endKiosk() {

        stopLockTask()
        Toast.makeText(this, "Lock mode off", Toast.LENGTH_SHORT).show()
    }

    private fun enterFullScreenMode(window: Window) {


        //for new devices edgeToedge+hide systembars
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            window.insetsController?.apply {

                hide(WindowInsetsCompat.Type.statusBars())
                hide(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }

        } else {

            //for older devices fullscreen+hide systembars


            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
        }



    }
}