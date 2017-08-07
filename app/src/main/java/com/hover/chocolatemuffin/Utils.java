package com.hover.chocolatemuffin;

import android.content.Context;
import android.content.SharedPreferences;

public class Utils {
	public static final String TAG = "Utils", CURRENCY = "currency", PRICE = "price", RECIP = "recip",
			PAY_OPTION = "pay_option", SERVICE_ID = "serviceId", SERVICE_NAME = "service_name",
			MUFFIN_COUNT = "muffin_count", TOTAL_SPENT = "total_spent";

	public static SharedPreferences getSharedPrefs(Context context) {
		return context.getSharedPreferences(context.getPackageName() + "_prefs", Context.MODE_MULTI_PROCESS);
	}

	public static int getTotalSpent(Context c) { return Utils.getSharedPrefs(c).getInt(TOTAL_SPENT, 0); }
	public static void increaseTotalSpent(int value, Context c) {
		SharedPreferences.Editor editor = Utils.getSharedPrefs(c).edit();
		editor.putInt(TOTAL_SPENT, getTotalSpent(c) + value);
		editor.commit();
	}

	public static int getMuffinCount(Context c) { return Utils.getSharedPrefs(c).getInt(MUFFIN_COUNT, 0); }
	public static void increaseMuffinCount(Context c) {
		SharedPreferences.Editor editor = Utils.getSharedPrefs(c).edit();
		editor.putInt(MUFFIN_COUNT, getMuffinCount(c) + 1);
		editor.commit();
	}

	public static void setServiceId(int value, Context c) {
		SharedPreferences.Editor editor = Utils.getSharedPrefs(c).edit();
		editor.putInt(SERVICE_ID, value);
		editor.commit();
	}
	public static int getServiceId(Context c) { return Utils.getSharedPrefs(c).getInt(SERVICE_ID, 8); }
	public static void setServiceName(String value, Context c) {
		SharedPreferences.Editor editor = Utils.getSharedPrefs(c).edit();
		editor.putString(SERVICE_NAME, value);
		editor.commit();
	}
	public static String getServiceName(Context c) { return Utils.getSharedPrefs(c).getString(SERVICE_NAME, c.getString(R.string.default_service_name)); }

	public static void setCurrency(String value, Context c) {
		SharedPreferences.Editor editor = Utils.getSharedPrefs(c).edit();
		editor.putString(CURRENCY, value);
		editor.commit();
	}
	public static String getCurrency(Context c) { return Utils.getSharedPrefs(c).getString(CURRENCY, c.getString(R.string.default_currency)); }

	public static void setPrice(String value, Context c) {
		SharedPreferences.Editor editor = Utils.getSharedPrefs(c).edit();
		editor.putString(PRICE, value);
		editor.commit();
	}
	public static String getPrice(Context c) { return Utils.getSharedPrefs(c).getString(PRICE, c.getString(R.string.default_price)); }

	public static void setRecip(String value, Context c) {
		SharedPreferences.Editor editor = Utils.getSharedPrefs(c).edit();
		editor.putString(RECIP, value);
		editor.commit();
	}
	public static String getRecip(Context c) { return Utils.getSharedPrefs(c).getString(RECIP, MainActivity.RECIP); }

	public static void setPayOption(String value, Context c) {
		SharedPreferences.Editor editor = Utils.getSharedPrefs(c).edit();
		editor.putString(PAY_OPTION, value);
		editor.commit();
	}
	public static String getPayOption(Context c) { return Utils.getSharedPrefs(c).getString(PAY_OPTION, c.getString(R.string.default_pay_option)); }
}
