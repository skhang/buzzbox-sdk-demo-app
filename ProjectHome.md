![http://hub.buzzbox.com/sr/images/demo_app_home_small.png](http://hub.buzzbox.com/sr/images/demo_app_home_small.png)

This is a very simple open source app that uses different parts of the BuzzBox SDK:

  * **Scheduler**:
it allows you to run a background task up to every minute according to a cron string (i.e. "0 8-19  1,2,3,4,5" will run every hour, from 8am to 7pm, Monday - Friday). All what you need is to implements an interface and write your logic there without struggling with Services, WakefulIntentService, Receivers, etc...

```
// 1. implement a Task
public class HelloWorldTask implements Task{
      
        @Override
        public TaskResult doWork(ContextWrapper ctx) {
                TaskResult res = new TaskResult();

                /*
                 * 1.1. do some work such as calling your server, calling a third part service or 
                 * whatever you need in order to decide if you need to deliver a notification.
                 */ 

                // 1.2. create a notification based on your results
                res.addMessage( new NotificationMessage("Hello world", "Don't forget to open Hello World App").setNotificationClickIntentClass(HelloWorldActivity.class));    
                return res;
        }

}
```

```
// 2. schedule the Task in your main activity
public class BrowserActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ...
        // 2.1. call BuzzBox Analytics
        int openAppStatus = AnalyticsManager.onOpenApp(this); 

        // 2.2. add the Task to the Scheduler
        if (openAppStatus==AnalyticsManager.OPEN_APP_FIRST_TIME) { 
              // register the Task when the App in installed
              SchedulerManager.getInstance().saveTask(this, "*/1 * * * *" /* this is a cron string*/, HelloWorldTask.class);
              SchedulerManager.getInstance().restart(this, HelloWorldTask.class);
        } else if (openAppStatus==AnalyticsManager.OPEN_APP_UPGRADE){
             // restart on upgrade
            SchedulerManager.getInstance().restartAll(this, 0);    
        }
        ...
    }

}
```

  * **Notifications Settings**:
advanced Notification settings with optional configurable UI for users

![http://hub.buzzbox.com/sr/images/demo_app_scheduler_conf_1_small.png](http://hub.buzzbox.com/sr/images/demo_app_scheduler_conf_1_small.png)

  * **Integrated Rss Parser**:
the SDK give you the possibility to monitor whatever Rss feed providing you an implemented Rss Parser Task. We are currently using the Google News Rss, but you can use whatever Rss feed you want

  * **Notifications Log**:
you can check if and when yours backgrounds task are running

  * **Notification Types**:
your app can support different type of notifications configurable by the user through the easy and completed UI

![http://hub.buzzbox.com/sr/images/demo_app_scheduler_conf_2_small.png](http://hub.buzzbox.com/sr/images/demo_app_scheduler_conf_2_small.png)


---

The BuzzBox SDK enables you to easily add a scheduler to your App. With few lines of code you can add a background task and app-side notifications. Besides, BuzzBox SDK also include free realtime analytics, so you can monitor in real time how many user are installing and using your App. Notifications and Analytics are tighly integrated, but you can use only want you need.

![http://hub.buzzbox.com/sr/images/buzzbox-sdk-diagram.png](http://hub.buzzbox.com/sr/images/buzzbox-sdk-diagram.png)

Use only what you need, BuzzBox SDK is highly customizable:
  * Create a background task to run whenever you want
  * Include the preference screen to let the user control when to run the task and how often
  * Create Notifications at the end of the task
  * Include Real-time Analytics to track you App usage based on Cohort Curves (something Google Analytics doesn't have)

The developer needs to do very little work to provide value to the user and also increase retention, visits/user and perceived value of the product. The app-side notifications do not require any server side code, so the implementation costs are minimal. The SDK is modularized, so it can be customized by the developer. We have added notifications to over many apps in the Android Marketplace, catch up to what everyone is doing.

List of the apps using the BuzzBox SDK:

http://hub.buzzbox.com/android-sdk/appsusingsdk

See also:

http://www.bestappsmarket.com as an example of an app using BuzzBox SDK