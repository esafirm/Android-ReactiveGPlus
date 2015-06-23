package com.incendiary.reactivegplus;

import android.app.Activity;
import android.content.IntentSender;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;

/**
 * Created by esa on 23/06/15, with awesomeness
 */
public class GoogleLoginUtils {

	private static final int RC_SIGN_IN = 91001;

	public static void resolve(Activity activity, ConnectionResult connectionResult) {
		try {
			connectionResult.startResolutionForResult(activity, RC_SIGN_IN);
		} catch (IntentSender.SendIntentException e) {
			Log.e("Capruk", e.toString());
		}
	}

	public static boolean onActivityResult(int requestCode, int resultCode) {
		return requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_OK;
	}
}
