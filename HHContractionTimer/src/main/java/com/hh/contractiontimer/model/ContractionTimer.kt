package com.hh.contractiontimer.model

import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hh.contractiontimer.R
import com.hh.contractiontimer.common.HHContractionTimerApp
import com.hh.contractiontimer.common.IntensityLevel
import com.hh.contractiontimer.common.Utils
import java.io.Serializable

@Entity
data class ContractionTimer( @PrimaryKey(autoGenerate = true)
                             var id: Long? = null,
                             @ColumnInfo val start: Long,
                            @ColumnInfo val end: Long,
                            @ColumnInfo val interval: Long,
                            @ColumnInfo val duration: Long,
                            @ColumnInfo val intensity: Int,
                            @ColumnInfo val memo: String?): Serializable {


    @Transient
    var intensityLevel : IntensityLevel = IntensityLevel.MildLevel
        get() {
            if (intensity == 1)  return IntensityLevel.MildLevel
            else if (intensity == 2)  return IntensityLevel.ModerateLevel
            else return IntensityLevel.SevereLevel
        }
    @Transient
    var intervalString : String = ""
    get() { return HHContractionTimerApp.myApplicationContext.getString(R.string.interval) + " " + Utils.getDateFromMillis(interval) }

    @Transient
    var startDateString: String = ""
    get() { return Utils.getDateString(start) }

    @Transient
    var endDateString: String = ""
        get() { return Utils.getDateString(end) }

    @Transient
    var durationString: String = ""
        get() {return  "  " + HHContractionTimerApp.myApplicationContext.getString(R.string.duration)  + " " + Utils.getDateFromMillis(duration)}

}