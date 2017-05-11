# Description
![Preview](img/diagram.jpg)

# Setup
![Preview](app/src/main/res/mipmap-hdpi/ic_launcher.png)
[![](https://jitpack.io/v/nhancv/nc-android-ntask.svg)](https://jitpack.io/#nhancv/nc-android-ntask)

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Add the dependency

	dependencies {
	        compile 'com.github.nhancv:nc-android-ntask:${version}'
	}

# Usage

Create TaskService

```java
public class TaskService extends AbstractTaskService {

    @Override
    protected void doing(Intent intent) {
        while (NTaskManager.hasNext()) {
            RTask rTask = NTaskManager.next();
            if (rTask != null) {
            	//Do something
                System.out.println("Process: " + rTask.getId());

                //Set complete if do succeed
                NTaskManager.completeTask(rTask);
            }
        }
    }
}
```

In manifest

```xml
<service android:name=".TaskService" />
```

Initialize service

```
NTaskManager.init(this, TaskService.class);
```

Post task request

```
NTaskManager.postTask(RTask.build(...));
```

#Libs
- RxAndroid 1.2.1
- RxJava 1.3.0
- Realm 3.1.3






