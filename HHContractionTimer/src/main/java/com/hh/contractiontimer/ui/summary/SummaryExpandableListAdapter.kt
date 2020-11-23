package com.hh.contractiontimer.ui.summary

import android.database.DataSetObserver
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.hh.contractiontimer.R
import com.hh.contractiontimer.common.IntensityLevel
import com.hh.contractiontimer.databinding.SummaryListItemDurationBinding
import com.hh.contractiontimer.databinding.SummaryListItemIntervalBinding
import com.hh.contractiontimer.databinding.SummaryListItemPieChartBinding
import com.hh.contractiontimer.ui.measure.MeasurementViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SummaryExpandableListAdapter constructor(private val fragment: Fragment,
                                               private val viewModel: MeasurementViewModel,
                                               private val titleList: List<String>) : BaseExpandableListAdapter() {

    lateinit var chart : LineChart
    lateinit var pieChart: PieChart
    private lateinit var disposable: Disposable
    private var currentTime : Long = System.currentTimeMillis()

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        super.unregisterDataSetObserver(observer)
        disposable.dispose()
    }

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return ""
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return 0
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return 1
    }

    override fun getChildView(listPosition: Int, expandedListPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val expandedListText = getChild(listPosition, expandedListPosition) as String
        if (convertView == null) {
            //val layoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layoutInflater = LayoutInflater.from(parent.context)
            if (listPosition == 0) {
                viewModel?.getLastContractionTimer()

                val dataBinding = SummaryListItemDurationBinding.inflate(layoutInflater, parent, false)
                dataBinding.viewModel = viewModel

                convertView = dataBinding.root
            } else if (listPosition == 1) {
                viewModel?.getLast5ContractionTimer()

                val dataBinding = SummaryListItemIntervalBinding.inflate(layoutInflater, parent, false)
                dataBinding.viewModel = viewModel

                convertView = dataBinding.root
            } else if (listPosition == 2) {
                println("Hung, getView for LineChart ----------------")

                convertView =
                    layoutInflater.inflate(R.layout.summary_list_item_line_chart, parent, false)
                chart = convertView!!.findViewById(R.id.chart1)

                chart.getDescription().setEnabled(false);
                chart.setDrawGridBackground(false);

                //chart.animateX(3000);

                val leftAxis: YAxis = chart.getAxisLeft();
                leftAxis.setAxisMaximum(4f);
                leftAxis.setAxisMinimum(0.0f);

                chart.getAxisRight().setEnabled(false);

                val xAxis: XAxis = chart.getXAxis();
                xAxis.setEnabled(false);

                // observe ObservableField and update data of LineChart
                setLineChartData()

                // every 5s get new data from DB -> update ObservableField in ViewModel
                disposable = Observable.interval(0, 5, TimeUnit.SECONDS)
                    //.repeat()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        currentTime = System.currentTimeMillis()
                        viewModel?.getLast30MinContractionTimer(currentTime)
                    }
            } else if (listPosition == 3) {
                println("Hung, getView for PieChart ----------------")
                //viewModel?.getContractionTimerList()

                val dataBinding = SummaryListItemPieChartBinding.inflate(layoutInflater, parent, false)
                dataBinding.viewModel = viewModel

                //convertView = layoutInflater.inflate(R.layout.summary_list_item_pie_chart, parent, false)
                convertView = dataBinding.root

                pieChart = convertView!!.findViewById(R.id.pieChart)

                pieChart.setUsePercentValues(true)
                pieChart.description.isEnabled = false
                pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

                pieChart.dragDecelerationFrictionCoef = 0.95f

                pieChart.setCenterText(generateCenterSpannableText())

                pieChart.setDrawHoleEnabled(true)
                pieChart.setHoleColor(Color.WHITE)

                pieChart.setTransparentCircleColor(Color.WHITE)
                pieChart.setTransparentCircleAlpha(110)

                pieChart.setHoleRadius(58f)
                pieChart.setTransparentCircleRadius(61f)

                pieChart.setDrawCenterText(true)

                pieChart.setRotationAngle(0f)

                // enable rotation of the chart by touch
                pieChart.setRotationEnabled(true)
                pieChart.isHighlightPerTapEnabled = true

                pieChart.animateY(1400, Easing.EaseInOutQuad)
                // chart.spin(2000, 0, 360);

                // chart.spin(2000, 0, 360);
                val l = pieChart.legend
                l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
                l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                l.orientation = Legend.LegendOrientation.VERTICAL
                l.setDrawInside(false)
                l.xEntrySpace = 7f
                l.yEntrySpace = 0f
                l.yOffset = 0f

                // entry label styling
                pieChart.setEntryLabelColor(Color.WHITE)
                pieChart.setEntryLabelTextSize(12f)

                setPieChartData()
            }
        }

        return convertView!!
    }


    private fun setPieChartData() {
        val entries = java.util.ArrayList<PieEntry>()

        var mildCount = 0
        var moderateCount = 0
        var severeCount = 0

        println("Hung, setPieChartData() ----------------")

        viewModel?.contractionTimerList?.observe(fragment, Observer {
            pieChart.clear()
            pieChart.data?.clearValues()

            it.forEach{ ct ->
                if (ct.intensity == IntensityLevel.MildLevel.value)
                    mildCount += 1
                else if (ct.intensity == IntensityLevel.ModerateLevel.value)
                    moderateCount += 1
                else
                    severeCount += 1
            }

            //println("Hung, getPieChartData(), mildCount="+mildCount+ ", moderateCount="+moderateCount+ ", severeCount="+severeCount)
            //println("Hung, (mildCount/it.size)="+(mildCount.toFloat()/it.size))
            //println("Hung, (moderateCount/it.size)="+(moderateCount.toFloat()/it.size))
            //println("Hung, (severeCount/it.size)="+(severeCount.toFloat()/it.size))
            entries.clear()
            entries.add(PieEntry(mildCount.toFloat()/it.size, "Mild", null))
            entries.add(PieEntry(moderateCount.toFloat()/it.size, "Moderate", null))
            entries.add(PieEntry(severeCount.toFloat()/it.size, "Severe", null))

            //val dataSet = PieDataSet(entries, "Intensity Distribution")
            val dataSet = PieDataSet(entries, "")
            dataSet.setDrawIcons(false)
            dataSet.sliceSpace = 3f
            dataSet.iconsOffset = MPPointF(0f, 40f)
            dataSet.selectionShift = 5f


            // add a lot of colors
            val colors = java.util.ArrayList<Int>()
//            for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)
//            for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
//            for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
//            colors.add(ColorTemplate.getHoloBlue())
            colors.add(Color.BLUE)
            colors.add(Color.rgb(255,165,0))
            colors.add(Color.RED)

            dataSet.colors = colors
            //dataSet.setSelectionShift(0f);

            //dataSet.setSelectionShift(0f);
            val data = PieData(dataSet)
            data.setValueFormatter(PercentFormatter(pieChart))
            data.setValueTextSize(9f)
            data.setValueTextColor(Color.WHITE)
            //data.setValueTypeface(tfLight)
            pieChart.setData(data)

            // undo all highlights
            pieChart.highlightValues(null)

            pieChart.invalidate()
        })
    }

    private fun setLineChartData() {
        viewModel?.last30MinContractionList?.observe(fragment, Observer {
            chart.clear()
            chart.data?.clearValues()

            val sets = ArrayList<ILineDataSet>()

            var entriesLevel1: ArrayList<Entry> = ArrayList()        // mild
            var entriesLevel2: ArrayList<Entry> = ArrayList()        // medium
            var entriesLevel3: ArrayList<Entry> = ArrayList()        // severe

            entriesLevel1.add(Entry(0.0f, 0.0f))
            entriesLevel1.add(Entry(1800f, 0.0f))
            entriesLevel2.add(Entry(0.0f, 0.0f))
            entriesLevel2.add(Entry(1800f, 0.0f))
            entriesLevel3.add(Entry(0.0f, 0.0f))
            entriesLevel3.add(Entry(1800f, 0.0f))


            val startTime = currentTime - 30 * 60 * 1000
            it.forEach{
                var entry1 : Entry? = null
                var entry2: Entry? = null
                var entry3: Entry? = null
                var entrya : Entry? = null
                var entryb : Entry? = null

                val offsetX : Float = ((it.start - startTime)/1000).toFloat()
                val offsetY : Float = when (it.intensityLevel) {
                    IntensityLevel.MildLevel -> 1f
                    IntensityLevel.ModerateLevel -> 2f
                    IntensityLevel.SevereLevel -> 3f
                }

                val duration : Float = (it.duration/1000).toFloat()

                //println("OffsetX="+offsetX + ", OffsetY="+offsetY + ", Duration="+duration)
                //println("start =" +(startTime/1000).toFloat())
                //println("end ="+(currentTime/1000).toFloat())
                entrya = Entry(offsetX, 0.0f)
                entry1 = Entry(offsetX, 0.01f)
                entry2 = Entry(offsetX + duration/2, offsetY)
                entry3 = Entry(offsetX+duration, 0.01f)
                entryb = Entry(offsetX+duration, 0.0f)

                if (it.intensityLevel == IntensityLevel.MildLevel) {
                    entriesLevel1.add(entrya)
                    entriesLevel1.add(entry1)
                    entriesLevel1.add(entry2)
                    entriesLevel1.add(entry3)
                    entriesLevel1.add(entryb)

                } else if (it.intensityLevel == IntensityLevel.ModerateLevel) {
                    entriesLevel2.add(entrya)
                    entriesLevel2.add(entry1)
                    entriesLevel2.add(entry2)
                    entriesLevel2.add(entry3)
                    entriesLevel2.add(entryb)
                } else {
                    entriesLevel3.add(entrya)
                    entriesLevel3.add(entry1)
                    entriesLevel3.add(entry2)
                    entriesLevel3.add(entry3)
                    entriesLevel3.add(entryb)
                }
            }

            val ds1 = LineDataSet(entriesLevel1, "Mild")
            val ds2 = LineDataSet(entriesLevel2, "Moderate")
            val ds3 = LineDataSet(entriesLevel3, "Severe")
            ds1.lineWidth = 1f
            ds2.lineWidth = 1f
            ds3.lineWidth = 1f

            ds1.setDrawCircles(false)
            ds2.setDrawCircles(false)
            ds3.setDrawCircles(false)

            ds1.color = Color.BLUE
            ds2.color = Color.rgb(255,165,0)
            ds3.color = Color.RED

            // load DataSets from files in assets folder
            sets.add(ds1)
            sets.add(ds2)
            sets.add(ds3)

            chart.data = LineData(sets)
            chart.invalidate()
        })

    }

    override fun getGroup(listPosition: Int): Any {
        return this.titleList[listPosition]
    }

    override fun getGroupCount(): Int {
        return this.titleList.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(listPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val listTitle = getGroup(listPosition) as String
        if (convertView == null) {
            //val layoutInflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layoutInflater = LayoutInflater.from(parent.context)
            convertView = layoutInflater.inflate(R.layout.summary_list_group, null)
        }
        val listTitleTextView = convertView!!.findViewById<TextView>(R.id.listTitle)
        listTitleTextView.setTypeface(null, Typeface.BOLD)
        listTitleTextView.text = listTitle
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }

    private fun generateCenterSpannableText(): SpannableString? {
        val s = SpannableString("HHContractionTimer\n developed by Hung Huong")
        s.setSpan(RelativeSizeSpan(.7f), 0, 18, 0)
        s.setSpan(StyleSpan(Typeface.NORMAL), 18, s.length - 10, 0)
        s.setSpan(ForegroundColorSpan(Color.GRAY), 18, s.length - 10, 0)

        s.setSpan(RelativeSizeSpan(.7f), 18, s.length - 10, 0)
        s.setSpan(StyleSpan(Typeface.ITALIC), s.length - 10, s.length, 0)
        s.setSpan(ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length - 10, s.length, 0)

        return s
    }
}
