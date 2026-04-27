package com.codecash.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codecash.app.adapter.CategoryTotalAdapter
import com.codecash.app.data.AppDatabase
import com.codecash.app.databinding.ActivityCategoryTotalsBinding
import com.codecash.app.util.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CategoryTotalsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryTotalsBinding
    private lateinit var db: AppDatabase
    private lateinit var session: SessionManager
    private lateinit var adapter: CategoryTotalAdapter
    private val displaySdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val storageSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
    private var startDate = ""
    private var endDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryTotalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)
        session = SessionManager(this)

        binding.btnBack.setOnClickListener { finish() }
        setDefaultPeriod()

        adapter = CategoryTotalAdapter(emptyList())
        binding.rvCategoryTotals.apply {
            layoutManager = LinearLayoutManager(this@CategoryTotalsActivity)
            adapter = this@CategoryTotalsActivity.adapter
        }

        setupDateButtons()
        binding.btnFilter.setOnClickListener {
            if (startDate > endDate) {
                Toast.makeText(this, "Start date must be before end date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loadTotals()
        }
        loadTotals()
    }

    private fun setDefaultPeriod() {
        val cal = Calendar.getInstance()
        endDate = storageSdf.format(cal.time)
        binding.btnEndDate.text = displaySdf.format(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        startDate = storageSdf.format(cal.time)
        binding.btnStartDate.text = displaySdf.format(cal.time)
    }

    private fun setupDateButtons() {
        binding.btnStartDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                val sel = Calendar.getInstance().apply { set(y, m, d) }
                startDate = storageSdf.format(sel.time)
                binding.btnStartDate.text = displaySdf.format(sel.time)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
        binding.btnEndDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                val sel = Calendar.getInstance().apply { set(y, m, d) }
                endDate = storageSdf.format(sel.time)
                binding.btnEndDate.text = displaySdf.format(sel.time)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun loadTotals() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                val totals = db.expenseEntryDao().getCategoryTotalsInPeriod(session.getUserId(), startDate, endDate)
                val cats = db.categoryDao().getCategoriesByUserOnce(session.getUserId())
                val catMap = cats.associateBy { it.id }

                val items = totals.map { ct ->
                    val cat = ct.categoryId?.let { catMap[it] }
                    CategoryTotalAdapter.CategoryTotalItem(
                        categoryName = cat?.name ?: "Uncategorised",
                        colorHex = cat?.colorHex ?: "#8BA4C0",
                        total = ct.total,
                        limit = cat?.monthlyLimit ?: 0.0,
                        formattedTotal = currencyFormat.format(ct.total)
                    )
                }.sortedByDescending { it.total }

                binding.tvGrandTotal.text = "Total: ${currencyFormat.format(items.sumOf { it.total })}"
                binding.tvPeriodLabel.text = "Period: ${binding.btnStartDate.text} – ${binding.btnEndDate.text}"
                adapter.updateData(items)
                binding.progressBar.visibility = View.GONE
                binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@CategoryTotalsActivity, "Failed to load totals", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
