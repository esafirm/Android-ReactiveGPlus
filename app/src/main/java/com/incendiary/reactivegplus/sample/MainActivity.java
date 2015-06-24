package com.incendiary.reactivegplus.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.plus.model.people.Person;
import com.incendiary.reactivegplus.GMSProvider;
import com.incendiary.reactivegplus.R;

import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.functions.Action1;

/**
 * Created by esa on 22/06/15, with awesomeness
 */
public class MainActivity extends Activity {

	private GMSProvider mGmsProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button button = (Button) findViewById(R.id.btn);
		ViewObservable.clicks(button).subscribe(new Action1<OnClickEvent>() {
			@Override
			public void call(OnClickEvent onClickEvent) {
				login();
			}
		});

		mGmsProvider = new GMSProvider(this);
	}

	private void login() {
		mGmsProvider.getCurrentUserObservable().subscribe(new Action1<Person>() {
			@Override
			public void call(Person person) {
				TextView textView = (TextView) findViewById(R.id.txt);
				textView.setText(person.getId());
			}
		}, new Action1<Throwable>() {
			@Override
			public void call(Throwable throwable) {
				// this shouldn't happen
				throwable.printStackTrace();
			}
		});
	}

	private void toast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.w("Capruk", "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode
				+ " intentData:" + data);

		mGmsProvider.onActivityResult(requestCode, resultCode);
		super.onActivityResult(requestCode, resultCode, data);
	}
}
