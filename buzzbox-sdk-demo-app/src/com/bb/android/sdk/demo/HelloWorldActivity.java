package com.bb.android.sdk.demo;

import android.app.Activity;
import android.os.Bundle;

import com.bb.android.sdk.demo.task.HelloWorldTask;
import com.buzzbox.mob.android.scheduler.SchedulerManager;
import com.buzzbox.mob.android.scheduler.analytics.AnalyticsManager;

public class HelloWorldActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 1. call BuzzBox Analytics
		int openAppStatus = AnalyticsManager.onOpenApp(this); 

		// 2. add the Task to the Scheduler
		if (openAppStatus==AnalyticsManager.OPEN_APP_FIRST_TIME) { 
		      // register the Task when the App in installed
		      SchedulerManager.getInstance().saveTask(this, "*/1 * * * *" /* this is a cron string*/, HelloWorldTask.class);
		      SchedulerManager.getInstance().restart(this, HelloWorldTask.class);
		} else if (openAppStatus==AnalyticsManager.OPEN_APP_UPGRADE){
		     // restart on upgrade
		    SchedulerManager.getInstance().restartAll(this);    
		}
	}
}
