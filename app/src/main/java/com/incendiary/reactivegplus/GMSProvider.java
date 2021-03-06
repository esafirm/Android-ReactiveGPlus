package com.incendiary.reactivegplus;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by esa on 22/06/15, with awesomeness
 */
public class GMSProvider {

	private final Context mContext;

	private Boolean isLoginResolved;

	public static GMSProvider newInstance(Activity activity) {
		return new GMSProvider(activity);
	}

	public GMSProvider(Context mContext) {
		this.mContext = mContext;
	}

	public final Observable<GoogleApiClient> getGoogleApiClientObservable(Api... apis) {
		//noinspection unchecked
		return GoogleAPIClientObservable.create(mContext, apis);
	}

	public final Observable<GoogleApiClient> getLoginObserveable() {
		List<Scope> scopes = Arrays.asList(Plus.SCOPE_PLUS_LOGIN, Plus.SCOPE_PLUS_PROFILE);
		return GoogleAPIClientObservable.create(mContext, scopes, Plus.API)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
					@Override
					public Observable<?> call(Observable<? extends Throwable> observable) {
						return observable.flatMap(new Func1<Throwable, Observable<?>>() {
							@Override
							public Observable<?> call(Throwable throwable) {
								if (throwable instanceof GoogleAPIConnectionException) {
									GoogleAPIConnectionException exception = (GoogleAPIConnectionException) throwable;
									if (exception.getConnectionResult().getErrorCode() == ConnectionResult.SIGN_IN_REQUIRED) {
										if (isLoginResolved == null || isLoginResolved) {
											isLoginResolved = false;
											GoogleLoginUtils.resolve((Activity) mContext, exception.getConnectionResult());
										}
										return Observable.timer(1, TimeUnit.SECONDS);
									}
								}
								return Observable.error(throwable);
							}
						});
					}
				});
	}

	public final Observable<Person> getCurrentUserObservable() {
		return getLoginObserveable().flatMap(new Func1<GoogleApiClient, Observable<Person>>() {
			@Override
			public Observable<Person> call(GoogleApiClient googleApiClient) {
				return Observable.just(Plus.PeopleApi.getCurrentPerson(googleApiClient));
			}
		});
	}

	public final Observable<People.LoadPeopleResult> getPeopleObservable(final String token) {
		return getLoginObserveable().flatMap(new Func1<GoogleApiClient, Observable<People.LoadPeopleResult>>() {
			@Override
			public Observable<People.LoadPeopleResult> call(GoogleApiClient googleApiClient) {
				return fromPendingResult(Plus.PeopleApi.loadVisible(googleApiClient, token));
			}
		});
	}

	public final Observable<String> getAccountNameObservable() {
		return getLoginObserveable().flatMap(new Func1<GoogleApiClient, Observable<String>>() {
			@Override
			public Observable<String> call(GoogleApiClient googleApiClient) {
				return Observable.just(Plus.AccountApi.getAccountName(googleApiClient));
			}
		});
	}

	public final Observable<Status> getRevokeAccessObservable() {
		return getLoginObserveable().flatMap(new Func1<GoogleApiClient, Observable<Status>>() {
			@Override
			public Observable<Status> call(GoogleApiClient googleApiClient) {
				return fromPendingResult(Plus.AccountApi.revokeAccessAndDisconnect(googleApiClient));
			}
		});
	}

	public static <T extends Result> Observable<T> fromPendingResult(PendingResult<T> result) {
		return Observable.create(new PendingResultObservable<>(result));
	}

	public void onActivityResult(int requestCode, int resultCode) {
		isLoginResolved = GoogleLoginUtils.onActivityResult(requestCode, resultCode);
	}
}
