package com.hh.contractiontimer.repository

import androidx.room.*
import com.hh.contractiontimer.model.ContractionTimer
import io.reactivex.*
import java.sql.Date

@Dao
interface ContractionTimerDAO {

    @Query("SELECT * FROM ContractionTimer ORDER BY start DESC ")
    fun getAllContractionTimer() : Flowable<List<ContractionTimer>>

    @Query( "SELECT * FROM ContractionTimer WHERE id = :id")
    fun getContractionTimeById(id: Long): Flowable<ContractionTimer>

    @Query( "SELECT * FROM ContractionTimer WHERE start BETWEEN :dateFrom AND :dateTo  ORDER BY id DESC ")
    fun getContractionTimeListLastHour(dateFrom: Long, dateTo: Long): Observable<List<ContractionTimer>>

    @Query("SELECT * FROM ContractionTimer ORDER BY start DESC LIMIT 1")
    fun getLastContractionTimer() : Single<ContractionTimer>

    @Query("SELECT * FROM ContractionTimer ORDER BY start DESC LIMIT 5")
    fun get5LastContractionTimer() : Flowable<List<ContractionTimer>>

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertContractionTimer(contractionTimer: ContractionTimer): Flowable<Long>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContractionTimer(contractionTimer: ContractionTimer): Maybe<Long>

    @Update
    fun updateContractionTimer(contractionTimer: ContractionTimer): Completable

    @Query("DELETE FROM ContractionTimer WHERE id =:id")
    fun deleteContractionTimerbyId(id: Long): Completable




}