package com.bb.android.sdk.demo;

import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout.LayoutParams;

import com.buzzbox.mob.android.scheduler.SchedulerManager;
import com.buzzbox.mob.android.scheduler.task.RssReaderTask;

public class RssTaskPreferenceActivity extends Activity {
	
	ScrollView scrollView; 
	LinearLayout rssPreferenceView;
	
	TextView rssUrlEditTextLbl;
	EditText rssUrlEditText;
	
	Button saveBtn;
	
	Spinner spinnerFeedsList;
	TextView spinnerTitleTextView;
	
	TextView rssUrlTextView;
	
	static final String[] feedsLabel = {"Select one...","TechCrunch","Ars Technica","Mashable","NPR","ESPN","Financial Times"};
	static final HashMap<String, String[]> feedsUrl = new HashMap<String, String[]>();
	static {
		feedsUrl.put(feedsLabel[0], new String[]{"Select one...","0"});
		feedsUrl.put(feedsLabel[1], new String[]{"http://feeds.feedburner.com/TechCrunch","1"});
		feedsUrl.put(feedsLabel[2], new String[]{"http://feeds.arstechnica.com/arstechnica/everything","2"});
		feedsUrl.put(feedsLabel[3], new String[]{"http://feeds.mashable.com/mashable","3"});
		feedsUrl.put(feedsLabel[4], new String[]{"http://www.npr.org/rss/rss.php?id=1002","4"});		
		feedsUrl.put(feedsLabel[5], new String[]{"http://sports-ak.espn.go.com/espn/rss/news","5"});		
		feedsUrl.put(feedsLabel[6], new String[]{"http://www.ft.com/rss/home/us","6"});
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		scrollView = new ScrollView(this);
		scrollView.setBackgroundColor(Color.WHITE);
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		rssPreferenceView = new LinearLayout(this);
		rssPreferenceView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		rssPreferenceView.setVisibility(View.VISIBLE);
		rssPreferenceView.setOrientation(LinearLayout.VERTICAL);

		LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(10, 10, 10, 10);
		
		rssUrlEditTextLbl = new TextView(this);
		rssUrlEditTextLbl.setTextColor(Color.BLACK);
		rssUrlEditTextLbl.setTextSize(18);
		rssUrlEditTextLbl.setText("Type an rss url:");
		rssUrlEditTextLbl.setLayoutParams(layoutParams);
		
		rssPreferenceView.addView(rssUrlEditTextLbl);
		
		rssUrlEditText = new EditText(this);
		rssUrlEditText.setLayoutParams(layoutParams);
		rssUrlEditText.setText("http://");
		
		rssPreferenceView.addView(rssUrlEditText);
		
		saveBtn = new Button(this);
		saveBtn.setLayoutParams(layoutParams);
		saveBtn.setText("Save");
		
		rssPreferenceView.addView(saveBtn);
		
		spinnerTitleTextView = new TextView(this);
		spinnerTitleTextView.setTextSize(18);
		spinnerTitleTextView.setTextColor(Color.BLACK);
		spinnerTitleTextView.setLayoutParams(layoutParams);
		spinnerTitleTextView.setText("or pick one from the list:");
		
		rssPreferenceView.addView(spinnerTitleTextView);
	
		ArrayAdapter<String> arrAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, feedsLabel);
		
		spinnerFeedsList = new Spinner(this);
		spinnerFeedsList.setLayoutParams(layoutParams);
		spinnerFeedsList.setAdapter(arrAdapter);
		
		rssPreferenceView.addView(spinnerFeedsList);
		
		saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final String newRssUrl = rssUrlEditText.getText().toString();
				if (newRssUrl == null || "".equals(newRssUrl.trim()) || "http://".equals(newRssUrl.trim())) {
					Toast.makeText(RssTaskPreferenceActivity.this, "You must type an URL!", Toast.LENGTH_SHORT).show();
					return;
				}
				spinnerFeedsList.setSelection(0);
				saveRssUrlAndRunTask(newRssUrl);
				
			}
		});
		
		final String rssUrl = RssReaderTask.getRssUrl(this);
		if (rssUrl != null && !"".equals(rssUrl.trim())) {
			Iterator<String> iter = feedsUrl.keySet().iterator();
			boolean flagSpinnerSelection = false;
			while (iter.hasNext()){
				String[] temp = feedsUrl.get(iter.next());
				if (rssUrl.equals(temp[0])) {
					flagSpinnerSelection = true;
					spinnerFeedsList.setSelection(Integer.parseInt(temp[1]));
					break;
				}
			}
			if (!flagSpinnerSelection)
				rssUrlEditText.setText(rssUrl);
		}
		
		rssUrlTextView = new TextView(this);
		rssUrlTextView.setTextColor(Color.BLACK);
		rssUrlTextView.setTextSize(16);
		rssUrlTextView.setLayoutParams(layoutParams);
		
		if (rssUrl == null || "".equals(rssUrl))
			rssUrlTextView.setText("You haven't saved any url yet.");
		else
			rssUrlTextView.setText("Currently you are checking the following rss:\n\n"+rssUrl);
			
		rssPreferenceView.addView(rssUrlTextView);
		
		spinnerFeedsList.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		    	String newRssUrl = feedsUrl.get(spinnerFeedsList.getSelectedItem().toString())[0];
				if (newRssUrl == null || "".equals(newRssUrl.trim())) {
					Toast.makeText(RssTaskPreferenceActivity.this, "You must type an URL!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if ("Select one...".equals(newRssUrl)) return;
				
				rssUrlEditText.setText("http://");
				saveRssUrlAndRunTask(newRssUrl);				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}

		});
		
		scrollView.addView(rssPreferenceView);
		setContentView(scrollView);
	}

	private void saveRssUrlAndRunTask(final String newRssUrl) {
		Toast.makeText(RssTaskPreferenceActivity.this, "Rss Url saved!", Toast.LENGTH_SHORT).show();
		RssReaderTask.setRssUrl(RssTaskPreferenceActivity.this, newRssUrl);
		rssUrlTextView.setText("You will get notifications from:\n"+RssReaderTask.getRssUrl(RssTaskPreferenceActivity.this));
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.e("RssTask", "Running the task in background!");
				SchedulerManager.getInstance().runNow(RssTaskPreferenceActivity.this, RssReaderTask.class, 0);
			}
		}).start();
	}

}
