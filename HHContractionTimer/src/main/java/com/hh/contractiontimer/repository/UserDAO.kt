package com.hh.contractiontimer.repository

import androidx.room.*
import com.hh.contractiontimer.model.User
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface UserDAO {

    @Query("SELECT * FROM User")
    fun getUser() : Single<User>

    @Query( "SELECT * FROM User WHERE id = :id")
    fun getUserById(id: Long): Single<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User): Maybe<Long>

    @Update
    fun updateUser(user: User): Completable

}