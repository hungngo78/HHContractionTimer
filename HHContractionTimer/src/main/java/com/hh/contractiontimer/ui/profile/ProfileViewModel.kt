package com.hh.contractiontimer.ui.profile

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hh.contractiontimer.R
import com.hh.contractiontimer.common.HHContractionTimerApp
import com.hh.contractiontimer.model.User
import com.hh.contractiontimer.repository.ContractionTimerRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ProfileViewModel: ViewModel() {

    var repository = ContractionTimerRepository()
    var user = MutableLiveData<User>(User())

    fun getUser(){
        repository.getUser().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
                user.postValue(it)
            },{
                println("get user error")
                user.postValue(User())})
    }

    fun saveProfile() {
        user.value?.let {
            it.estimatedDateBirth = it.getEstimateBabyDate() ?: 0L

            if (it.id!! > 0L) {
                repository.updateUser(it).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ Toast.makeText(
                        HHContractionTimerApp.myApplicationContext,
                        HHContractionTimerApp.myApplicationContext.getString(R.string.save_successful), Toast.LENGTH_SHORT).show()}, {Toast.makeText(
                        HHContractionTimerApp.myApplicationContext,
                        HHContractionTimerApp.myApplicationContext.getString(R.string.save_error), Toast.LENGTH_SHORT).show()})
            } else
               repository.insertUser(it).subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread()).subscribe({ Toast.makeText(
                       HHContractionTimerApp.myApplicationContext,
                       HHContractionTimerApp.myApplicationContext.getString(R.string.save_successful), Toast.LENGTH_SHORT).show()},
                       {Toast.makeText(
                           HHContractionTimerApp.myApplicationContext,
                           HHContractionTimerApp.myApplicationContext.getString(R.string.save_error), Toast.LENGTH_SHORT).show()})
        }
    }




}