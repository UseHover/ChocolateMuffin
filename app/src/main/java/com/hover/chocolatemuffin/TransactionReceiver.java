package com.hover.chocolatemuffin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.HashMap;

public class TransactionReceiver extends BroadcastReceiver {
	public TransactionReceiver() { }

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Utils.increaseMuffinCount(context);
			if (intent.hasExtra("transaction_extras")) {
				//noinspection unchecked
				String amount = ((HashMap<String, String>) intent.getExtras().get("transaction_extras")).get("amount");
				Utils.increaseTotalSpent(Float.valueOf(amount).intValue(), context);
			}
//			Toast.makeText(context, context.getString(R.string.successfully_bought, intent.getStringExtra("amount")), Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(context, context.getString(R.string.error_receiver), Toast.LENGTH_LONG).show();
		}

		Intent i = new Intent(intent);
		i.setClass(context, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(i);
	}
}
