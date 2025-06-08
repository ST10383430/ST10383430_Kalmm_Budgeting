package vcmsa.projects.kalmmbudgeting2

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcmsa.projects.kalmmbudgeting2.R
import vcmsa.projects.kalmmbudgeting2.data.AppDatabase
import vcmsa.projects.kalmmbudgeting2.data.BudgetEntry
import vcmsa.projects.kalmmbudgeting2.databinding.ActivityAnnualOverviewBinding
import java.text.SimpleDateFormat
import java.util.*

class AnnualOverviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnnualOverviewBinding
    private val db by lazy { AppDatabase.getInstance(this) }
    private lateinit var adapter: MonthlyExpenseAdapter
    private var allEntries = listOf<BudgetEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnnualOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        setupRecyclerView()
        setupCategorySpinner()
        loadData("All")
        setupBottomNav()
    }

    private fun setupRecyclerView() {
        adapter = MonthlyExpenseAdapter(emptyList())
        binding.monthlyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.monthlyRecyclerView.adapter = adapter
    }

    private fun setupCategorySpinner() {
        // we'll populate after loading data
        binding.categoryFilterSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: android.view.View?, pos: Int, id: Long
                ) {
                    val category = parent!!.getItemAtPosition(pos) as String
                    loadData(category)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun loadData(filterCategory: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Fetch filtered entries
            allEntries = if (filterCategory == "All") {
                db.budgetEntryDao().getAll()
            } else {
                db.budgetEntryDao().getAll()
                    .filter { it.category == filterCategory }
            }

            // Prepare spinner categories
            val categories = listOf("All") +
                    allEntries.map { it.category }.distinct()

            // Aggregate per month
            val cal = Calendar.getInstance()
            val locale = Locale.getDefault()
            val monthLabels = listOf(
                "Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec"
            )
            // zero-fill 12 values
            val monthlyTotals = FloatArray(12) { 0f }
            allEntries.forEach { entry ->
                cal.time = entry.date
                val idx = cal.get(Calendar.MONTH)
                monthlyTotals[idx] += entry.amount.toFloat()
            }

            withContext(Dispatchers.Main) {
                // populate spinner
                val spinAdapter = ArrayAdapter(
                    this@AnnualOverviewActivity,
                    android.R.layout.simple_spinner_item,
                    categories
                ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                binding.categoryFilterSpinner.adapter = spinAdapter
                binding.categoryFilterSpinner.setSelection(categories.indexOf(filterCategory))

                // setup chart
                val entries = monthlyTotals.mapIndexed { i, total ->
                    BarEntry(i.toFloat(), total)
                }
                val dataSet = BarDataSet(entries, "Monthly Expenses")
                val barData = BarData(dataSet)
                binding.annualBarChart.data = barData

                binding.annualBarChart.xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(monthLabels)
                    granularity = 1f
                    setDrawGridLines(false)
                    position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                }

                val max = monthlyTotals.maxOrNull() ?: 0f
                val step = (max / 5).coerceAtLeast(1f)
                binding.annualBarChart.axisLeft.apply {
                    axisMinimum = 0f
                    axisMaximum = max + step
                    granularity = step
                }
                binding.annualBarChart.axisRight.isEnabled = false
                binding.annualBarChart.description.isEnabled = false
                binding.annualBarChart.legend.isEnabled = false
                binding.annualBarChart.invalidate()

                // update RecyclerView
                val monthlyList = monthLabels.mapIndexed { i, label ->
                    MonthlyExpense(label, monthlyTotals[i])
                }
                adapter.submitList(monthlyList)

                // CalendarView defaults to today automatically
                binding.calendarView.date = System.currentTimeMillis()
            }
        }
    }

    private fun setupBottomNav() {
       // binding.navHomeButton.setOnClickListener {
         //   startActivity(Intent(this, HomeActivity::class.java))
       // }
        binding.navReportsButton.setOnClickListener { /* already here */ }
      //  binding.navProfileButton.setOnClickListener {
       //     startActivity(Intent(this, ProfileActivity::class.java))
       // }
    }
}
