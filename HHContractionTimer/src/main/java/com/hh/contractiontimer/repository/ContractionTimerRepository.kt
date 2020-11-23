package com.hh.contractiontimer.repository

import androidx.room.Room
import com.hh.contractiontimer.common.HHContractionTimerApp
import com.hh.contractiontimer.model.ContractionTimer
import com.hh.contractiontimer.model.User
import io.reactivex.*

class ContractionTimerRepository() {

    private val databaseName = "contractiont_timer"
    private var contractionTimerDB: ContractionTimerDB? = null

    private fun getInstance() : ContractionTimerDB{
        if (contractionTimerDB == null) {
            contractionTimerDB =
                Room.databaseBuilder(HHContractionTimerApp.myApplicationContext, ContractionTimerDB::class.java, databaseName).build()
        }
        return contractionTimerDB as ContractionTimerDB
    }

    // Access data of Contraction Timer table
    fun insertContractionTimer(contractionTimer: ContractionTimer) : Maybe<Long> {
       return getInstance().contractionTimerDao().insertContractionTimer(contractionTimer)
    }

    fun getAllContractionTimer() : Flowable<List<ContractionTimer>> {
        return getInstance().contractionTimerDao().getAllContractionTimer()
    }

    fun getLast30MinContractionTimer(from: Long, to: Long) : Observable<List<ContractionTimer>> {
        return getInstance().contractionTimerDao().getContractionTimeListLastHour(from, to)
    }

    fun get5LastContractionTimer() : Flowable<List<ContractionTimer>> {
        return getInstance().contractionTimerDao().get5LastContractionTimer()
    }

    fun getLastContractionTimer() : Single<ContractionTimer> {
        return getInstance().contractionTimerDao().getLastContractionTimer()
    }

    fun updateContractionTimer(contractionTimer: ContractionTimer) : Completable {
        return getInstance().contractionTimerDao().updateContractionTimer(contractionTimer)
    }
    fun deleteContractionTimer(id: Long) : Completable {
        return getInstance().contractionTimerDao().deleteContractionTimerbyId(id)
    }

    // Access data of User table
    fun getUser(): Single<User> {
        return getInstance().userDao().getUser()
    }
    fun updateUser(user: User): Completable{
        return getInstance().userDao().updateUser(user)
    }
    fun insertUser(user: User): Maybe<Long>{
        return getInstance().userDao().insertUser(user)
    }
}