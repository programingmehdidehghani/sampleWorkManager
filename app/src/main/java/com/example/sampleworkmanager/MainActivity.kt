package com.example.sampleworkmanager

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.sampleworkmanager.databinding.ActivityMainBinding
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 200

    private val TAG = "LocationUpdate"

    var mainBinding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setSupportActionBar(mainBinding.toolbar)

        if (!checkLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }

        try {
            if (isWorkScheduled(WorkManager.getInstance().getWorkInfosByTag(TAG).get())) {
                mainBinding.appCompatButtonStart.text = getString(R.string.button_text_stop)
                mainBinding.message.text = getString(R.string.message_worker_running)
                mainBinding.logs.text = getString(R.string.log_for_running)
            } else {
                mainBinding.appCompatButtonStart.text = getString(R.string.button_text_start)
                mainBinding.message.text = getString(R.string.message_worker_stopped)
                mainBinding.logs.text = getString(R.string.log_for_stopped)
            }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        mainBinding.appCompatButtonStart.setOnClickListener(object : View.OnClickListener() {
            override fun onClick(v: View?) {
                if (mainBinding.appCompatButtonStart.text.toString()
                        .equalsIgnoreCase(getString(R.string.button_text_start))
                ) {
                    // START Worker
                    val periodicWork: PeriodicWorkRequest =
                        Builder(MyWorker::class.java, 15, TimeUnit.MINUTES)
                            .addTag(TAG)
                            .build()
                    WorkManager.getInstance().enqueueUniquePeriodicWork(
                        "Location",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        periodicWork
                    )
                    Toast.makeText(
                        this@MainActivity,
                        "Location Worker Started : " + periodicWork.id,
                        Toast.LENGTH_SHORT
                    ).show()
                    mainBinding.appCompatButtonStart.text = getString(R.string.button_text_stop)
                    mainBinding.message.text = periodicWork.id.toString()
                    mainBinding.logs.text = getString(R.string.log_for_running)
                } else {
                    WorkManager.getInstance().cancelAllWorkByTag(TAG)
                    mainBinding.appCompatButtonStart.text = getString(R.string.button_text_start)
                    mainBinding.message.text = getString(R.string.message_worker_stopped)
                    mainBinding.logs.text = getString(R.string.log_for_stopped)
                }
            }
        })
    }

    private fun isWorkScheduled(workInfos: List<WorkInfo>?): Boolean {
        var running = false
        if (workInfos == null || workInfos.isEmpty()) return false
        for (workStatus in workInfos) {
            running =
                (workStatus.state == WorkInfo.State.RUNNING) or (workStatus.state == WorkInfo.State.ENQUEUED)
        }
        return running
    }

    /**
     * All about permission
     */
    private fun checkLocationPermission(): Boolean {
        val result3 = ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
        val result4 = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
        return result3 == PackageManager.PERMISSION_GRANTED &&
                result4 == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                val coarseLocation = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val fineLocation = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (coarseLocation && fineLocation) Toast.makeText(
                    this,
                    "Permission Granted",
                    Toast.LENGTH_SHORT
                ).show() else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}