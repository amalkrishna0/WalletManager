package com.example.walletmanager

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.walletapp.Expense
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private lateinit var expenseAdapter: ExpenseAdapter
    private val expenses = mutableListOf<Expense>()
    private lateinit var firestore: FirebaseFirestore
    private var expenseListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firestore = FirebaseFirestore.getInstance()

        expenseAdapter = ExpenseAdapter(expenses) { expense ->
            showDeleteDialog(expense)
        }
        val recyclerView = findViewById<RecyclerView>(R.id.rvExpenses)
        val spinnerFilter = findViewById<Spinner>(R.id.spinnerFilter)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = expenseAdapter

        val addBtn = findViewById<Button>(R.id.btnAddExpense)
        addBtn.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivityForResult(intent, 100)
        }
        val filterOptions = listOf("All", "High to Low", "Low to High", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = spinnerAdapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()

                val filteredList = when (selected) {
                    "All" -> expenses
                    "High to Low" -> expenses.sortedByDescending { it.amount.toDoubleOrNull() ?: 0.0 }
                    "Low to High" -> expenses.sortedBy { it.amount.toDoubleOrNull() ?: 0.0 }
                    else -> expenses.filter {
                        val month = it.date.split("-").getOrNull(1)?.toIntOrNull()
                        val monthName = month?.let { m -> java.text.DateFormatSymbols().months[m - 1] }
                        monthName?.equals(selected, ignoreCase = true) == true
                    }
                }

                expenseAdapter.updateList(filteredList)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        listenToExpensesFromFirestore()
    }

    private fun listenToExpensesFromFirestore() {
        expenseListener = firestore.collection("expenses")
            .orderBy("timestamp")  // 🔥 This ensures sorting by time
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                expenses.clear()
                for (doc in snapshot.documents) {
                    val expense = doc.toObject(Expense::class.java)
                    expense?.let { expenses.add(it) }
                }

                // Automatically refresh UI based on current filter
                val spinnerFilter = findViewById<Spinner>(R.id.spinnerFilter)
                val selected = spinnerFilter.selectedItem.toString()

                val filteredList = when (selected) {
                    "All" -> expenses
                    "High to Low" -> expenses.sortedByDescending { it.amount.toDoubleOrNull() ?: 0.0 }
                    "Low to High" -> expenses.sortedBy { it.amount.toDoubleOrNull() ?: 0.0 }
                    else -> expenses.filter {
                        val month = it.date.split("-").getOrNull(1)?.toIntOrNull()
                        val monthName = month?.let { m -> java.text.DateFormatSymbols().months[m - 1] }
                        monthName?.equals(selected, ignoreCase = true) == true
                    }
                }

                expenseAdapter.updateList(filteredList)

            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            val expense = data.getSerializableExtra("expense") as? Expense
            expense?.let {
                firestore.collection("expenses").add(it) // ✅ Add to Firestore
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        expenseListener?.remove() // ✅ Cleanup Firestore listener
    }
    private fun showDeleteDialog(expense: Expense) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Expense")
        builder.setMessage("Do you really want to delete this expense?")

        builder.setPositiveButton("Delete") { _, _ ->
            showBiometricPrompt {
                deleteExpense(expense)
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate to Delete")
            .setDescription("Use fingerprint to confirm deletion")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun deleteExpense(expense: Expense) {
        firestore.collection("expenses")
            .whereEqualTo("amount", expense.amount)
            .whereEqualTo("description",expense.description)
            .whereEqualTo("date", expense.date)
            .whereEqualTo("time", expense.time)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    firestore.collection("expenses").document(document.id).delete()
                }
            }
    }

}
