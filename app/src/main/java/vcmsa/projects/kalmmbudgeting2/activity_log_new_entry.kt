package vcmsa.projects.kalmmbudgeting2

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import vcmsa.projects.kalmmbudgeting2.R
import vcmsa.projects.kalmmbudgeting2.data.AppDatabase
import vcmsa.projects.kalmmbudgeting2.data.BudgetEntry
import vcmsa.projects.kalmmbudgeting2.databinding.ActivityLogNewEntryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class LogNewEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogNewEntryBinding
    private val categories = mutableListOf("Transport", "School", "Car")
    private var imageUri: Uri? = null
    private val db by lazy { AppDatabase.getInstance(this) }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                binding.selectedImageView.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogNewEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCategorySpinner()
        setupDatePicker()
        setupButtons()
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.dateEditText.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selected = Calendar.getInstance().apply {
                        set(year, month, day)
                    }
                    binding.dateEditText.setText(
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(selected.time)
                    )
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener { finish() }
        binding.addCategoryButton.setOnClickListener { promptNewCategory() }
        binding.addImageButton.setOnClickListener { pickImage.launch("image/*") }
        binding.submitButton.setOnClickListener { submitEntry() }

        binding.navHomeButton.setOnClickListener { startActivity(Intent(this, HomeActivity::class.java)) }
         binding.navReportsButton.setOnClickListener { startActivity(Intent(this, ReportsExpenseActivity::class.java)) }
        // binding.navSettingsButton.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
    }

    private fun promptNewCategory() {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("New Category")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val newCat = input.text.toString().trim()
                if (newCat.isEmpty() || categories.contains(newCat)) {
                    Toast.makeText(this, "Invalid or duplicate category", Toast.LENGTH_SHORT).show()
                } else {
                    categories.add(newCat)
                    (binding.categorySpinner.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun submitEntry() {
        // Gather & validate inputs
        val entryTypeId = binding.entryTypeRadioGroup.checkedRadioButtonId
        val paymentMethodId = binding.paymentMethodRadioGroup.checkedRadioButtonId
        val amountText = binding.amountEditText.text.toString()
        val dateText = binding.dateEditText.text.toString()
        val descText = binding.descriptionEditText.text.toString()
        val category = binding.categorySpinner.selectedItem as String

        if (entryTypeId == -1 || paymentMethodId == -1 ||
            amountText.isBlank() || dateText.isBlank() || descText.isBlank()
        ) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val entryType = if (entryTypeId == R.id.radioIncome) "Income" else "Expense"
        val paymentMethod = when (paymentMethodId) {
            R.id.radioCash -> "Cash"
            R.id.radioDebit -> "Debit"
            else -> "Credit"
        }

        // Parse date
        val parsedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .parse(dateText)!!

        val entry = BudgetEntry(
            entryType = entryType,
            amount = amount,
            paymentMethod = paymentMethod,
            date = parsedDate,
            description = descText,
            category = category,
            imageUri = imageUri?.toString()
        )

        // Persist with Room on IO thread
        lifecycleScope.launch(Dispatchers.IO) {
            db.budgetEntryDao().insert(entry)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@LogNewEntryActivity,
                    "Entry saved!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}
