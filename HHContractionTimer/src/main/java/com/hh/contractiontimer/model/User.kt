package com.hh.contractiontimer.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hh.contractiontimer.common.Utils

@Entity
data class User (

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo
    var emailAddress: String = "",
    @ColumnInfo
    var firstName: String= "",
    @ColumnInfo
    var lastName: String= "",
    @ColumnInfo
    var estimatedDateBirth: Long = 0L,
    @ColumnInfo
    var birthYearOfUser: String = "",
// information of Doctor
    @ColumnInfo
    var firstNameOfDoctor: String="",
    @ColumnInfo
    var lastNameOfDoctor: String ="",
    @ColumnInfo
    var phoneNumberOfDoctor: String= "",
    @ColumnInfo
    var emailAddressOfDoctor: String = "",
// information or Spouse
    @ColumnInfo
    var phoneNumberOfSpouse: String= "",
    @ColumnInfo
    var emailAddressOfSpouse: String = "") {

    var estimateBabyDay: Int?
        get() {
            return Utils.getDate(estimatedDateBirth)
        }
    set(value) { estimatedDateBirth = Utils.getDateTime(value, estimateBabyMonth, estimateBabyYear)}

    var estimateBabyMonth: Int?
        get() {return Utils.getMonth(estimatedDateBirth)}
        set(value) { estimatedDateBirth = Utils.getDateTime(estimateBabyDay, value, estimateBabyYear)}

    var estimateBabyYear: Int?
        get() {return Utils.getYear(estimatedDateBirth)}
        set(value) { estimatedDateBirth = Utils.getDateTime(estimateBabyDay, estimateBabyMonth, value)}

    fun getEstimateBabyDate() : Long{
         return Utils.getDateTime(estimateBabyDay, estimateBabyMonth, estimateBabyYear)

    }
}