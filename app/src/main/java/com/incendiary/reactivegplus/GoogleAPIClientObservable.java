package com.incendiary.reactivegplus;

import android.content.Context;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import java.util.List;

import rx.Observable;
import rx.Observer;

public class GoogleAPIClientObservable extends BaseObservable<GoogleApiClient> {

	@SafeVarargs
	public static Observable<GoogleApiClient> create(Context context, Api<? extends Api.ApiOptions.NotRequiredOptions>... apis) {
		return Observable.create(new GoogleAPIClientObservable(context, apis));
	}

	@SafeVarargs
	public static Observable<GoogleApiClient> create(Context context, List<Scope> scopes, Api<?
			extends Api.ApiOptions.NotRequiredOptions>... apis) {
		return Observable.create(new GoogleAPIClientObservable(context, scopes, apis));
	}

	@SafeVarargs
	protected GoogleAPIClientObservable(Context ctx, Api<? extends Api.ApiOptions.NotRequiredOptions>... apis) {
		this(ctx, null, apis);
	}

	@SafeVarargs
	protected GoogleAPIClientObservable(Context ctx, List<Scope> scopes, Api<? extends Api.ApiOptions.NotRequiredOptions>... apis) {
		super(ctx, scopes, apis);
	}

	@Override
	protected void onGoogleApiClientReady(GoogleApiClient apiClient, Observer<? super GoogleApiClient> observer) {
		observer.onNext(apiClient);
		observer.onCompleted();
	}
}