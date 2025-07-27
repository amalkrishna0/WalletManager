package com.example.walletmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class SMSBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
            val extras = intent.extras ?: return
            val status = extras.get(SmsRetriever.EXTRA_STATUS) as? Status

            when (status?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as? String
                    message?.let {
                        Toast.makeText(context, "SMS received: $it", Toast.LENGTH_LONG).show()

                        val addIntent = Intent(context, AddExpenseActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            putExtra("sms_text", it)
                        }
                        context?.startActivity(addIntent)
                    }
                }

                CommonStatusCodes.TIMEOUT -> {
                    Toast.makeText(context, "SMS Retriever timed out", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(context, "Failed to retrieve SMS", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
