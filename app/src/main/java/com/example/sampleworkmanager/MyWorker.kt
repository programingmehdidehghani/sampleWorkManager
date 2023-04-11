package com.example.sampleworkmanager

import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.*
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val DEFAULT_START_TIME = "08:00"
    private val DEFAULT_END_TIME = "19:00"

    private val TAG = "MyWorker"


    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000


    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2


    private var mLocation: Location? = null


    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private val mContext: Context? = null


    private var mLocationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    override fun doWork(): Result {
        Log.d(TAG, "doWork: Done")

        Log.d(TAG, "onStartJob: STARTING JOB..")

        val dateFormat: DateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val c = Calendar.getInstance()
        val date = c.time
        val formattedDate: String = dateFormat.format(date)

        try {
            val currentDate: Date = dateFormat.parse(formattedDate) as Date
            val startDate: Date = dateFormat.parse(DEFAULT_START_TIME) as Date
            val endDate: Date = dateFormat.parse(DEFAULT_END_TIME) as Date
            if (currentDate.after(startDate) && currentDate.before(endDate)) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext!!)
                mLocationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                    }
                }
                val mLocationRequest = LocationRequest()
                mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
                mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                try {
                    mFusedLocationClient!!
                        .getLastLocation()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful && task.result != null) {
                                mLocation = task.result
                                Log.d(TAG, "Location : $mLocation")

                                // Create the NotificationChannel, but only on API 26+ because
                                // the NotificationChannel class is new and not in the support library
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val name: CharSequence = mContext.getString(R.string.app_name)
                                    val description = mContext.getString(R.string.app_name)
                                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                                    val channel = NotificationChannel(
                                        mContext.getString(R.string.app_name),
                                        name,
                                        importance
                                    )
                                    channel.description = description
                                    // Register the channel with the system; you can't change the importance
                                    // or other notification behaviors after this
                                    val notificationManager = mContext.getSystemService(
                                        NotificationManager::class.java
                                    )
                                    notificationManager.createNotificationChannel(channel)
                                }
                                val builder = NotificationCompat.Builder(
                                    mContext,
                                    mContext.getString(R.string.app_name)
                                )
                                    .setSmallIcon(R.drawable.ic_menu_mylocation)
                                    .setContentTitle("New Location Update")
                                    .setContentText(
                                        "You are at " + getCompleteAddressString(
                                            mLocation.getLatitude(),
                                            mLocation.getLongitude()
                                        )
                                    )
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setStyle(
                                        NotificationCompat.BigTextStyle().bigText(
                                            "You are at " + getCompleteAddressString(
                                                mLocation.getLatitude(),
                                                mLocation.getLongitude()
                                            )
                                        )
                                    )
                                val notificationManager = NotificationManagerCompat.from(
                                    mContext
                                )

                                // notificationId is a unique int for each notification that you must define
                                notificationManager.notify(1001, builder.build())
                                mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                            } else {
                                Log.w(TAG, "Failed to get location.")
                            }
                        }
                } catch (unlikely: SecurityException) {
                    Log.e(TAG, "Lost location permission.$unlikely")
                }
                try {
                    mFusedLocationClient!!.requestLocationUpdates(mLocationRequest, null)
                } catch (unlikely: SecurityException) {
                    //Utils.setRequestingLocationUpdates(this, false);
                    Log.e(TAG, "Lost location permission. Could not request updates. $unlikely")
                }
            } else {
                Log.d(
                    TAG,
                    "Time up to get location. Your time is : $DEFAULT_START_TIME to $DEFAULT_END_TIME"
                )
            }
        } catch (ignored: ParseException) {
        }

        return Result.success()
    }

    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String? {
        var strAdd = ""
        val geocoder = Geocoder(mContext!!, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress: Address = addresses[0]
                val strReturnedAddress = StringBuilder()
                for (i in 0..returnedAddress.getMaxAddressLineIndex()) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return strAdd
    }
}