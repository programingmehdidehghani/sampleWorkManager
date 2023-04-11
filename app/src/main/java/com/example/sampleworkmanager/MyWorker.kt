package com.example.sampleworkmanager

import android.content.Context
import android.location.Location
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback


class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val DEFAULT_START_TIME = "08:00"
    private val DEFAULT_END_TIME = "19:00"

    private val TAG = "MyWorker"


    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000


    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2


    private val mLocation: Location? = null


    private val mFusedLocationClient: FusedLocationProviderClient? = null

    private val mContext: Context? = null


    private val mLocationCallback: LocationCallback? = null

    override fun doWork(): Result {
        TODO("Not yet implemented")
    }
}