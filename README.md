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

##License
    
    Copyright 2015 Esa Firman

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.





