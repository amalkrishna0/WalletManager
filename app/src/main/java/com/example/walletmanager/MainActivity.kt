package com.example.walletmanager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.walletapp.Expense
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity() {

    private lateinit var expenseAdapter: ExpenseAdapter
    private val expenses = mutableListOf<Expense>()
    private lateinit var firestore: FirebaseFirestore
    private var expenseListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firestore = FirebaseFirestore.getInstance()

        expenseAdapter = ExpenseAdapter(expenses)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = expenseAdapter

        val addBtn = findViewById<Button>(R.id.btnAddExpense)
        addBtn.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivityForResult(intent, 100)
        }

        listenToExpensesFromFirestore()
    }

    private fun listenToExpensesFromFirestore() {
        expenseListener = firestore.collection("expenses")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                expenses.clear()
                for (doc in snapshot.documents) {
                    val expense = doc.toObject(Expense::class.java)
                    expense?.let { expenses.add(it) }
                }
                expenseAdapter.notifyDataSetChanged()
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
}
