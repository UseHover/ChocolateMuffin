package com.hover.chocolatemuffin;

import android.content.Context;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hover.sdk.buttons.BuyButton;
import com.hover.sdk.buttons.BuyButtonCallback;
import com.hover.sdk.main.HoverParameters;
import com.hover.sdk.onboarding.HoverIntegrationActivity;
import com.hover.sdk.operators.Permission;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
	public static String TAG = "Main Activity", RECIP = "+255752836781";
	public static final int PERM_REQUEST = 0, BUY_REQUEST = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		addHoverIntegration();
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

	private void addHoverIntegration() {
		Intent integrationIntent = new Intent(this, HoverIntegrationActivity.class);
		integrationIntent.putExtra(HoverIntegrationActivity.SERVICE_IDS, new int[] { 3, 4, 5, 6, 7, 8, 11, 14, 15, 16, 13, 17, 19, 20 });
		integrationIntent.putExtra(HoverIntegrationActivity.PERM_LEVEL, Permission.NORMAL);
		integrationIntent.putExtra(HoverIntegrationActivity.PRIMARY_COLOR, R.color.colorPrimary);
		startActivityForResult(integrationIntent, PERM_REQUEST);
//		HoverIntegration.add(new int[] {3, 4, 5, 6, 7, 8, 11, 14, 15, 16, 13, 17, 19, 20}, Permission.NORMAL, this, this);
	}

	private void updateParams() {
		String request;
		switch (Utils.getPayOption(this)) {
			case "Pay Bill": request = "pay_bill"; break;
			case "Merchant Till": request = "pay_merchant"; break;
			default: request = "send_money";
		}
		BuyButton btn = ((BuyButton) findViewById(R.id.hover_button));
		btn.setText(getString(R.string.buy_with, Utils.getServiceName(this)));
		btn.setBuyParameters(
				new HoverParameters.Builder(this)
						.request(request, Utils.getPrice(this), Utils.getCurrency(this), Utils.getRecip(this))
						.from(Utils.getServiceId(this))
						.build(), BUY_REQUEST);
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

		Intent i = new HoverParameters.Builder(this)
				.request("send_money", Utils.getPrice(this), Utils.getCurrency(this), Utils.getRecip(this))
				.from(Utils.getServiceId(this))
				.buildIntent();
		startActivityForResult(i, BUY_REQUEST);
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
		else if (requestCode == BUY_REQUEST)
			((BuyButton) findViewById(R.id.hover_button)).onActivityResult(requestCode, resultCode, data);
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

	private BuyButtonCallback mBtnCallbacks = new BuyButtonCallback() {
		@Override public void onError(Throwable throwable) {
			Toast.makeText(MainActivity.this, "Fatal error: " + throwable.toString(), Toast.LENGTH_LONG).show();
		}

		@Override public void onValidationError(String s) {
			Toast.makeText(MainActivity.this, "Validation error: " + s, Toast.LENGTH_LONG).show();
		}

		@Override public void onServiceError(String s) {
			Toast.makeText(MainActivity.this, "Service returned error: " + s, Toast.LENGTH_LONG).show();
		}

		@Override public void onServiceProcessing(String s, int i) {
//			Toast.makeText(MainActivity.this, "Processing...\n " + s, Toast.LENGTH_LONG).show();
		}
	};

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
