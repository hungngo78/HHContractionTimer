package com.hh.contractiontimer.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hh.contractiontimer.model.ContractionTimer
import com.hh.contractiontimer.model.User

@Database(entities = [ContractionTimer::class, User::class], version = 1, exportSchema = false)
abstract class ContractionTimerDB: RoomDatabase() {
    abstract fun contractionTimerDao() : ContractionTimerDAO
    abstract fun userDao(): UserDAO
}