package com.hh.contractiontimer.common

sealed class IntensityLevel(val value: Int, val description: String) {
    object MildLevel : IntensityLevel(1, "Mild")
    object ModerateLevel : IntensityLevel(2, "Moderate" )
    object SevereLevel: IntensityLevel(3, "Severe")
}