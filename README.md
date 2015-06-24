# Android-ReactiveGPlus
Chain that shit (Read: Google Plus from Google Play Service API) up! 

### This is a work in progress - your suggestion is highly appreciated  

##Setup

First thing first setup your Google Play Service, you can find the documentation in here https://developers.google.com/+/mobile/android/getting-started

There's no stable release yet (or any release to be fair), but if you want to try it (with hassle free) you can add this jitpack.io to your build.gradle

```java
repositories {
	    maven {
	        url "https://jitpack.io"
	    }
	}
		dependencies {
	        compile 'com.github.esafirm:Android-ReactiveGPlus:42b8f1be17'
	}
```

##Usage

Call what you need from `GMSProvider` class

```java
  
  GMSProvider gmsProvider = new GMSProvider(activity);
	gmsProvider.getCurrentUserObservable().subscribe(new Action1<Person>() {
			@Override
			public void call(Person person) {
				TextView textView = (TextView) findViewById(R.id.txt);
				textView.setText(person.getId());
			}
	});

```

And listen to `onActivityResult` on your activity

```java
mGmsProvider.onActivityResult(requestCode, resultCode);
```






