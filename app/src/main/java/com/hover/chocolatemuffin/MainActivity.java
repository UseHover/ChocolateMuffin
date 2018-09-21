package com.hover.chocolatemuffin;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hover.sdk.api.Hover;
import com.hover.sdk.api.HoverParameters;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
	public static String TAG = "Main Activity", RECIP = "+255752836781";
	public static final int PERM_REQUEST = 0, BUY_REQUEST = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Hover.initialize(this);
		fillViews();
		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (intent.getAction().equals("com.hover.chocolatemuffin.CONFIRMED_TRANSACTION")) {
			muffinBought();
			updateRunningTotal();
		}
	}

	private void updateParams() {
		String request;
		switch (Utils.getPayOption(this)) {
			case "Pay Bill": request = "pay_bill"; break;
			case "Merchant Till": request = "pay_merchant"; break;
			default: request = "send_money";
		}
		Button btn = ((Button) findViewById(R.id.hover_button));
		btn.setText(getString(R.string.buy_with, Utils.getServiceName(this)));
	}

	public void buy(View view) {
		launchHover("1e5926fc");
	}

	private void launchHover(String request) {
		Intent i = new HoverParameters.Builder(this)
				.request(request)
				.extra("amount", Utils.getPrice(this))
				.extra("recipient", Utils.getRecip(this))
				.buildIntent();
		startActivityForResult(i, BUY_REQUEST);
	}

	public void launchInstructionDialog(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Payment Instructions")
				.setMessage("Dial *150*00# choose MPESA then Send Money. Enter " + RECIP + " for the recipient, " + Utils.getPrice(this) + " Tsh for amount, then enter your pin and confirm");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();

		buy(null);
	}

	private void muffinBought() {
//		findViewById(R.id.background).setBackgroundResource(R.drawable.celebration);
//		((ImageView) findViewById(R.id.muffin_image)).setImageResource(R.drawable.exploded_muffin_orange);
		findViewById(R.id.hover_button).setVisibility(View.GONE);
		findViewById(R.id.success_msg).setVisibility(View.VISIBLE);
	}

	private void updateRunningTotal() {
		String totalString = getString(R.string.no_running_total);
		if (Utils.getMuffinCount(this) > 0)
			totalString = getResources().getQuantityString(R.plurals.running_total, Utils.getMuffinCount(this), Utils.getTotalSpent(this), Utils.getCurrency(this), Utils.getMuffinCount(this));
		((TextView) findViewById(R.id.running_total)).setText(totalString);
	}

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.e(TAG, "Received result");
		if (requestCode == PERM_REQUEST && resultCode == RESULT_OK)
			onSuccess(data);
		else if (requestCode == PERM_REQUEST)
			onUserCanceled();
		else if (requestCode == BUY_REQUEST && resultCode == RESULT_OK)
			Toast.makeText(MainActivity.this, "Please wait for confirmation", Toast.LENGTH_LONG).show();
		else if (requestCode == BUY_REQUEST && resultCode == RESULT_CANCELED)
			Toast.makeText(MainActivity.this, "Error: " + data.getStringExtra("error"), Toast.LENGTH_LONG).show();
	}

	public void onSuccess(Intent data) {
		Log.e(TAG, "Successfully added service: " + data.getStringExtra("serviceName"));
		Utils.setCurrency(data.getStringExtra("currency"), this);
		Utils.setServiceId(data.getIntExtra("serviceId", -1), this);
		Utils.setServiceName(data.getStringExtra("serviceName"), this);
		((TextView) findViewById(R.id.currency)).setText(data.getStringExtra("currency"));

		updateParams();
//		((BuyButton) findViewById(R.id.hover_button)).setCallback(mBtnCallbacks);
	}

	public void onUserCanceled() { Log.d(TAG, "User canceled"); }

	private void fillViews() {
		((TextView) findViewById(R.id.currency)).setText(Utils.getCurrency(this));
		((EditText) findViewById(R.id.price)).setText(Utils.getPrice(this));
		((EditText) findViewById(R.id.price)).addTextChangedListener(mPriceWatcher);
		((EditText) findViewById(R.id.recipient)).setText(Utils.getRecip(this));
		((EditText) findViewById(R.id.recipient)).addTextChangedListener(mRecipWatcher);
		Spinner spinner = (Spinner) findViewById(R.id.payment_type);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.payment_options, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);
		updateRunningTotal();
	}
	public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
		Utils.setPayOption((String) parent.getItemAtPosition(pos), this);
		updateParams();
	}
	public void onNothingSelected(AdapterView<?> parent) { }

	private TextWatcher mPriceWatcher = new TextWatcher() {
		@Override public void afterTextChanged(Editable s) { }
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
			Utils.setPrice(s.toString(), MainActivity.this);
			updateParams();
		}

	};
	private TextWatcher mRecipWatcher = new TextWatcher() {
		@Override public void afterTextChanged(Editable s) { }
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
			Utils.setRecip(s.toString(), MainActivity.this);
			updateParams();
		}
	};
}
