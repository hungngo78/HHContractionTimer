package com.hh.contractiontimer.ui.measure

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.Navigation

import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.hh.contractiontimer.MainActivity
import com.hh.contractiontimer.R
import com.hh.contractiontimer.common.Define
import com.hh.contractiontimer.common.HHContractionTimerApp
import com.hh.contractiontimer.common.IntensityLevel
import com.hh.contractiontimer.databinding.MeasureListItemBinding
import com.hh.contractiontimer.model.ContractionTimer

class MeasurementRecycleViewAdapter(val activity: MainActivity): RecyclerView.Adapter<MeasurementRecycleViewAdapter.ViewHolder>() {

    var list : List<ContractionTimer>? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasurementRecycleViewAdapter.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val dataBinding = MeasureListItemBinding.inflate(inflater, parent, false)
        val viewHolder = ViewHolder(dataBinding.root)
        viewHolder.onBind(dataBinding)
        return viewHolder
    }

    fun setData(listItem : List<ContractionTimer>?) {
        list = ArrayList<ContractionTimer>()
        list = listItem
        notifyDataSetChanged()

    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: MeasurementRecycleViewAdapter.ViewHolder, position: Int) {
        val contractionTimer = list?.get(position)
        holder.setData(contractionTimer)
        holder.itemView.setOnClickListener{
            val navController = Navigation.findNavController(activity, R.id.nav_host_fragment)
            var bundle = Bundle()
            bundle.putInt(Define.KEY_CONTRACTION_TIMER_INDEX, position)
            bundle.putSerializable(Define.KEY_CONTRACTION_TIMER, list?.get(position))
            navController.navigate(R.id.action_MeasurementFragment_to_MeasurementDetailFragment, bundle)
        }
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dataBinding : MeasureListItemBinding? = null
        fun onBind(binding: MeasureListItemBinding?) {
            dataBinding = binding
            dataBinding?.executePendingBindings()

        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun setData(contractionTimer: ContractionTimer?) {
            dataBinding?.contractionTimer = contractionTimer

             when (contractionTimer?.intensityLevel) {
                IntensityLevel.MildLevel -> {
                    dataBinding?.image = HHContractionTimerApp.myApplicationContext.resources.getDrawable(
                    R.drawable.ic_intensity_mild)}
                IntensityLevel.ModerateLevel -> {
                    dataBinding?.image = HHContractionTimerApp.myApplicationContext.resources.getDrawable(
                    R.drawable.ic_intensity_moderate) }
                IntensityLevel.SevereLevel -> {
                    dataBinding?.image = HHContractionTimerApp.myApplicationContext.resources.getDrawable(
                        R.drawable.ic_intensity_severe)
                }
            }
        }
    }



}