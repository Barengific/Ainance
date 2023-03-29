package com.barengific.ainance

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.barengific.ainance.databinding.ActivityMainBinding
import com.barengific.ainance.obj.Category
import com.barengific.ainance.obj.Expense
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout

import java.time.Instant
import java.util.*
import java.text.SimpleDateFormat

import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        lateinit var recyclerView: RecyclerView
        private var instance: MainActivity? = null
        val entries: ArrayList<PieEntry> = ArrayList()

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }

        var pieChart: PieChart? = null
        fun setupPieChart(sum: Double) {
            pieChart!!.isDrawHoleEnabled = true
            pieChart!!.setUsePercentValues(true)
            pieChart!!.setEntryLabelTextSize(14f)
            pieChart!!.setEntryLabelColor(Color.BLACK)
            pieChart!!.centerText = "Total: $sum"
            pieChart!!.setCenterTextSize(24f)
            pieChart!!.description.isEnabled = false
            val l = pieChart!!.legend
//        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
//        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
//        l.orientation = Legend.LegendOrientation.VERTICAL
//        l.textSize = 16f
//        l.setDrawInside(false)
            l.isEnabled = false

        }

        fun loadPieChartData(expense: Map<String, Double>) {
            entries.clear()
            expense.forEach { entry ->
                entries.add(PieEntry(entry.value.toFloat(),entry.key))
            }

            val colors: ArrayList<Int> = ArrayList()
            for (color in ColorTemplate.MATERIAL_COLORS) {
                colors.add(color)
            }
            for (color in ColorTemplate.VORDIPLOM_COLORS) {
                colors.add(color)
            }
            val dataSet = PieDataSet(entries, "category")
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

        fun getSumRange(expense: List<Expense>): Double {
            val withdraws = expense.map { it.withdraw }
            return withdraws.sumOf { it?.toDoubleOrNull() ?: 0.0 }
        }

        fun getSumByCategory(quantities: List<Expense>): Map<String, Double> {
            val map = mutableMapOf<String, Double>()
            for (quantity in quantities) {
                val category = quantity.category ?: continue
                val value = quantity.withdraw?.toIntOrNull() ?: continue
                map[category] = (map[category] ?: 0.0) + value
            }
            return map
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        instance = this

        hideSystemBars()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ////////////////////////////////////////////////////////////////////////////////////////////
        //Current Date
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        val currentDate = Date()
        val currentDateFormatted = formatter.format(currentDate)

        val date = formatter.parse(currentDateFormatted)
        val currentDateMilliseconds = date?.time ?: 0

        ////////////////////////////////////////////////////////////////////////////////////////////
        //UI item size setup
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

        binding.etName.maxWidth = (width/2)
        binding.etPrice.maxWidth = (width/2)
        binding.etCategory.maxWidth = (width/2)
        binding.btnDate.maxWidth = (width/2)

        binding.etName.minWidth = (width/2)
        binding.etPrice.minWidth = (width/2)
        binding.etCategory.minWidth = (width/2)
        binding.btnDate.minWidth = (width/2)

        ////////////////////////////////////////////////////////////////////////////////////////////
        //shared preference for default view
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDates = sdf.format(Date())
        val calendar = Calendar.getInstance()
        calendar.time = Date()

        var pastDate = ""
        val sharedPreferences = getSharedPreferences("default_view_prefs", Context.MODE_PRIVATE)

        val value = sharedPreferences.getString("default_view", "")

        if (sharedPreferences.contains("default_view")) {

            when (value.toString()) {
                "Daily" -> {
                    pastDate = currentDateFormatted
                }
                "Weekly (7 days)" -> {
                    calendar.add(Calendar.DAY_OF_MONTH, -7)
                    pastDate = sdf.format(calendar.time)
                }
                "Weekly (Start of week)" -> {
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, -1)
                    val tempDate = calendar.time
                    pastDate = sdf.format(tempDate)
                }
                "Monthly (30 days)" -> {
                    calendar.add(Calendar.DAY_OF_MONTH, -30)
                    pastDate = sdf.format(calendar.time)
                }
                "Monthly (Start month)" -> {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    val tempDate = calendar.time
                    pastDate = sdf.format(tempDate)
                }
                "Yearly (365 days)" -> {
                    calendar.add(Calendar.DAY_OF_MONTH, -365)
                    pastDate = sdf.format(calendar.time)
                }
                "Yearly (This year)" -> {
                    calendar.set(Calendar.MONTH, Calendar.JANUARY)
                    calendar.set(Calendar.DAY_OF_YEAR, 1)
                    val tempDate = calendar.time
                    pastDate = sdf.format(tempDate)
                }
            }

        } else {
            val editor = sharedPreferences.edit()
            editor.putString("default_view", "Monthly (Start month)")
            editor.apply()

            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val tempDate = calendar.time
            pastDate = sdf.format(tempDate)

        }

        val datePast = formatter.parse(pastDate)
        val pastDateMilliseconds = datePast?.time ?: 0

        ////////////////////////////////////////////////////////////////////////////////////////////
        //DB impl
        val room = Room.databaseBuilder(applicationContext,
            AppDatabase::class.java,
            "database-names")
            .allowMainThreadQueries()
            .build()
        val expenseDao = room.expenseDao()
        val categoryDao = room.categoryDao()

        ////////////////////////////////////////////////////////////////////////////////////////////
        //Date button
        val mPickDateButton = binding.btnDate
        val mShowSelectedDateText = binding.btnDate

        val materialDateBuilder: MaterialDatePicker.Builder<*> =
            MaterialDatePicker.Builder.datePicker()

        materialDateBuilder.setTitleText("SELECT A DATE")

        val materialDatePicker = materialDateBuilder.build()

        mPickDateButton.setOnClickListener {
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
        }

        materialDatePicker.addOnPositiveButtonClickListener { selectedDateTimestamp ->
            val instant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Instant.ofEpochMilli(selectedDateTimestamp as Long)
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            val selectedDate = Date.from(instant)
            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val formattedDate = simpleDateFormat.format(selectedDate)

            mShowSelectedDateText.text = formattedDate
            Toast.makeText(this, formattedDate, Toast.LENGTH_LONG).show()
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        //Category setup
        val cats = categoryDao.getAll()
        val names = cats.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
        binding.actCategory.setAdapter(adapter)

        ////////////////////////////////////////////////////////////////////////////////////////////
        //Recyclerview expenses
        recyclerView = binding.rvExpense
        pieChart = binding.pieCharter
        val arrr = expenseDao.getExpensesInDateRange(pastDateMilliseconds.toString(),
                                                     currentDateMilliseconds.toString())



        Log.v("aaaaaaaaaaaaQQQQQQQQQQQq", pastDateMilliseconds.toString())
        Log.v("aaaaaaaaaaaaQQQQQQQQQQQq", currentDateMilliseconds.toString())

        dateRangeHandler(arrr)
//        for (item in arrr) {
//            val timestamp = item.date?.toLong()
//            val date = Date(timestamp!!)
//            val formattedDate = formatter.format(date)
//            item.date = formattedDate
//        }
//
//        val adapters = RvAdapter(arrr)
//        recyclerView.setHasFixedSize(false)
//        recyclerView.adapter = adapters
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        runOnUiThread {
//            adapter.notifyDataSetChanged()
//        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        //pieChart
//        pieChart = binding.pieCharter
//        setupPieChart(getSumRange(arrr))
//        loadPieChartData(getSumByCategory(arrr))

        ////////////////////////////////////////////////////////////////////////////////////////////
        //Add expense
        binding.btnAdd.setOnClickListener {
            if (!binding.btnDate.text.toString().isNullOrEmpty() &&
                !binding.etName.editText?.text.toString().isNullOrEmpty() &&
                !binding.etPrice.editText?.text.toString().isNullOrEmpty() &&
                !binding.actCategory.text.toString().isNullOrEmpty()){

                val date = formatter.parse(binding.btnDate.text.toString())
                val dateMilliseconds = date?.time ?: 0

                val aa = Expense(
                    0,
                    binding.etName.editText?.text.toString(),
                    binding.etPrice.editText?.text.toString(),
                    binding.actCategory.text.toString(),
                    dateMilliseconds.toString()
                )
                expenseDao.insertAll(aa)

                val arrr = expenseDao.getAll()
                dateRangeHandler(arrr)

            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        //FloatingActionButton
        val fab: View = binding.fab
        fab.setOnClickListener {

            if(!binding.fab2.isVisible){
                binding.fab2.visibility = View.VISIBLE
                binding.fab3.visibility = View.VISIBLE
                binding.fab4.visibility = View.VISIBLE
                binding.fab5.visibility = View.VISIBLE
                binding.tvCat.visibility = View.VISIBLE
                binding.tvLrange.visibility = View.VISIBLE
                binding.tvLrange2.visibility = View.VISIBLE
                binding.tvSrange.visibility = View.VISIBLE
            }else{
                binding.fab2.visibility = View.INVISIBLE
                binding.fab3.visibility = View.INVISIBLE
                binding.fab4.visibility = View.INVISIBLE
                binding.fab5.visibility = View.INVISIBLE
                binding.tvCat.visibility = View.INVISIBLE
                binding.tvLrange.visibility = View.INVISIBLE
                binding.tvLrange2.visibility = View.INVISIBLE
                binding.tvSrange.visibility = View.INVISIBLE
            }
        }

        binding.fab2.setOnClickListener {
            onCreateCategoryDialog()
        }

        binding.fab3.setOnClickListener {
            onCreateCategorySearchDialog()
        }

        binding.fab4.setOnClickListener {
            onCreateSumDialog()
        }
        binding.fab5.setOnClickListener {
            onCreateRangeDialog()
        }

        val contentView = binding.root
        contentView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            contentView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = contentView.rootView.height
            val keyboardHeight = screenHeight - rect.bottom

            if (keyboardHeight > screenHeight * 0.15) {
                // Soft keyboard is visible, hide the fab
                binding.fab.visibility = View.INVISIBLE
                binding.fab2.visibility = View.INVISIBLE
                binding.fab3.visibility = View.INVISIBLE
                binding.fab4.visibility = View.INVISIBLE
                binding.fab5.visibility = View.INVISIBLE
                binding.tvCat.visibility = View.INVISIBLE
                binding.tvLrange.visibility = View.INVISIBLE
                binding.tvLrange2.visibility = View.INVISIBLE
                binding.tvSrange.visibility = View.INVISIBLE
            }else {
                // Soft keyboard is not visible, show the fab
                binding.fab.visibility = View.VISIBLE
            }
        }

    }

    private fun onCreateCategoryDialog(): Dialog {
        val room = Room.databaseBuilder(applicationContext,
            AppDatabase::class.java,
            "database-names")
            .allowMainThreadQueries()
            .build()
        val categoryDao = room.categoryDao()

        return this.let { it ->
            val builder = AlertDialog.Builder(it)

            val inflater = this.layoutInflater
            val view: View = inflater.inflate(R.layout.dialog_category, null)

            val recyclerViews = view.findViewById(R.id.rv_cate) as RecyclerView

            val arrr = categoryDao.getAll()
            val adapters = RvCateAdapter(arrr)
            recyclerViews.setHasFixedSize(false)
            recyclerViews.adapter = adapters
            recyclerViews.layoutManager = LinearLayoutManager(this)

            builder.setView(view)
                // Add action buttons
                .setPositiveButton("add"
                ) { _, _ ->

                    val name = view.findViewById(R.id.cateName) as TextInputLayout

                    if(!name.editText?.text.toString().isNullOrEmpty()){
                        val aa = Category(
                            0,
                            name.editText?.text.toString(),
                            false,
                        )
                        categoryDao.insertAll(aa)

                        val cats = categoryDao.getAll()
                        val names = cats.map { it.name }
                        val adapter =
                            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
                        binding.actCategory.setAdapter(adapter)
                    }
                }
                .setNegativeButton(R.string.cancel
                ) { _, _ ->

                }

            // set adapter before calling show method
            recyclerViews.adapter = adapters

            builder.create().apply {
                show()
            }

        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun onCreateCategorySearchDialog(): Dialog {
        return this.let { it ->
            val builder = AlertDialog.Builder(it)

            val inflater = this.layoutInflater
            val view: View = inflater.inflate(R.layout.dialog_category_search, null)

            val room = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "database-names"
            )
                .allowMainThreadQueries()
                .build()
            val categoryDao = room.categoryDao()

            val cats = categoryDao.getAll()
            val names = cats.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
            view.findViewById<AutoCompleteTextView>(R.id.actCategorySearch).setAdapter(adapter)

            builder.setView(view)
                // Add action buttons
                .setPositiveButton("Search"
                ) { _, _ ->
                    val expenseDao = room.expenseDao()

                    val matchDate = view.findViewById<Switch>(R.id.switchCate).isChecked

                    var catSearch: List<Expense> = emptyList()
                    if (!matchDate){
                        catSearch = expenseDao.findByCategory(
                            view.findViewById<AutoCompleteTextView>
                                (R.id.actCategorySearch).text.toString())
                    }else if(matchDate){
                        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        val currentDate = sdf.format(Date())
                        val calendar = Calendar.getInstance()
                        calendar.time = Date()

                        var pastDate = ""
                        val sharedPreferences = getSharedPreferences("default_view_prefs", Context.MODE_PRIVATE)
                        val value = sharedPreferences.getString("default_view", "")

                        if (sharedPreferences.contains("default_view")) {

                            when (value.toString()) {
                                "Daily" -> {
                                    pastDate = currentDate
                                }
                                "Weekly (7 days)" -> {
                                    calendar.add(Calendar.DAY_OF_MONTH, -7)
                                    pastDate = sdf.format(calendar.time)
                                }
                                "Weekly (Start of week)" -> {
                                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                                    calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, -1)
                                    val tempDate = calendar.time
                                    pastDate = sdf.format(tempDate)
                                }
                                "Monthly (30 days)" -> {
                                    calendar.add(Calendar.DAY_OF_MONTH, -30)
                                    pastDate = sdf.format(calendar.time)
                                }
                                "Monthly (Start month)" -> {
                                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                                    val tempDate = calendar.time
                                    pastDate = sdf.format(tempDate)
                                }
                                "Yearly (365 days)" -> {
                                    calendar.add(Calendar.DAY_OF_MONTH, -365)
                                    pastDate = sdf.format(calendar.time)
                                }
                                "Yearly (This year)" -> {
                                    calendar.set(Calendar.MONTH, Calendar.JANUARY)
                                    calendar.set(Calendar.DAY_OF_YEAR, 1)
                                    val tempDate = calendar.time
                                    pastDate = sdf.format(tempDate)
                                }
                            }

                        } else {
                            val editor = sharedPreferences.edit()
                            editor.putString("default_view", "Monthly (Start month)")
                            editor.apply()

                            calendar.set(Calendar.DAY_OF_MONTH, 1)
                            val tempDate = calendar.time
                            pastDate = sdf.format(tempDate)

                        }

                        val datePast = sdf.parse(pastDate)
                        val pastDateMilliseconds = datePast?.time ?: 0

                        val date = sdf.parse(currentDate)
                        val currentDateMilliseconds = date?.time ?: 0

                        val arrr = expenseDao.getExpensesInDateRange(pastDateMilliseconds.toString(),
                            currentDateMilliseconds.toString())

                        catSearch = arrr.filter { it.category ==
                                view.findViewById<AutoCompleteTextView>(R.id.actCategorySearch).text.toString()}

                    }


                    if (!view.findViewById<AutoCompleteTextView>
                            (R.id.actCategorySearch).text.toString().isNullOrEmpty()){
                        dateRangeHandler(catSearch)
                    }


                }
                .setNegativeButton(R.string.cancel
                ) { _, _ ->

                }

            builder.create().apply {
                show()
            }

        } ?: throw IllegalStateException("Activity cannot be null")

    }

    private fun onCreateSumDialog(): Dialog {
        return this.let {
            val builder = AlertDialog.Builder(it)

            val inflater = this.layoutInflater
            val view: View = inflater.inflate(R.layout.dialog_sum_options, null)

            var selectedOption = ""
            val radioGroup = view.findViewById<RadioGroup>(R.id.rg_sum)
            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                val radioButton = group.findViewById<RadioButton>(checkedId)
                selectedOption = radioButton.text as String

            }

            val sharedPreferences = getSharedPreferences("default_view_prefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            builder.setView(view)
                // Add action buttons
                .setPositiveButton("Search"
                ) { _, _ ->

                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val currentDate = sdf.format(Date())
                    val calendar = Calendar.getInstance()
                    calendar.time = Date()

                    var pastDate = ""

                    when (selectedOption) {
                        "Daily" -> {
                            pastDate = currentDate
                            editor.putString("default_view", "Daily")
                            editor.apply()

                        }
                        "Weekly (7 days)" -> {
                            calendar.add(Calendar.DAY_OF_MONTH, -7)
                            pastDate = sdf.format(calendar.time)
                            editor.putString("default_view", "Weekly (7 days)")
                            editor.apply()

                        }
                        "Weekly (Start of week)" -> {
                            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                            calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, -1)
                            val tempDate = calendar.time
                            pastDate = sdf.format(tempDate)
                            editor.putString("default_view", "Weekly (Start of week)")
                            editor.apply()

                        }
                        "Monthly (30 days)" -> {
                            calendar.add(Calendar.DAY_OF_MONTH, -30)
                            pastDate = sdf.format(calendar.time)
                            editor.putString("default_view", "Monthly (30 days)")
                            editor.apply()

                        }
                        "Monthly (Start month)" -> {
                            calendar.set(Calendar.DAY_OF_MONTH, 1)
                            val tempDate = calendar.time
                            pastDate = sdf.format(tempDate)
                            editor.putString("default_view", "Monthly (Start month)")
                            editor.apply()

                        }
                        "Yearly (365 days)" -> {
                            calendar.add(Calendar.DAY_OF_MONTH, -365)
                            pastDate = sdf.format(calendar.time)
                            editor.putString("default_view", "Yearly (365 days)")
                            editor.apply()

                        }
                        "Yearly (This year)" -> {
                            calendar.set(Calendar.MONTH, Calendar.JANUARY)
                            calendar.set(Calendar.DAY_OF_YEAR, 1)
                            val tempDate = calendar.time
                            pastDate = sdf.format(tempDate)
                            editor.putString("default_view", "Yearly (This year)")
                            editor.apply()

                        }
                    }

                    val room = Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        "database-names"
                    )
                        .allowMainThreadQueries()
                        .build()
                    val expenseDao = room.expenseDao()

                    val formatter = SimpleDateFormat("dd-MM-yyyy")
                    val currentDates = Date()
                    val currentDateFormatted = formatter.format(currentDates)

                    val date = formatter.parse(currentDateFormatted)
                    val currentDateMilliseconds = date?.time ?: 0

                    val datePast = formatter.parse(pastDate)
                    val pastDateMilliseconds = datePast?.time ?: 0

                    val dateSpecified =
                        expenseDao.getExpensesInDateRange(
                            pastDateMilliseconds.toString(),
                            currentDateMilliseconds.toString()
                        )

                    dateRangeHandler(dateSpecified)

                }
                .setNegativeButton(R.string.cancel
                ) { _, _ ->

                }

            builder.create().apply {
                show()
            }

        } ?: throw IllegalStateException("Activity cannot be null")

    }

    private fun onCreateRangeDialog(): Dialog {
        return this.let {
            val builder = AlertDialog.Builder(it)
            val inflater = this.layoutInflater
            val view: View = inflater.inflate(R.layout.dialog_range_options, null)

            ////////////////////////////////////////////////////////////////////////////////////////
            //To Date
            val mPickDateButtonF = view.findViewById<Button>(R.id.btnFrom)
            val mShowSelectedDateTextF = view.findViewById<Button>(R.id.btnFrom)

            val materialDateBuilderF: MaterialDatePicker.Builder<*> =
                MaterialDatePicker.Builder.datePicker()

            materialDateBuilderF.setTitleText("SELECT A FROM DATE")

            val materialDatePickerF = materialDateBuilderF.build()

            mPickDateButtonF.setOnClickListener {
                materialDatePickerF.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
            }

            materialDatePickerF.addOnPositiveButtonClickListener { selectedDateTimestamp ->
                val instant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Instant.ofEpochMilli(selectedDateTimestamp as Long)
                } else {
                    TODO("VERSION.SDK_INT < O")
                }
                val selectedDate = Date.from(instant)
                val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formattedDate = simpleDateFormat.format(selectedDate)

                mShowSelectedDateTextF.text = formattedDate
                Toast.makeText(this, formattedDate, Toast.LENGTH_LONG).show()
            }
            ////////////////////////////////////////////////////////////////////////////////////////
            //From Date
            val mPickDateButtonT = view.findViewById<Button>(R.id.btnTo)
            val mShowSelectedDateTextT = view.findViewById<Button>(R.id.btnTo)

            val materialDateBuilderT: MaterialDatePicker.Builder<*> =
                MaterialDatePicker.Builder.datePicker()

            materialDateBuilderT.setTitleText("SELECT A TO DATE")

            val materialDatePickerT = materialDateBuilderT.build()

            mPickDateButtonT.setOnClickListener {
                materialDatePickerT.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
            }

            materialDatePickerT.addOnPositiveButtonClickListener { selectedDateTimestamp ->
                val instant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Instant.ofEpochMilli(selectedDateTimestamp as Long)
                } else {
                    TODO("VERSION.SDK_INT < O")
                }
                val selectedDate = Date.from(instant)
                val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formattedDate = simpleDateFormat.format(selectedDate)

                mShowSelectedDateTextT.text = formattedDate
                Toast.makeText(this, formattedDate, Toast.LENGTH_LONG).show()
            }
            ////////////////////////////////////////////////////////////////////////////////////////

            builder.setView(view)
                .setPositiveButton("Set"
                ) { _, _ ->

                    val room = Room.databaseBuilder(applicationContext,
                        AppDatabase::class.java,
                        "database-names")
                        .allowMainThreadQueries()
                        .build()
                    val expenseDao = room.expenseDao()

                    val formatter = SimpleDateFormat("dd-MM-yyyy")

                    val date = formatter.parse(mShowSelectedDateTextT.text.toString())
                    val toDateMilliseconds = date?.time ?: 0

                    val datePast = formatter.parse(mShowSelectedDateTextF.text.toString())
                    val fromDateMilliseconds = datePast?.time ?: 0

                    val dateSpecified =
                        expenseDao.getExpensesInDateRange(fromDateMilliseconds.toString(),
                            toDateMilliseconds.toString(),
                        )

                    dateRangeHandler(dateSpecified)

                }
                .setNegativeButton("Cancel"){_,_ ->

                }
            builder.create().apply {
                show()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun dateRangeHandler(expense: List<Expense>){
        val formatter = SimpleDateFormat("dd-MM-yyyy")

        for (item in expense) {
            val timestamp = item.date?.toLong()
            val date = Date(timestamp!!)
            val formattedDate = formatter.format(date)
            item.date = formattedDate
        }
        val adapters = RvAdapter(expense)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = adapters
        recyclerView.layoutManager = LinearLayoutManager(this)
        runOnUiThread {
            adapters.notifyDataSetChanged()
        }

        setupPieChart(getSumRange(expense))
        loadPieChartData(getSumByCategory(expense))

    }

//    private fun getSumRange(expense: List<Expense>): Double {
//        val withdraws = expense.map { it.withdraw }
//        return withdraws.sumOf { it?.toDoubleOrNull() ?: 0.0 }
//    }
//
//    private fun getSumByCategory(quantities: List<Expense>): Map<String, Double> {
//        val map = mutableMapOf<String, Double>()
//        for (quantity in quantities) {
//            val category = quantity.category ?: continue
//            val value = quantity.withdraw?.toIntOrNull() ?: continue
//            map[category] = (map[category] ?: 0.0) + value
//        }
//        return map
//    }

//    private fun setupPieChart(sum: Double) {
//        pieChart!!.isDrawHoleEnabled = true
//        pieChart!!.setUsePercentValues(true)
//        pieChart!!.setEntryLabelTextSize(14f)
//        pieChart!!.setEntryLabelColor(Color.BLACK)
//        pieChart!!.centerText = "Total: $sum"
//        pieChart!!.setCenterTextSize(24f)
//        pieChart!!.description.isEnabled = false
//        val l = pieChart!!.legend
////        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
////        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
////        l.orientation = Legend.LegendOrientation.VERTICAL
////        l.textSize = 16f
////        l.setDrawInside(false)
//        l.isEnabled = false
//
//    }

//    private fun loadPieChartData(expense: Map<String, Double>) {
//        entries.clear()
//        expense.forEach { entry ->
//            entries.add(PieEntry(entry.value.toFloat(),entry.key))
//        }
//
//        val colors: ArrayList<Int> = ArrayList()
//        for (color in ColorTemplate.MATERIAL_COLORS) {
//            colors.add(color)
//        }
//        for (color in ColorTemplate.VORDIPLOM_COLORS) {
//            colors.add(color)
//        }
//        val dataSet = PieDataSet(entries, "category")
//        dataSet.colors = colors
//        val data = PieData(dataSet)
//        data.setDrawValues(true)
//        data.setValueFormatter(PercentFormatter(pieChart))
//        data.setValueTextSize(12f)
//        data.setValueTextColor(Color.BLACK)
//
//        pieChart!!.data = data
//        pieChart!!.invalidate()
//        pieChart!!.animateY(1400, Easing.EaseInOutQuad)
//    }

    private fun hideSystemBars() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return

        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}

class RvAdapter(private val dataSet: List<Expense>) :
    RecyclerView.Adapter<RvAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView1: TextView
        val textView2: TextView
        val textView3: TextView
        val textView4: TextView
        val textView5: TextView
        var ivMore: ImageView

        init {
            ivMore = view.findViewById(R.id.ivMore) as ImageView
            // Define click listener for the ViewHolder's View
            textView1 = view.findViewById(R.id.textView1)
            textView2 = view.findViewById(R.id.textView2)
            textView3 = view.findViewById(R.id.textView3)
            textView4 = view.findViewById(R.id.textView4)
            textView5 = view.findViewById(R.id.textView5)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rv_row, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.ivMore.setOnClickListener { view ->
            val wrapper: Context = ContextThemeWrapper(view?.context, R.style.PopupMenu)

            val popup = PopupMenu(wrapper, viewHolder.ivMore)
            //inflating menu from xml resource
            popup.inflate(R.menu.rv_menu_context)
            //adding click listener
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_copy -> {
                        Log.d("aaa menu", "copy")
                        val clipboard =
                            view?.context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        val clip: ClipData =
                            ClipData.newPlainText("a", viewHolder.textView4.text.toString())
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(view.context, "Text Copied", Toast.LENGTH_LONG).show()

                    }
                    R.id.menu_delete -> {
                        val room = view?.context?.let {
                            Room.databaseBuilder(it, AppDatabase::class.java, "database-names")
                                .allowMainThreadQueries()
                                .build()
                        }
                        val expenseDao = room?.expenseDao()

                        val description: TextView = viewHolder.textView1
                        val price: TextView = viewHolder.textView2
                        val cate: TextView = viewHolder.textView3
                        val date: TextView = viewHolder.textView4
                        val eid: TextView = viewHolder.textView5

                        val a = Expense(
                            eid.text.toString().toInt(),
                            description.text.toString(),
                            price.text.toString(),
                            cate.text.toString(),
                            date.text.toString()
                        )
                        room?.expenseDao()?.delete(a)
                        val arrr = expenseDao?.getAll()

                        val formatter = SimpleDateFormat("dd-MM-yyyy")

                        if (arrr != null) {
                            for (item in arrr) {
                                val timestamp = item.date?.toLong()
                                val date = Date(timestamp!!)
                                val formattedDate = formatter.format(date)
                                item.date = formattedDate
                            }
                        }
                        val adapters = arrr?.let { RvAdapter(it) }
                        MainActivity.recyclerView.setHasFixedSize(false)
                        MainActivity.recyclerView.adapter = adapters
                        MainActivity.recyclerView.layoutManager = LinearLayoutManager(
                            MainActivity.applicationContext())

                        adapters?.notifyDataSetChanged()

                        arrr?.let { MainActivity.getSumRange(it) }
                            ?.let { MainActivity.setupPieChart(it) }
                        arrr?.let { MainActivity.getSumByCategory(it) }
                            ?.let { MainActivity.loadPieChartData(it) }

                        val adapter = arrr?.let { RvAdapter(it) }

                        MainActivity.recyclerView.setHasFixedSize(false)
                        MainActivity.recyclerView.adapter = adapter
                        MainActivity.recyclerView.layoutManager =
                            LinearLayoutManager(view?.context)
                        room?.close()
                        Log.d("aaa menu", "DDDelete")

                    }

                    R.id.menu_cancel -> {
                        Log.d("aaa menu", "cancel") //TODO
                    }
                }
                true
            }
            //displaying the popup
            popup.show()
        }

        viewHolder.textView1.text = dataSet[position].description.toString()
        viewHolder.textView2.text = dataSet[position].withdraw.toString()
        viewHolder.textView3.text = dataSet[position].category.toString()
        viewHolder.textView4.text = dataSet[position].date.toString()
        viewHolder.textView5.text = dataSet[position].id.toString()
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size


}

