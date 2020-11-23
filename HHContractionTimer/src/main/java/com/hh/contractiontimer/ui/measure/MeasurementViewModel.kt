package com.hh.contractiontimer.ui.measure

import android.os.AsyncTask
import android.os.Handler
import android.widget.RadioGroup
import android.widget.Toast
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hh.contractiontimer.R
import com.hh.contractiontimer.common.HHContractionTimerApp

import com.hh.contractiontimer.common.IntensityLevel
import com.hh.contractiontimer.common.Utils
import com.hh.contractiontimer.model.ContractionTimer
import com.hh.contractiontimer.repository.ContractionTimerRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

import io.reactivex.schedulers.Schedulers

class MeasurementViewModel : ViewModel() {

    private var repository = ContractionTimerRepository()
    var isMeasuring = ObservableBoolean(false)
    var isFinished = ObservableBoolean(false)
    var duration = ObservableField<String>("00:00:00")
    var handleTimeCount = Handler()
    var startMeasure : Long = 0L
    var endMeasure: Long = 0L
    private var intensity: IntensityLevel = IntensityLevel.MildLevel
    var compositeDisposable = CompositeDisposable()
    private val TWOHOURS = 2 * 60 * 60 * 1000
    var clearCheck = ObservableInt(-1)
    var checkedButtonId = ObservableInt(R.id.intensity_mild_edit)
    var memo = ObservableField<String>("")

    var lastContractionDuration = ObservableField<String>("00:00:00")
    var averageOfFivelastContractionDuration = ObservableField<String>("00:00:00")
    var lastInterval = ObservableField<String>("00:00:00")
    var averageOfLastFiveContractionInterval = ObservableField<String>("00:00:00")
    var last30MinContractionList = MutableLiveData<List<ContractionTimer>>()

    var lowIntensityLevelCount = ObservableField<String> ("0")
    var mediumIntensityLevelCount = ObservableField<String> ("0")
    var highIntensityLevelCount = ObservableField<String> ("0")

    fun getLastContractionTimer() {
        repository.getLastContractionTimer().subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( {
                lastContractionDuration.set(Utils.getDateFromMillis(it.duration))
                lastInterval.set(Utils.getDateFromMillis(it.interval))
            },
                { println("Hung, Error 1")})
    }

