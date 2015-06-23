package com.incendiary.reactivegplus.nonRx;

import android.os.Bundle;

public interface GMSListener {
	void onServiceConnected(Bundle bundle);

	void onConnectionFailed();
}