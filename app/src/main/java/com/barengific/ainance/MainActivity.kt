package com.barengific.ainance

import android.R
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.ui.AppBarConfiguration
import com.barengific.ainance.databinding.ActivityMainBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var pieChart: PieChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        binding.etName.maxWidth = (width/2)
        binding.etPrice.maxWidth = (width/2)
        binding.etCategory.maxWidth = (width/2)
        binding.etDate.maxWidth = (width/2)

        binding.etName.minWidth = (width/2)
        binding.etPrice.minWidth = (width/2)
        binding.etCategory.minWidth = (width/2)
        binding.etDate.minWidth = (width/2)


        val items = arrayOf("Item 1", "Item 2", "Item 3")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line ,items)
        binding.actCategory.setAdapter(adapter)

        //
//
//        val Subjects = arrayOf("Android", "Flutter", "React Native")
//        val adapter1 = ArrayAdapter<String>(this, R.layout.simple_spinner_dropdown_item, Subjects)
//        binding.actCategory.setAdapter(adapter1)

        val Subjects = arrayOf("Android", "Flutter", "React Native")
        val adapter1 = ArrayAdapter<String>(this, R.layout.simple_spinner_dropdown_item, Subjects)
        binding.spinner.adapter = adapter1


        pieChart = binding.pieCharter
        setupPieChart();
        loadPieChartData();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    private fun setupPieChart() {
        pieChart!!.isDrawHoleEnabled = true
        pieChart!!.setUsePercentValues(true)
        pieChart!!.setEntryLabelTextSize(14f)
        pieChart!!.setEntryLabelColor(Color.BLACK)
        pieChart!!.centerText = "Category"
        pieChart!!.setCenterTextSize(24f)
        pieChart!!.description.isEnabled = false
        val l = pieChart!!.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.textSize = 16f
        l.setDrawInside(false)
        l.isEnabled = false

    }

    private fun loadPieChartData() {
        val entries: ArrayList<PieEntry> = ArrayList()
        entries.add(PieEntry(0.2f, "Food & Dining"))
        entries.add(PieEntry(0.15f, "Medical"))
        entries.add(PieEntry(0.10f, "Entertainment"))
        entries.add(PieEntry(0.25f, "Electricity and Gas"))
        entries.add(PieEntry(0.3f, "Housing"))
        entries.add(PieEntry(0.37f, "Cars"))
        entries.add(PieEntry(0.40f, "School"))
        val colors: ArrayList<Int> = ArrayList()
        for (color in ColorTemplate.MATERIAL_COLORS) {
            colors.add(color)
        }
        for (color in ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color)
        }
        val dataSet = PieDataSet(entries, "Expense Category")
        dataSet.colors = colors
        val data = PieData(dataSet)
        data.setDrawValues(true)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(12f)
        data.setValueTextColor(Color.BLACK)

        pieChart!!.data = data
        pieChart!!.invalidate()
        pieChart!!.animateY(1400, Easing.EaseInOutQuad)
    }
}

