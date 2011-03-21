package com.bb.android.sdk.demo.task;

import android.content.ContextWrapper;

import com.bb.android.sdk.demo.HelloWorldActivity;
import com.buzzbox.mob.android.scheduler.NotificationMessage;
import com.buzzbox.mob.android.scheduler.Task;
import com.buzzbox.mob.android.scheduler.TaskResult;

// 1. implement a Task
public class HelloWorldTask implements Task{

	@Override
	public String getTitle() {                        
		return "Reminder";
	}

	@Override
	public String getId() {                        
		return "reminder"; // give it an ID
	}

	@Override
	public TaskResult doWork(ContextWrapper ctx) {
		TaskResult res = new TaskResult();

		/*
		 * 2. do some work such as calling your server, calling a third part service or 
		 * whatever you need in order to decide if you need to deliver a notification.
		 */ 

		// 3. create a notification based on your results
		res.addMessage( new NotificationMessage("Hello World",
												"Don't forget to open Hello World App")
												.setNotificationClickIntentClass(HelloWorldActivity.class));    
		return res;
	}

}
