package com.buzzbox.demo.helloworld;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.buzzbox.mob.android.scheduler.SchedulerManager;
import com.buzzbox.mob.android.scheduler.analytics.AnalyticsManager;
import com.buzzbox.mob.android.scheduler.ui.NotificationHistoryActivity;
import com.buzzbox.mob.android.scheduler.ui.SchedulerLogActivity;
import com.buzzbox.mob.android.scheduler.ui.TimeUtils;

// BuzzBox SDK 0.6

public class HelloWorldActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);

		Bundle b = getIntent().getExtras();
		if (b!=null) {
			String toastMessage = b.getString("toast");			
			if (toastMessage!=null) {
				Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
				getIntent().removeExtra("toast");
			}
		}
		// 1. call BuzzBox Analytics
		int openAppStatus = AnalyticsManager.onOpenApp(this); 

		// 2. add the Task to the Scheduler
		if (openAppStatus==AnalyticsManager.OPEN_APP_FIRST_TIME) { 
		      // register the Task when the App in installed
		      SchedulerManager.getInstance().saveTask(this, 
		      		"0 */5 * * *",   // a cron string
		      		ReminderTask.class);
		      SchedulerManager.getInstance().restart(this, ReminderTask.class );
		} else if (openAppStatus==AnalyticsManager.OPEN_APP_UPGRADE){
		     // restart on upgrade
		    SchedulerManager.getInstance().restartAll(this);    
		}
		
		
		// 3. set up UI buttons
        Button settingsButton = (Button) findViewById(R.id.settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SchedulerManager.getInstance()
	        	.startConfigurationActivity(HelloWorldActivity.this, ReminderTask.class);
			}
		});
        
        Button log = (Button) findViewById(R.id.log);
        log.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HelloWorldActivity.this, SchedulerLogActivity.class);
				startActivity(intent);
			}
		});   
        
        Button refresh = (Button) findViewById(R.id.notify);
        refresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SchedulerManager.getInstance().runNow(HelloWorldActivity.this, ReminderTask.class, 0);
				//createCustomNotification();
			}
		}); 
        
        Button history = (Button) findViewById(R.id.notifications_history);
        history.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HelloWorldActivity.this, NotificationHistoryActivity.class);			
				intent.putExtra("taskClass", ReminderTask.class);
				intent.putExtra("windowTitle", "Demo App Notification History");
				
				startActivity(intent);
			}
		});
        
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {        
        super.onActivityResult(requestCode, resultCode, data);        
        if (SchedulerManager.SCHEDULER_CONFIG_REQ_CODE == requestCode && data!=null) {
            SchedulerManager.getInstance()
            	.handleConfigurationResult(this, data);        
        }
    }
    
    static String toTime(long time, Context ctx) {
    	String format = "h:mm a";
        if (android.text.format.DateFormat.is24HourFormat(ctx)) {
        	format = "HH:mm";
        }
        return new SimpleDateFormat(format).format(time);
    }
    
    void createCustomNotification(){
    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
    	
		
		final Notification notification = new Notification(
				R.drawable.icon_notification_cards_clubs, 
				"Custom Notification Layout",
				System.currentTimeMillis());	
		
    	RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.buzzbox_notification);
    	contentView.setImageViewResource(R.id.icon, R.drawable.icon_notification_cards_clubs);
    	contentView.setTextViewText(R.id.title, "Subject: Hello");
    	contentView.setTextViewText(R.id.text, "Hello, this message is in a custom expanded view");
    	contentView.setTextViewText(R.id.time, TimeUtils.formatTime(System.currentTimeMillis(), this));
    	notification.contentView = contentView;

    	Intent notificationIntent = new Intent(this, HelloWorldActivity.class);
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    	notification.contentIntent = contentIntent;
    	
    	
    	
    	mNotificationManager.notify(526536326, notification);
    }
    
}
