package com.mariussoft.endlessjabber.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

public class EndlessJabberInterface {

	/** Checks whether or not EndlessJabber is installed */
	public static boolean IsInstalled(Context ctx) {

		PackageManager pm = ctx.getPackageManager();
		try {
			pm.getPackageInfo("com.mariussoft.endlessjabber", PackageManager.GET_ACTIVITIES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	/** Opens up GooglePlay and redirects it to the EndlessJabber app */
	public static void OpenGooglePlayLink(Context ctx, String referralCode) {

		SharedPreferences.Editor editor = ctx.getSharedPreferences("EndlessJabberSDK", Context.MODE_PRIVATE).edit();
		editor.putString("Referral", referralCode);
		editor.commit();

		final String appPackageName = "com.mariussoft.endlessjabber";
		try {
			ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName + "&referrer=utm_source%3D" + referralCode)));
		} catch (android.content.ActivityNotFoundException anfe) {
			ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName + "&referrer=utm_source%3D" + referralCode)));
		}
	}

	/** Launches EndlessJabber */
	public static void LaunchEndlessJabber(Context ctx) {
		Intent intent = ctx.getPackageManager().getLaunchIntentForPackage("com.mariussoft.endlessjabber");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);
	}

	/**
	 * Enables integration by specifying which class to call
	 * 
	 * @param context
	 *            The context to use
	 * @param concreteImplementation
	 *            The concrete implementation to call on events
	 * @param sendSMS
	 *            If true, your app will be responsible for sending SMS messages
	 *            as well as persisting them to the SMS repository
	 * @param sendMMS
	 *            If true, your app will be responsible for sending & persisting
	 *            MMS messages to the MMS repository
	 * @param dontDeleteMessages
	 *            If true, your app will be responsible for deleting conversations and messages when signalled
	 *            MMS messages to the MMS repository
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static void EnableIntegration(Context context, Class concreteImplementation, boolean sendSMS, boolean sendMMS, boolean dontDeleteMessages) throws Exception {
		Class<?> clazz = concreteImplementation;
		if (!IEndlessJabberImplementation.class.isAssignableFrom(clazz)) {
			throw new Exception("Class must implement IEndlessJabberImplementation");
		}

		SharedPreferences.Editor editor = context.getSharedPreferences("EndlessJabberSDK", Context.MODE_PRIVATE).edit();
		editor.putString("InterfaceClass", concreteImplementation.getName());
		editor.putBoolean("SendSMS", sendSMS);
		editor.putBoolean("SendMMS", sendMMS);
		editor.putBoolean("DontDelete", dontDeleteMessages);
		editor.commit();

		SendInfoToEndlessJabber(context);
	}

	/** Disables integration */
	@SuppressWarnings("rawtypes")
	public static void DisableIntegration(Context context, Class nameOfImplementation) {
		SharedPreferences.Editor editor = context.getSharedPreferences("EndlessJabberSDK", Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();

		SendInfoToEndlessJabber(context);
	}

	/**
	 * Refresh integration info with EndlessJabber
	 * 
	 * @param ctx
	 *            Context to use
	 */
	static void SendInfoToEndlessJabber(Context context) {
		String EndlessJabber_INTENT = "com.mariussoft.endlessjabber.action.extendResponse";
		SharedPreferences prefs = context.getSharedPreferences("EndlessJabberSDK", Context.MODE_PRIVATE);

		Intent i = new Intent();
		i.setAction(EndlessJabber_INTENT);
		i.putExtra("Action", "UpdateInfo");
		i.putExtra("PackageName", context.getPackageName());
		i.putExtra("Enabled", prefs.contains("InterfaceClass"));
		i.putExtra("Referral", prefs.getString("Referral", ""));

		if (prefs.contains("InterfaceClass")) {
			i.putExtra("SendSMS", prefs.getBoolean("SendSMS", false));
			i.putExtra("SendMMS", prefs.getBoolean("SendMMS", false));
 			i.putExtra("DontDelete", prefs.getBoolean("DontDelete", false));
		}

		context.sendBroadcast(i);
	}
}
