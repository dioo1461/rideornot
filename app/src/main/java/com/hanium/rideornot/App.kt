package com.hanium.rideornot

import android.app.Application
import android.content.Context
import com.hanium.rideornot.domain.StationDatabase
import com.hanium.rideornot.repository.LineRepository
import com.hanium.rideornot.repository.StationExitRepository
import com.hanium.rideornot.repository.StationRepository
import com.hanium.rideornot.utils.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class App : Application() {
    companion object {
        val applicationScope = CoroutineScope(SupervisorJob())

        private val database by lazy { StationDatabase.getInstance(getApplicationContext()) }

        val lineRepository by lazy { LineRepository(database!!.lineDao()) }
        val stationRepository by lazy { StationRepository(database!!.stationDao()) }
        val stationExitRepository by lazy { StationExitRepository(database!!.stationExitDao()) }

        lateinit var instance: App

        lateinit var preferenceUtil: PreferenceUtil

        fun getApplicationContext(): Context {
            return instance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        preferenceUtil = PreferenceUtil(applicationContext)
        instance = this
    }
}