    fun getLast5ContractionTimer() {
        repository.get5LastContractionTimer().subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    println("Hung, get5LastContractionTimer() success")
//                    it.forEach({ ct ->
//                        println("  Hung, " + ct.startDateString)
//                    })
                    val durationAvg : Long = it.map { item -> item.duration}.average().toLong()
                    averageOfFivelastContractionDuration.set(Utils.getDateFromMillis(durationAvg))

                    val intervalAvg : Long = it.map { item -> item.interval}.average().toLong()
                    averageOfLastFiveContractionInterval.set(Utils.getDateFromMillis(intervalAvg))
                },
                {
                    println("Hung, Error 2 with msg = ${it.message}")
                }
            )
    }

    fun getLast30MinContractionTimer(currentTime: Long) {
        //val to = System.currentTimeMillis()
        val from = currentTime - 30 * 60 * 1000
        repository.getLast30MinContractionTimer(from, currentTime).subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
//                println("Hung, getLast30MinContractionTimer() success")
//                it.forEach({ ct ->
//                        println("  Hung, " + ct.startDateString + ", duration="+ct.intensityLevel)
//                    })
                last30MinContractionList.value = it
            },
            {
                println("Hung, Error 3 " + it.message) })
    }

    //var contractionTimerLiveData : LiveData<List<ContractionTimer>>? = null
    var contractionTimerList = MutableLiveData<List<ContractionTimer>>()

    fun getContractionTimerList() : LiveData<List<ContractionTimer>>{
        loadContractionTimer()
        return contractionTimerList
    }

    fun onClickStop() {
        println("Hung, onClickStop")
        isMeasuring.set(false)
        isFinished.set(true)
        handleTimeCount.removeCallbacks(updateTimer)
        endMeasure = System.currentTimeMillis()
    }
    fun onClickIntensity(view: RadioGroup, id: Int){
        if (clearCheck.get() == -1) {
            return
        }
        isMeasuring.set(false)
        isFinished.set(false)
        if (id == -1) return
        println("Hung, onClickIntensity with id = " + id + " and view id = " + view.checkedRadioButtonId)
        when(id) {
            R.id.intensity_mild -> {intensity = IntensityLevel.MildLevel}
            R.id.intensity_moderate -> {intensity = IntensityLevel.ModerateLevel}
            R.id.intensity_severe -> {intensity = IntensityLevel.SevereLevel}
        }
        clearCheck.set(-1)
        view.clearCheck()
        // save contraction timer record to database
        val lastContractionTimer = repository.getLastContractionTimer()?.subscribeOn(Schedulers.computation())
        compositeDisposable.add(lastContractionTimer.observeOn(AndroidSchedulers.mainThread())?.subscribe ({
            println("Successful")

            insertContractionTimerToDatabase(startMeasure, endMeasure, intensity,"",
            it?.start ?: 0L)
        },
            { println("Error")
                insertContractionTimerToDatabase(startMeasure, endMeasure, intensity,"",
                     0L)}))
    }

    /**
     * click on Start button to counter contractionTimer
     */
    fun onClickStart() {
        println("Hung, onClickStart")
        clearCheck.set(0)
        isMeasuring.set(true)
        isFinished.set(false)
        startMeasure = System.currentTimeMillis()
        endMeasure = System.currentTimeMillis()
        //count duration
        handleTimeCount.postDelayed(updateTimer, 0)

    }
    private fun countTime() {
        if (isMeasuring.get())
            handleTimeCount.postDelayed(updateTimer, 1000)
    }


    private var updateTimer = Runnable {
        val time = Utils.getDateFromMillis(System.currentTimeMillis() - startMeasure)
        duration.set(time)
        countTime()
    }

    fun insertContractionTimerToDatabase(start: Long, end: Long, intensityLevel: IntensityLevel, memo: String = "", lastTime: Long = 0L) {
        val duration = end - start
        val interval = start - lastTime
        var contractionTimer = ContractionTimer(null, start, end, interval, duration, intensityLevel.value, memo)
        insertContractionTimer(contractionTimer)

    }
    private fun insertContractionTimer(contractionTimer: ContractionTimer) {
        compositeDisposable.add(repository.insertContractionTimer(contractionTimer)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
            println("insert = "+ it)
                Toast.makeText(
                    HHContractionTimerApp.myApplicationContext,
                    HHContractionTimerApp.myApplicationContext.getString(R.string.insert_successfull), Toast.LENGTH_SHORT).show()
            // if during 2 hours, it has more than 80% contractionTimer which have interval <= 10 and > 5
//                    // OR during 1 hour, it has more than 80% contractionTimer which have interval <= 5 -> show alert to user to ask
//                    //her go to hospital (night time) or call to doctor (daylight time).

        })

    }

    fun updateContractionTimer(contractionTimer: ContractionTimer){
//        val duration = startMeasure - endMeasure
//        var contractionTimer = ContractionTimer(startMeasure, endMeasure,duration,)
        repository.updateContractionTimer(contractionTimer).subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                println("updated")
            }

    }

    fun deleteContractionTimer(id: Long) {
        AsyncTask.execute {
            repository.deleteContractionTimer(id).subscribeOn(Schedulers.io())
            .subscribe{
                println("deleted")
            }
        }

    }

    // asynchronyn task
    private fun loadContractionTimer() {

        // query data from database
        repository.getAllContractionTimer().subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    println("Hung, getAllContractionTimer() success")
                    if (it != null) {
                        contractionTimerList.postValue(it)

                        var lowCount = 0
                        var mediumCount = 0
                        var highCount = 0
                        it.forEach() {ct ->
                            if (ct.intensityLevel == IntensityLevel.MildLevel)
                                lowCount += 1
                            else if (ct.intensityLevel == IntensityLevel.ModerateLevel)
                                mediumCount += 1
                            else
                                highCount += 1
                        }

                        lowIntensityLevelCount.set(lowCount.toString())
                        mediumIntensityLevelCount.set(mediumCount.toString())
                        highIntensityLevelCount.set(highCount.toString())
                    }
                },{println("Error")})
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }


}
