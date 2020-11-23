package com.hh.contractiontimer.ui.summary

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.github.mikephil.charting.charts.LineChart
import com.google.android.material.snackbar.Snackbar
import com.hh.contractiontimer.MainActivity
import com.hh.contractiontimer.R
import com.hh.contractiontimer.databinding.SummaryFragmentBinding
import com.hh.contractiontimer.ui.measure.MeasurementViewModel

class SummaryFragment : Fragment() {

    private lateinit var chart : LineChart

    private lateinit var adapter: SummaryExpandableListAdapter
    private lateinit var expandableListView: ExpandableListView

    companion object {
        fun newInstance() = SummaryFragment()
    }

    lateinit var databinding: SummaryFragmentBinding
    private lateinit var viewModel: MeasurementViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.summary_fragment, container, false)
        databinding =  SummaryFragmentBinding.bind(view)

        expandableListView = view.findViewById(R.id.expandableListView)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(MeasurementViewModel::class.java)
        databinding.viewModel = viewModel

        val titleList = ArrayList<String>()
        titleList.add("Contraction Durations")
        titleList.add("Interval Durations")
        titleList.add("Contraction Timeline (30 min)")
        titleList.add("Intensity Distribution")
        adapter = SummaryExpandableListAdapter(this, viewModel, titleList)

        expandableListView.setAdapter(adapter)

        expandableListView.expandGroup(0)
        expandableListView.expandGroup(1)
        expandableListView.expandGroup(2)
        expandableListView.expandGroup(3)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_summary, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.nav_saveSummary) {
            println("Hung, onOptionsItemSelected")
            // save
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //saveToGallery()
                println("Hung, save summary")
                adapter.pieChart.saveToGallery("summary2")
                adapter.chart.saveToGallery("summary1")


                Toast.makeText(context, this.getText(R.string.save_summary_success), Toast.LENGTH_SHORT).show()

                // go up
//                val navController =
//                    Navigation.findNavController(context as MainActivity, R.id.nav_host_fragment)
//                return navController.navigateUp()
            } else {
                println("Hung, save summary, requestStoragePermission")
                requestStoragePermission()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        if (requestCode == PERMISSION_STORAGE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //saveToGallery()
                println("Hung, save to Gallery")
                adapter.pieChart.saveToGallery("summary2")
                adapter.chart.saveToGallery("summary1")

            } else {
                println("Hung, Request Save Permission FAILED!")
                Toast.makeText(
                    requireActivity()?.applicationContext,
                    "Request Save Permission FAILED!",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val PERMISSION_STORAGE = 0
    protected fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            println("Hung, requestStoragePermission 1")

            Snackbar.make(requireView(), "Write permission is required to save image to gallery", Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION_STORAGE)
                }.show()
        } else {
            println("Hung, requestStoragePermission 2")

            Toast.makeText(requireActivity()?.applicationContext, "Permission Required!", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_STORAGE)
        }
    }

}
