package com.hanium.rideornot.domain

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Room Database 클래스 정의
@Database(entities = [SearchHistory::class], version = 1)
abstract class SearchHistoryDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        private var instance: SearchHistoryDatabase? = null

//        fun getInstance(context: Context): SearchHistoryDatabase {
//            return instance ?: synchronized(this) {
//                instance ?: buildDatabase(context).also { instance = it }
//            }
//        }
        
        @Synchronized
        fun getInstance(context: Context): SearchHistoryDatabase? {
            if (instance == null) {
                synchronized(SearchHistoryDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SearchHistoryDatabase::class.java,
                        "search-history-database"
                    ).build()
                }
            }

            return instance
        }
    }

}