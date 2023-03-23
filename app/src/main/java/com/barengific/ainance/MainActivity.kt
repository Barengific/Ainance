package com.barengific.ainance

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.barengific.ainance.databinding.ActivityMainBinding
import com.barengific.ainance.obj.Expense
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.datepicker.MaterialDatePicker


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


        //DB impl
        val room = Room.databaseBuilder(applicationContext,
            AppDatabase::class.java,
            "database-names")
            .allowMainThreadQueries()
            .build()
        val expenseDao = room.expenseDao()




        ////////////////
        val mPickDateButton = binding.btnDate
        val mShowSelectedDateText = binding.btnDate

        val materialDateBuilder: MaterialDatePicker.Builder<*> =
            MaterialDatePicker.Builder.datePicker()

        materialDateBuilder.setTitleText("SELECT A DATE")

        val materialDatePicker = materialDateBuilder.build()


        mPickDateButton.setOnClickListener { // getSupportFragmentManager() to
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
        }

        materialDatePicker.addOnPositiveButtonClickListener {
            mShowSelectedDateText.text = materialDatePicker.headerText
        }

        ///////////////


        val items = arrayOf("Item 1", "Item 2", "Item 3")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line ,items)
        binding.actCategory.setAdapter(adapter)
        

        pieChart = binding.pieCharter
        setupPieChart();
        loadPieChartData();

        recyclerView = binding.rvExpense

        val arrr = expenseDao.getAll()
        val adapters = RvAdapter(arrr)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = adapters
        recyclerView.layoutManager = LinearLayoutManager(this)

        runOnUiThread {
            adapter.notifyDataSetChanged()
        }

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
    }

    private fun setupPieChart() {
        pieChart!!.isDrawHoleEnabled = true
        pieChart!!.setUsePercentValues(true)
        pieChart!!.setEntryLabelTextSize(14f)
        pieChart!!.setEntryLabelColor(Color.BLACK)
        pieChart!!.centerText = "Category"
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

    private fun loadPieChartData() {
        entries.add(PieEntry(0.2f, "Food & Dining"))
        entries.add(PieEntry(0.15f, "Medical"))
        entries.add(PieEntry(0.10f, "Entertainment"))
        entries.add(PieEntry(0.25f, "Electricity and Gas"))
        entries.add(PieEntry(0.3f, "Housing"))


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

