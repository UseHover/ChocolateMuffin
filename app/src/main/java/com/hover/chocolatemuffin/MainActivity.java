package com.hover.chocolatemuffin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hover.sdk.buttons.BuyButton;
import com.hover.sdk.buttons.BuyButtonCallback;
import com.hover.sdk.main.HoverIntegration;
import com.hover.sdk.main.HoverParameters;
import com.hover.sdk.main.PermActivity;
import com.hover.sdk.operators.Permission;

public class MainActivity extends AppCompatActivity implements HoverIntegration.HoverListener, AdapterView.OnItemSelectedListener {
	public static String TAG = "Main Activity";
	public static final int PERM_REQUEST = 0, BUY_REQUEST = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startActivityForResult(new Intent(this, PermActivity.class), PERM_REQUEST);
		fillViews();
	}

	private void addHoverIntegration() { HoverIntegration.add(8, Permission.NORMAL, this, this); }

	@Override public void onSIMError(String message) { Log.d(TAG, "Sim error: " + message); }
	@Override public void onError(String message) {
		Log.d(TAG, "Error: " + message);
	}
	@Override public void onUserDenied() { Log.d(TAG, "User denied"); }
	@Override
	public void onSuccess(int serviceId, String serviceName, String opSlug, String countryName, String currency) {
		Log.d(TAG, "Successfully added service: " + serviceName);
		Utils.setCurrency(currency, MainActivity.this);
		Utils.setServiceId(serviceId, MainActivity.this);
		Utils.setServiceName(serviceName, MainActivity.this);
		((TextView) findViewById(R.id.currency)).setText(currency);

		updateParams();
		((BuyButton) findViewById(R.id.hover_button)).setCallback(mBtnCallbacks);
	}

	private void updateParams() {
		String request;
		switch (Utils.getPayOption(this)) {
			case "Pay Bill": request = "pay_bill"; break;
			case "Merchant Till": request = "pay_merchant"; break;
			default: request = "send";
		}
		BuyButton btn = ((BuyButton) findViewById(R.id.hover_button));
		btn.setText(getString(R.string.buy_with, Utils.getServiceName(this)));
		btn.setBuyParameters(
				new HoverParameters.Builder(this)
						.request(request, Utils.getPrice(this), Utils.getCurrency(this), Utils.getRecip(this))
						.from(Utils.getServiceId(this))
						.build());
	}

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "Received result");
		if (requestCode == PERM_REQUEST && resultCode == RESULT_OK)
			addHoverIntegration();
		else if (requestCode == PERM_REQUEST)
			onUserDenied();
		else if (requestCode == BUY_REQUEST)
			hoverResult(requestCode, resultCode, data);
	}

	private void hoverResult(int requestCode, int resultCode, Intent data) {

	}

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
			Toast.makeText(MainActivity.this, "Processing...\n " + s, Toast.LENGTH_LONG).show();
		}

		@Override public void onSuccess(String s, int i) {

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
