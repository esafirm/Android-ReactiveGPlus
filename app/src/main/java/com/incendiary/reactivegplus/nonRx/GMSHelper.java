package com.incendiary.reactivegplus.nonRx;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by esa on 24/11/14. with awesomeness
 */
public class GMSHelper implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

	private static final int RC_SIGN_IN = 91001;

	public static WeakHashMap<Context, GMSHelper> managerHashMap = new WeakHashMap<>();

	private Activity mActivity;
	private GoogleApiClient mGoogleApiClient;
	private GMSListener mGmsListener;

	private Dialog mErrorDialog;

	private int mErrorCode = -1;

	public static GMSHelper getInstance(Activity activity) {
		GMSHelper gmsHelper = managerHashMap.get(activity);
		if (gmsHelper == null) {
			gmsHelper = new GMSHelper(activity);
			managerHashMap.put(activity, gmsHelper);
		}
		return gmsHelper;
	}

	public GMSHelper(Activity activity) {
		mActivity = activity;
		mGoogleApiClient = new GoogleApiClient.Builder(activity)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Plus.API)
				.addScope(Plus.SCOPE_PLUS_LOGIN)
				.addScope(Plus.SCOPE_PLUS_PROFILE)
				.build();
	}

	public GMSHelper listener(GMSListener mGmsListener) {
		this.mGmsListener = mGmsListener;
		return this;
	}


	/* ============== ACTIVITY RELATED =============*/

	public void onStart() {
		mGoogleApiClient.connect();
	}

	public void onStop() {
		if (mGoogleApiClient.isConnected())
			mGoogleApiClient.disconnect();
		managerHashMap.remove(mActivity);
	}

	/* ============== PUBLIC METHOD ============ */

	public GoogleApiClient getClient() {
		return mGoogleApiClient;
	}

	public Activity getActivity() {
		return mActivity;
	}

	public void signOut() {
		List<GMSHelper> gmsHelpers = new ArrayList<>(managerHashMap.values());
		for (GMSHelper gmsHelper : gmsHelpers) {
			if (gmsHelper.getClient().isConnected()) {
				Plus.AccountApi.clearDefaultAccount(gmsHelper.getClient());
				gmsHelper.getClient().disconnect();
				managerHashMap.remove(gmsHelper.getActivity());
			}
		}
	}

	public void signInIfNeeded() {
		if (mGoogleApiClient.isConnected())
			onConnected(null);
		else
			mGoogleApiClient.connect();
	}

	public void loadPeopleList(String pageToken, ResultCallback<People.LoadPeopleResult> resultResultCallback) {
		Plus.PeopleApi.loadVisible(mGoogleApiClient, pageToken).setResultCallback(resultResultCallback);
	}

	/* ============ CONNECTIN RELATED =============*/

	@Override
	public void onConnected(Bundle bundle) {
		if (mGmsListener != null)
			mGmsListener.onServiceConnected(bundle);
	}

	@Override
	public void onConnectionSuspended(int i) {
		mGoogleApiClient.connect();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (mGmsListener != null)
			mGmsListener.onConnectionFailed();

		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(mActivity, RC_SIGN_IN);
			} catch (IntentSender.SendIntentException e) {
				e.printStackTrace();
			}
		} else {
			showErrorDialog(connectionResult.getErrorCode());
		}
		mErrorCode = connectionResult.getErrorCode();
	}

	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RC_SIGN_IN) {
			if (resultCode == Activity.RESULT_OK) {
				mGoogleApiClient.connect();
			} else {
				GooglePlayServicesUtil.getErrorDialog(mErrorCode, mActivity, requestCode);
			}
			return true;
		}
		return false;
	}

	private void showErrorDialog(int errorCode) {
		if (mErrorDialog == null || errorCode != mErrorCode)
			mErrorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, mActivity, RC_SIGN_IN);

		if (mErrorDialog != null && !mErrorDialog.isShowing()) {
			FragmentManager fragmentManager = mActivity.getFragmentManager();
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();
			errorFragment.setDialog(mErrorDialog);
			fragmentManager.beginTransaction().add(errorFragment,
					ErrorDialogFragment.class.getSimpleName()).commitAllowingStateLoss();
		}
		mErrorCode = errorCode;
	}

	public static class ErrorDialogFragment extends DialogFragment {

		private Dialog mDialog;

		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
}
