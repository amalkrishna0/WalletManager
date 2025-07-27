package com.example.walletmanager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.walletapp.Expense
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // STEP 1: Read SMS Text if available
        val smsText = intent.getStringExtra("sms_text")
        smsText?.let {
            autoFillFromSMS(it)
        }

        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (amount.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val expense = Expense(amount, description, date, time)

            val resultIntent = Intent().apply {
                putExtra("expense", expense)
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    // STEP 2: Basic SMS Parsing Logic
    private fun autoFillFromSMS(sms: String) {
        val amountRegex = Regex("""(?:Rs\.?|INR|₹)\s?([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)
        val merchantRegex = Regex("""(?:at|to|in|from)\s+([\w\s@._-]+)""", RegexOption.IGNORE_CASE)

        val amountMatch = amountRegex.find(sms)
        val merchantMatch = merchantRegex.find(sms)

        val amount = amountMatch?.groups?.get(1)?.value?.replace(",", "")
        val description = merchantMatch?.groups?.get(1)?.value

        if (amount != null) etAmount.setText(amount)
        if (description != null) etDescription.setText(description.trim())
    }
}
