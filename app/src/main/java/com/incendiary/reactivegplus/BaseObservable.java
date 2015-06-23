package com.incendiary.reactivegplus;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;


public abstract class BaseObservable<T> implements Observable.OnSubscribe<T> {

	private WeakHashMap<Context, ApiClientConnectionCallbacks> mClientWeakHashMap = new WeakHashMap<>();

	private final Context ctx;
	private final List<Api<? extends Api.ApiOptions.NotRequiredOptions>> services;
	private final List<Scope> scopes;

	@SafeVarargs
	protected BaseObservable(Context ctx, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
		this(ctx, null, services);
	}

	@SafeVarargs
	protected BaseObservable(Context ctx, List<Scope> scopes, Api<? extends Api.ApiOptions.NotRequiredOptions>... services) {
		this.ctx = ctx;
		this.services = Arrays.asList(services);
		this.scopes = scopes;
	}

	@Override
	public void call(Subscriber<? super T> subscriber) {

		boolean canReuse = mClientWeakHashMap.containsKey(ctx);

		final ApiClientConnectionCallbacks apiConnection =
				canReuse ? mClientWeakHashMap.get(ctx).withObserver(subscriber) : createApiClient(subscriber);

		if (!canReuse)
			mClientWeakHashMap.put(ctx, apiConnection);

		final GoogleApiClient apiClient = apiConnection.getApiClient();

		try {
			apiClient.connect();
		} catch (Throwable ex) {
			subscriber.onError(ex);
		}

		subscriber.add(Subscriptions.create(new Action0() {
			@Override
			public void call() {
				if (apiClient.isConnected() || apiClient.isConnecting()) {
					mClientWeakHashMap.remove(ctx);
					onUnsubscribed(apiClient);
					apiClient.disconnect();
				}
			}
		}));
	}


	protected ApiClientConnectionCallbacks createApiClient(Subscriber<? super T> subscriber) {

		ApiClientConnectionCallbacks apiClientConnectionCallbacks = new ApiClientConnectionCallbacks(subscriber);

		GoogleApiClient.Builder apiClientBuilder = new GoogleApiClient.Builder(ctx);

		for (Api<? extends Api.ApiOptions.NotRequiredOptions> service : services)
			apiClientBuilder.addApi(service);

		if (scopes != null)
			for (Scope scope : scopes) {
				apiClientBuilder.addScope(scope);
			}

		apiClientBuilder.addConnectionCallbacks(apiClientConnectionCallbacks);
		apiClientBuilder.addOnConnectionFailedListener(apiClientConnectionCallbacks);

		GoogleApiClient apiClient = apiClientBuilder.build();

		apiClientConnectionCallbacks.setClient(apiClient);

		return apiClientConnectionCallbacks;
	}

	protected void onUnsubscribed(GoogleApiClient locationClient) {
	}

	protected abstract void onGoogleApiClientReady(GoogleApiClient apiClient, Observer<? super T> observer);

	private class ApiClientConnectionCallbacks implements
			GoogleApiClient.ConnectionCallbacks,
			GoogleApiClient.OnConnectionFailedListener {

		private Observer<? super T> observer;

		private GoogleApiClient apiClient;

		private ApiClientConnectionCallbacks(Observer<? super T> observer) {
			this.observer = observer;
		}

		@Override
		public void onConnected(Bundle bundle) {
			try {
				onGoogleApiClientReady(apiClient, observer);
			} catch (Throwable ex) {
				observer.onError(ex);
			}
		}

		@Override
		public void onConnectionSuspended(int cause) {
			observer.onError(new GoogleAPIConnectionSuspendedException(cause));
		}

		@Override
		public void onConnectionFailed(ConnectionResult connectionResult) {
			observer.onError(new GoogleAPIConnectionException("Error connecting to GoogleApiClient.", connectionResult));
		}

		public ApiClientConnectionCallbacks withObserver(Observer<? super T> observer) {
			this.observer = observer;
			return this;
		}

		public void setClient(GoogleApiClient client) {
			this.apiClient = client;
		}

		public GoogleApiClient getApiClient() {
			return this.apiClient;
		}
	}

}