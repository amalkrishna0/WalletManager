package com.example.walletmanager

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.walletapp.Expense
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button
    private lateinit var ivSelectedImage: ImageView
    private var selectedImageBase64: String? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        btnSave = findViewById(R.id.btnSave)
        ivSelectedImage = findViewById(R.id.ivPreview)

        // Pre-fill from SMS (if any)
        intent.getStringExtra("sms_text")?.let {
            autoFillFromSMS(it)
        }

        // Choose image on click
        ivSelectedImage.setOnClickListener {
            pickImageFromGallery()
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
            val image = selectedImageBase64 ?: ""

            // Create Expense object
            val expense = Expense(
                amount = amount,
                description = description,
                date = date,
                time = time,
                imageBase64 = image
            )
            // Save to Firebase
            val expenseMap = hashMapOf(
                "amount" to amount,
                "description" to description,
                "date" to date,
                "time" to time,
                "imageBase64" to image
            )

            FirebaseFirestore.getInstance()
                .collection("expenses")
                .add(expenseMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Expense saved!", Toast.LENGTH_SHORT).show()

                    // Pass data back to MainActivity
                    val resultIntent = Intent().apply {
                        putExtra("expense", expense)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data ?: return
            val bitmap = getBitmapFromUri(imageUri)
            ivSelectedImage.setImageBitmap(bitmap)
            selectedImageBase64 = bitmapToBase64(bitmap)
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun autoFillFromSMS(sms: String) {
        val amountRegex = Regex("""(?:Rs\.?|INR|₹)\s?([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)
        val merchantRegex = Regex("""(?:at|to|in|from)\s+([\w\s@._-]+)""", RegexOption.IGNORE_CASE)

        val amount = amountRegex.find(sms)?.groups?.get(1)?.value?.replace(",", "")
        val description = merchantRegex.find(sms)?.groups?.get(1)?.value

        amount?.let { etAmount.setText(it) }
        description?.let { etDescription.setText(it.trim()) }
    }
}
