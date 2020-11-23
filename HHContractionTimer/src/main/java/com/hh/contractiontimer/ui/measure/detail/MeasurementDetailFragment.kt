package com.hh.contractiontimer.ui.measure.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

import com.hh.contractiontimer.R
import com.hh.contractiontimer.common.Define
import com.hh.contractiontimer.common.IntensityLevel
import com.hh.contractiontimer.databinding.FragmentMeasurementDetailBinding
import com.hh.contractiontimer.model.ContractionTimer
import com.hh.contractiontimer.ui.measure.MeasurementViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MeasurementDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MeasurementDetailFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var viewModel: MeasurementViewModel
    lateinit var databinding: FragmentMeasurementDetailBinding
    var contractionTimer : ContractionTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
        isCancelable = true
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_measurement_detail, container, false)
        viewModel = ViewModelProviders.of(requireActivity()).get(MeasurementViewModel::class.java)
        databinding = FragmentMeasurementDetailBinding.bind(view)
        databinding.viewModel = viewModel as MeasurementViewModel

        val index = getArguments()?.getInt(Define.KEY_CONTRACTION_TIMER_INDEX, -1) ?: -1
        val size = (viewModel as MeasurementViewModel).contractionTimerList.value?.size ?: 0
        if( index >= 0 && index < size) {
            contractionTimer = viewModel.contractionTimerList.value?.get(index)
            databinding.contractionTimer = contractionTimer
            viewModel.memo.set(contractionTimer?.memo ?: "")
            setCheckedButtonId(contractionTimer?.intensityLevel ?: IntensityLevel.MildLevel)
            databinding.notifyChange()
        }

        view.findViewById<Button>(R.id.done_btn).setOnClickListener{
            dialog?.dismiss()
            //update data to database
            contractionTimer?.let {
                val contractionTimerUpdate = ContractionTimer(it.id, it.start,
                    it.end, it.interval, it.duration, getIntensityValue(), viewModel.memo.get())
                viewModel.updateContractionTimer(contractionTimerUpdate)
            }
        }

        view.findViewById<ImageButton>(R.id.delete_btn).setOnClickListener{
            dialog?.dismiss()
            // delete contraction Timer
            contractionTimer?.id?.let {
                viewModel.deleteContractionTimer(it)}
        }
        return view
    }

    private fun getIntensityValue(): Int {
           return when (viewModel.checkedButtonId.get()) {
                R.id.intensity_mild_edit -> IntensityLevel.MildLevel.value
                R.id.intensity_moderate_edit -> IntensityLevel.ModerateLevel.value
                R.id.intensity_severe_edit -> IntensityLevel.SevereLevel.value
               else -> IntensityLevel.MildLevel.value
            }
    }
    private fun setCheckedButtonId(intensityLevel: IntensityLevel) {
        viewModel?.checkedButtonId.set(when (intensityLevel) {
             IntensityLevel.MildLevel -> R.id.intensity_mild_edit
             IntensityLevel.ModerateLevel -> R.id.intensity_moderate_edit
             IntensityLevel.SevereLevel -> R.id.intensity_severe_edit
        })
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MeasurementDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MeasurementDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
