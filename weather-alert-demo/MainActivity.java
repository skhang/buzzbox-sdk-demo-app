package com.rf.android.weather;

import com.buzzbox.mob.android.scheduler.SchedulerManager;
import com.buzzbox.mob.android.scheduler.analytics.AnalyticsManager;
import com.buzzbox.mob.android.scheduler.ui.SchedulerLogActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
	
	TextView today;
	TextView tomorrow;
	TextView lastRefreshView;
	Button locationButton;
	SharedPreferences prefs;
	 
	public void refreshUi(){
		today.setText( prefs.getString("forecast.last.0", "N/A"));
		today.setCompoundDrawablesWithIntrinsicBounds(prefs.getInt("forecast.last.0.icon", 0), 0, 0, 0);
		
		
        tomorrow.setText( prefs.getString("forecast.last.1", "N/A"));
        tomorrow.setCompoundDrawablesWithIntrinsicBounds(prefs.getInt("forecast.last.1.icon", 0), 0, 0, 0);
        
        Long lastRef = prefs.getLong("lastRefesh", 0);
        if (lastRef>0){
        	lastRefreshView.setVisibility(View.VISIBLE);
        	StringBuilder sb = new StringBuilder();
			com.rf.android.util.StringUtils.calculateRelativeTime(sb,System.currentTimeMillis(), lastRef, false);
			lastRefreshView.setText("Last refresh: "+sb.toString());
        } else {
        	lastRefreshView.setVisibility(View.GONE);
        }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		refreshUi();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        int openAppStatus = AnalyticsManager.onOpenApp(this);
        if (openAppStatus==AnalyticsManager.OPEN_APP_FIRST_TIME) { // very first time
            SchedulerManager.getInstance()
            	.saveTask(this, "0 8-20/2 * * 1,2,3,4,5,6,7", WeatherTask.class);
            SchedulerManager.getInstance()
            	.restart(this, WeatherTask.class);
            openFirstTimePopup();
            	
        } else if (openAppStatus==AnalyticsManager.OPEN_APP_UPGRADE){
            SchedulerManager.getInstance()
            	.restartAll(this, 0); // they need to be rescheduled
        }
        
        lastRefreshView = (TextView) findViewById(R.id.lastRefreshed);
        today = (TextView) findViewById(R.id.todayForecast);
        tomorrow = (TextView) findViewById(R.id.tomorrowForecast);
               
        //String lastForecast =  prefs.getString("forecast.last", "N/A");
        
        final String zip = prefs.getString("settings.zip", "");
        
        locationButton = (Button) findViewById(R.id.location);
        
        if (!"".equals(zip))
        	locationButton.setText("zip: "+zip);
        
        locationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final View view = getLayoutInflater().inflate(R.layout.location_dialog, null);
				
				final EditText zipText = (EditText)view.findViewById(R.id.location_zip);
				zipText.setText( zip );
				
				final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
				.setCancelable(true)
				.setTitle("Change Location")
				.setPositiveButton("Save", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						String locationZip = zipText.getText().toString().trim();
						
						Editor edit = prefs.edit();
						edit.putString("settings.zip", locationZip);
						
						edit.commit();
						
						locationButton.setText("zip: "+locationZip);
						SchedulerManager.getInstance().runNow(MainActivity.this, WeatherTask.class, 50);
					}
				})
				.setCancelable(false)
				.setNegativeButton("Cancel", null)
				.setView(view);
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
        
        Button settingsButton = (Button) findViewById(R.id.settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SchedulerManager.getInstance()
	        	.startConfigurationActivity(MainActivity.this, WeatherTask.class);
			}
		});
        
        Button log = (Button) findViewById(R.id.log);
        log.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SchedulerLogActivity.class);
				startActivity(intent);
			}
		});    
        
        Button refresh = (Button) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SchedulerManager.getInstance().runNow(MainActivity.this, WeatherTask.class, 50);
			}
		}); 
        
        /*
        // create a notification just for the hell of it
        Notification notification = new Notification(R.drawable.scheduler_notification_2, "Test Custum Layout", System.currentTimeMillis());

        View notificationContainer = createNotificationView(); 
        RemoteViews contentView = new RemoteViews(getPackageName(), notificationContainer.getId()); // mmm
        
        //contentView.setImageViewResource(R.id.image, R.drawable.notification_image);
        contentView.setTextViewText(991, "Hello Notifications!");
        contentView.setTextViewText(992, "Hello, this message is in a custom expanded view");
        notification.contentView = contentView;
        
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(123, notification);
        */
		this.registerReceiver(this.receiverUpdateStart, new IntentFilter(SchedulerManager.INTENT_UPDATE_START+WeatherTask.class.getName()));
		this.registerReceiver(this.receiverUpdateEnd, new IntentFilter(SchedulerManager.INTENT_UPDATE_END+WeatherTask.class.getName()));
    }
    
    private final BroadcastReceiver receiverUpdateEnd=new BroadcastReceiver() {
    	public void onReceive(android.content.Context arg0, Intent arg1) {
    		refreshUi();
    		setProgressBarIndeterminateVisibility(false);
    	};
    };
    private final BroadcastReceiver receiverUpdateStart=new BroadcastReceiver() {
    	public void onReceive(android.content.Context arg0, Intent arg1) {
    		setProgressBarIndeterminateVisibility(true);
    		lastRefreshView.setVisibility(View.VISIBLE);
    		lastRefreshView.setText("(refreshing)");
    	};
    };
    
    
    private View createNotificationView(){
		LinearLayout layout = new LinearLayout(this);
		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params1.gravity = Gravity.CENTER_HORIZONTAL;
		params1.weight  = 1.0f;

	

		layout.setPadding(15, 5, 10, 5);
		layout.setOrientation(LinearLayout.VERTICAL);

		TextView text1 = new TextView(this);
		text1.setId(991);
		text1.setText(getTitle());
		text1.setTextSize(18);
		text1.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		text1.setGravity(Gravity.CENTER_HORIZONTAL);
		text1.setLayoutParams(params1);
		text1.setTextColor(Color.parseColor("#FABE53"));
		text1.setText("Powered by BuzzBox SDK");

		TextView text2 = new TextView(this);
		text2.setId(992);
		text2.setText(getTitle());
		text2.setTextSize(14);
		text2.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
		text2.setGravity(Gravity.CENTER_HORIZONTAL);
		text2.setTextColor(Color.parseColor("#53B9FA"));
		text2.setLayoutParams(params1);

		text2.setText("Android Developer? Add BuzzBox SDK to your App for free. Click to learn more.");

		layout.addView(text1);
		layout.addView(text2);

		layout.setId(999);
		return layout;
    }
    
	public void openFirstTimePopup() {
		final String html = "Welcome to Weather Alerts!\n\n" +
			"You don't need to check the weather every day: you just need to know if the weather is gonna change.\n\n" +
			"Weather Alert check the weather for you and notifies you if rain, snow, thunderstorms, hail.. are coming.\n"+
			"If the forecast are getting worst (from light rain to thunderstorm) you may get a notification too.\n" +
			"After days of rain, you will be notified if finally the forecast says that tomorrow you'll see the sun.\n\n" +
			"To start: \n" +
			"1) enter your Zip code\n" +
			"2) change your notificaition settings\n" +
			"3) enjoy your weather notifications :)\n\n" +
			"This is the very first release: let me know how I can make it better.\n" +			
			"Thank you!\n\n";
		final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this).
		setCancelable(true).
		setTitle("Welcome!").
		setMessage(html).
		/*
		setPositiveButton("Email Us",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				UIHelper.startEmailIntent(context, "siliconapps@gmail.com", "Best Apps - feedback", "");
			}
		}).
		*/
		setNegativeButton("Select Zip",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				MainActivity.this.locationButton.performClick();
			}
		});

		final AlertDialog di = builder.create();
		
		di.show();
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {        
        super.onActivityResult(requestCode, resultCode, data);        
        if (SchedulerManager.SCHEDULER_CONFIG_REQ_CODE == requestCode && data!=null) {
            SchedulerManager.getInstance()
            	.handleConfigurationResult(this, data);        
        }
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	this.unregisterReceiver(this.receiverUpdateStart);
		this.unregisterReceiver(this.receiverUpdateEnd);
    }
}