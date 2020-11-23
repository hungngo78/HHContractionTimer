package com.hh.contractiontimer.ui.measure

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hh.contractiontimer.MainActivity

import com.hh.contractiontimer.R
import com.hh.contractiontimer.databinding.MeasurementFragmentBinding

class MeasurementFragment : Fragment() {

    var recycleViewAdapterAdapter: MeasurementRecycleViewAdapter? = null
    lateinit var recycleView : RecyclerView
    lateinit var databinding: MeasurementFragmentBinding

    companion object {
        fun newInstance() =
            MeasurementFragment()
    }

    private var viewModel: MeasurementViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.measurement_fragment, container, false)
         databinding = MeasurementFragmentBinding.bind(view)

        recycleView = view.findViewById<RecyclerView>(R.id.recycleView)
        recycleView.setHasFixedSize(true)
        recycleViewAdapterAdapter = MeasurementRecycleViewAdapter(activity as MainActivity)
        recycleView.adapter = recycleViewAdapterAdapter
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(MeasurementViewModel::class.java)
        databinding.viewModel = viewModel

        viewModel?.getContractionTimerList()?.observe(this.viewLifecycleOwner, Observer {
           // recycleView.adapter = null
            recycleViewAdapterAdapter?.setData(it)
            //recycleView.adapter = recycleViewAdapterAdapter
        })

    }



}