class RvCateAdapter(private val dataSet: List<Category>) :
    RecyclerView.Adapter<RvCateAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView1: TextView
        val textView2: TextView
        val textView3: TextView
        var ivMore: ImageView

        init {
            ivMore = view.findViewById(R.id.ivMore) as ImageView
            textView1 = view.findViewById(R.id.textView1)
            textView2 = view.findViewById(R.id.textView2)
            textView3 = view.findViewById(R.id.textView3)
        }
    }
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rv_cate, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.ivMore.setOnClickListener { view ->
            val wrapper: Context = ContextThemeWrapper(view?.context, R.style.PopupMenu)

            val popup = PopupMenu(wrapper, viewHolder.ivMore)
            //inflating menu from xml resource
            popup.inflate(R.menu.rv_menu_context)
            //adding click listener
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_copy -> {

                    }
                    R.id.menu_delete -> {
                        val room = view?.context?.let {
                            Room.databaseBuilder(it, AppDatabase::class.java, "database-names")
                                .allowMainThreadQueries()
                                .build()
                        }
                        val categoryDao = room?.categoryDao()

                        val id: TextView = viewHolder.textView1
                        val name: TextView = viewHolder.textView2
                        val bool: TextView = viewHolder.textView3

                        val a = Category(
                            id.text.toString().toInt(),
                            name.text.toString(),
                            bool.text.toString().toBoolean()
                        )
                        room?.categoryDao()?.delete(a)
                        val arrr = categoryDao?.getAll()
                        val adapter = arrr?.let { RvCateAdapter(it) }

                        MainActivity.recyclerView.setHasFixedSize(false)
                        MainActivity.recyclerView.adapter = adapter
                        MainActivity.recyclerView.layoutManager =
                            LinearLayoutManager(view?.context)
                        room?.close()

                    }

                    R.id.menu_cancel -> {
                        Log.d("aaa menu", "cancel") //TODO
                    }
                }
                true
            }
            //displaying the popup
            popup.show()
        }

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView1.text = dataSet[position].id.toString()
        viewHolder.textView2.text = dataSet[position].name.toString()
        viewHolder.textView3.text = dataSet[position].type.toString()
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
