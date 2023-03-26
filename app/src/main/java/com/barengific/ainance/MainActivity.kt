package com.barengific.ainance

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.DialogInterface
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.navigation.ui.AppBarConfiguration
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
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    val entries: ArrayList<PieEntry> = ArrayList()

    private var pieChart: PieChart? = null

    companion object {
        var pos: Int = 0
        lateinit var recyclerView: RecyclerView
        var posis: MutableList<Int> = mutableListOf(-1)
        var authStatus = false
        private var instance: MainActivity? = null
        fun getPosi(): Int = pos
        fun setPosi(pos: Int) {
            this.pos = pos
        }
        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        hideSystemBars()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ////////////////////////////////////////////////////////////////////////////////////////////
        //UI item size setup
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
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
        val arrr = expenseDao.getAll()
        val adapters = RvAdapter(arrr)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = adapters
        recyclerView.layoutManager = LinearLayoutManager(this)

        runOnUiThread {
            adapter.notifyDataSetChanged()
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        //pieChart
        pieChart = binding.pieCharter
        setupPieChart(getSumRange(arrr));
        loadPieChartData(getSumByCategory(arrr));

        ////////////////////////////////////////////////////////////////////////////////////////////
        //Add expense
        binding.btnAdd.setOnClickListener {
            val aa = Expense(
                0,
                binding.etName.editText?.text.toString(),
                binding.etPrice.editText?.text.toString(),
                binding.actCategory.text.toString(),
                binding.btnDate.text.toString()
            )
            expenseDao.insertAll(aa)

            val arrr = expenseDao.getAll()
            val adapter = RvAdapter(arrr)
            recyclerView.setHasFixedSize(false)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this)

            runOnUiThread {
                adapter.notifyDataSetChanged()
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        //FloatingActionButton
        val fab: View = binding.fab
        fab.setOnClickListener { view ->

            if(!binding.fab2.isVisible){
                binding.fab2.visibility = View.VISIBLE
                binding.fab3.visibility = View.VISIBLE
                binding.fab4.visibility = View.VISIBLE
                binding.tvCat.visibility = View.VISIBLE
                binding.tvLrange.visibility = View.VISIBLE
                binding.tvSrange.visibility = View.VISIBLE
            }else{
                binding.fab2.visibility = View.INVISIBLE
                binding.fab3.visibility = View.INVISIBLE
                binding.fab4.visibility = View.INVISIBLE
                binding.tvCat.visibility = View.INVISIBLE
                binding.tvLrange.visibility = View.INVISIBLE
                binding.tvSrange.visibility = View.INVISIBLE
            }
        }

        binding.fab2.setOnClickListener {
            onCreateCategoryDialog();
        }

        binding.fab3.setOnClickListener {
            onCreateSumDialog();
        }

        binding.fab4.setOnClickListener {
            onCreateRangeDialog();
        }

    }

    fun onCreateCategoryDialog(): Dialog? {
        val room = Room.databaseBuilder(applicationContext,
            AppDatabase::class.java,
            "database-names")
            .allowMainThreadQueries()
            .build()
        val categoryDao = room.categoryDao()

        return this?.let { it ->
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = this.layoutInflater;
            val view: View = inflater.inflate(R.layout.dialog_category, null)

            val recyclerViews = view.findViewById<RecyclerView>(R.id.rv_cate) as RecyclerView

            val arrr = categoryDao.getAll()
            val adapters = RvCateAdapter(arrr)
            recyclerViews.setHasFixedSize(false)
            recyclerViews.adapter = adapters
            recyclerViews.layoutManager = LinearLayoutManager(this)

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because it's going in the dialog layout
            builder.setView(view)
                // Add action buttons
                .setPositiveButton("add",
                    DialogInterface.OnClickListener { dialog, id ->

                        val name = view.findViewById<TextInputLayout>(R.id.cateName) as TextInputLayout
                        val switchCate = view.findViewById<Switch>(R.id.switchCate)
                        val switchValue = switchCate.isChecked
                        val aa = Category(
                            0,
                            name.editText?.text.toString(),
                            switchValue,
                        )
                        categoryDao.insertAll(aa)


                        val cats = categoryDao.getAll()
                        val names = cats.map { it.name }
                        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
                        binding.actCategory.setAdapter(adapter)

                    })
                .setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->

                    })

            // set adapter before calling show method
            recyclerViews.adapter = adapters

            builder.create().apply {
                show()
            }

        } ?: throw IllegalStateException("Activity cannot be null")

        val cats = categoryDao.getAll()
        val names = cats.map { it.name }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
        binding.actCategory.setAdapter(adapter)
    }

    fun onCreateSumDialog(): Dialog? {
        val room = Room.databaseBuilder(applicationContext,
            AppDatabase::class.java,
            "database-names")
            .allowMainThreadQueries()
            .build()
        val categoryDao = room.categoryDao()

        return this?.let { it ->
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = this.layoutInflater;
            val view: View = inflater.inflate(R.layout.dialog_sum_options, null)

            var selectedOption = ""
            val radioGroup = view.findViewById<RadioGroup>(R.id.rg_sum)
            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                val radioButton = group.findViewById<RadioButton>(checkedId)
                selectedOption = radioButton.text as String
                Toast.makeText(this, "You selected $selectedOption", Toast.LENGTH_LONG).show()
            }


            // Inflate and set the layout for the dialog
            // Pass null as the parent view because it's going in the dialog layout
            builder.setView(view)
                // Add action buttons
                .setPositiveButton("add",
                    DialogInterface.OnClickListener { dialog, id ->

//                        val radioChosen = view.findViewById<RadioGroup>(R.id.rg_sum)
//                        val radioText = radioChosen.checkedRadioButtonId;
//
//                        pieChartText(selectedOption)



//                        val name = view.findViewById<TextInputLayout>(R.id.cateName) as TextInputLayout
//                        val switchCate = view.findViewById<Switch>(R.id.switchCate)
//                        val switchValue = switchCate.isChecked
//                        val aa = Category(
//                            0,
//                            name.editText?.text.toString(),
//                            switchValue,
//                        )
//                        categoryDao.insertAll(aa)


                        val cats = categoryDao.getAll()
                        val names = cats.map { it.name }
                        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
                        binding.actCategory.setAdapter(adapter)

                    })
                .setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->

                    })

            builder.create().apply {
                show()
            }

        } ?: throw IllegalStateException("Activity cannot be null")

    }

    fun onCreateRangeDialog(): Dialog? {
        return this?.let { it ->
            val builder = AlertDialog.Builder(it)
            val inflater = this.layoutInflater;
            val view: View = inflater.inflate(R.layout.dialog_range_options, null)

            //////////
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
            ///////////////////////////////////////////
            //////////
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
            ///////////////////////////////////////////

            builder.setView(view)
                .setPositiveButton("Set"
                ) { _, _ ->

                    val room = Room.databaseBuilder(applicationContext,
                        AppDatabase::class.java,
                        "database-names")
                        .allowMainThreadQueries()
                        .build()
                    val expenseDao = room.expenseDao()

                    val dateSpecified = expenseDao.getExpensesInDateRange(
                        mShowSelectedDateTextF.text as String,
                        mShowSelectedDateTextT.text as String
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

    fun dateRangeHandler(expense: List<Expense>){
        val adapters = RvAdapter(expense)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = adapters
        recyclerView.layoutManager = LinearLayoutManager(this)
        runOnUiThread {
            adapters.notifyDataSetChanged()
        }

        setupPieChart(getSumRange(expense));
        loadPieChartData(getSumByCategory(expense));

    }

    private fun getSumRange(expense: List<Expense>): Double {
        val withdraws = expense.map { it.withdraw }
        return withdraws.sumByDouble { it?.toDoubleOrNull() ?: 0.0 }
    }

    private fun getSumByCategory(quantities: List<Expense>): Map<String, Double> {
        val map = mutableMapOf<String, Double>()
        for (quantity in quantities) {
            val category = quantity.category ?: continue
            val value = quantity.withdraw?.toIntOrNull() ?: continue
            map[category] = (map[category] ?: 0.0) + value
        }
        Log.v("aaaaaaaaaaaaCATESUMM", map.toString())
        return map
    }

    private fun setupPieChart(sum: Double) {
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

    private fun loadPieChartData(expense: Map<String, Double>) {

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
        val dataSet = PieDataSet(entries, "ategory")
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

    private fun hideSystemBars() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
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
                            ClipData.newPlainText("aaaa", viewHolder.textView4.text.toString())
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

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
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
                        Log.d("aaa menu", "copy")
                        val clipboard =
                            view?.context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        val clip: ClipData =
                            ClipData.newPlainText("aaaa", viewHolder.textView2.text.toString())
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(view.context, "Text Copied", Toast.LENGTH_LONG).show()

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

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView1.text = dataSet[position].id.toString()
        viewHolder.textView2.text = dataSet[position].name.toString()
        viewHolder.textView3.text = dataSet[position].type.toString()
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

