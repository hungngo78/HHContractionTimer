package com.hh.contractiontimer.common

import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*

class Utils {

    companion object{

        fun getStringHHMMSS(interval: Long) : String{
            val dayLength = 1000 * 60 * 60 * 24.toLong()
            val dayMs = interval % dayLength
            val percentOfDay = dayMs.toDouble() / dayLength
            val hour = (percentOfDay * 24).toInt()
            val minute = (percentOfDay * 24 * 60).toInt() % 60
            val second = (percentOfDay * 24 * 60 * 60).toInt() % 60
            return ""+hour + " : " + minute + " : " + second
        }

        fun getDateString(time: Long): String{
            val df = SimpleDateFormat("yyyy/dd/MM HH:mm:ss")
            //df.setTimeZone(TimeZone.getTimeZone("GMT"))
            return df.format(time)
        }
        fun getDateFromMillis(d: Long): String? {
            val df = SimpleDateFormat("HH:mm:ss")
            df.setTimeZone(TimeZone.getTimeZone("GMT"))
            return df.format(d)
        }

        fun getDate(dateTime: Long) : Int{
            if (dateTime == 0L) {
                return Calendar.getInstance().get(Calendar.DATE)
            } else {
                var t = Calendar.getInstance()
                t.timeInMillis = dateTime
                return t.get(Calendar.DATE)
            }
        }
        fun getMonth(dateTime: Long) : Int{
            println("dateTime = " + dateTime)
            if (dateTime == 0L) {
                return Calendar.getInstance().get(Calendar.MONTH)
            } else {
                var t = Calendar.getInstance()
                t.timeInMillis = dateTime
                return t.get(Calendar.MONTH)
            }
        }
        fun getYear(dateTime: Long) : Int{
            println("dateTime = " + dateTime)
            if (dateTime == 0L) {
                return Calendar.getInstance().get(Calendar.YEAR)
            } else {
                var t = Calendar.getInstance()
                t.timeInMillis = dateTime
                return t.get(Calendar.YEAR)
            }
        }

        fun getDateTime(day : Int?, month : Int?, year : Int?) : Long{
            println("date = " + day + " month = " + month + " year = " + year)
            var c = Calendar.getInstance()
                c.set(year!!, month!!, day!!)

            return c.timeInMillis

        }
    }
}