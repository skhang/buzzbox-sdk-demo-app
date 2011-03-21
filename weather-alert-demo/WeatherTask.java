package com.rf.android.weather;

import org.json.JSONObject;

import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.buzzbox.mob.android.scheduler.NotificationMessage;
import com.buzzbox.mob.android.scheduler.Task;
import com.buzzbox.mob.android.scheduler.TaskResult;
import com.buzzbox.mob.android.scheduler.analytics.HttpUtils;
import com.buzzbox.mob.android.scheduler.analytics.HttpUtils.HttpUtilsResponse;

public class WeatherTask implements Task {

	@Override
	public String getId() {
		return "weather";
	}
	
	@Override
	public String getTitle() {
		return "Current Weather";
	}
	
	@Override
	public TaskResult doWork(ContextWrapper ctx) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		final String zip = prefs.getString("settings.zip", "");
		
		TaskResult res = new TaskResult();
		try {
			HttpUtilsResponse resp = HttpUtils.http(
					ctx, 
					"http://query.yahooapis.com/v1/public/yql?q=select%20item.forecast%20from%20weather.forecast%20where%20location%3D%22"+zip+"%22&format=json", false);

			
			Editor edit = prefs.edit();
			for (int i=0; i<2; i++) {
				String day = i == 0 ? "Today" : "Tomorrow";
				
				JSONObject forecastJson = resp.getJSONObjectResponse()
					.getJSONObject("query") .getJSONObject("results") .getJSONArray("channel").getJSONObject(i) 
					.getJSONObject("item") .getJSONObject("forecast");
				
				
				int lastForecastInt = prefs.getInt("forecast.last."+i+".numtype", 0);
				
				int code = forecastJson.getInt("code");
				String text = forecastJson.getString("text");
				String date = forecastJson.getString("date");
				int typeNum = YahooWeather.getWeatherTypeNum(code);
				
				String lastMessage  =  date +" : "+ text;
							
				edit.putString("forecast.last."+i, lastMessage);
				
				edit.putInt("forecast.last."+i+".numtype", typeNum);
				
				int icon = R.drawable.notification_ok;
				if (typeNum<=-10) {
					icon = R.drawable.notification_thunderstorm;
				} else if (typeNum<=-7) {
					icon = R.drawable.notification_snow;
				} else if (typeNum<=0) {
					icon = R.drawable.notification_rain;
				}
				edit.putInt("forecast.last."+i+".icon", icon);
				
				Log.d("weather", "code ["+code+"] type["+typeNum+"] was ["+lastForecastInt+"] day ["+i+"]");
				
				// bad weather and getting worst OR it was bad and it will be Ok OR first time
				if (typeNum<0 && typeNum<lastForecastInt || lastForecastInt<0 && typeNum>0 || lastForecastInt==0 ) {
					
					res.addMessage( 
							new NotificationMessage(null, 
									"Weather Forecast!", "Bad weather Alert!", day+" "+lastMessage , icon)
					.setNotificationSettings(true, false, false)
					.setNotificationClickIntentClass( MainActivity.class )
					.setNotificationId(7777777+i));
					
				}
			}
			edit.putLong("lastRefesh", System.currentTimeMillis());
			edit.commit();
		} catch (Exception e) {
			Log.e("weather.task", e.getMessage(), e);
		}
		return res;
	}
}
