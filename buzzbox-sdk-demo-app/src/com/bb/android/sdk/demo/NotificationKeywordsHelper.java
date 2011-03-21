package com.bb.android.sdk.demo;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bb.android.sdk.demo.model.Topic;
import com.bb.android.sdk.demo.util.FilteringQuery;
import com.buzzbox.mob.android.scheduler.analytics.AnalyticsManager;

public class NotificationKeywordsHelper {
	
	final static String KEYWORDS_PREFS_KEY = "notification.keywords";
	
	public static FilteringQuery getKeywords(ContextWrapper ctx) {
		SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(ctx);
		String k = prefs.getString(KEYWORDS_PREFS_KEY, ""); // tab separated
		if ("".equals(k)) {
			return null;
		}
		else{
			return FilteringQuery.parse(k);
		}
	}
	
	public static String getKeywordsString(ContextWrapper ctx) {
		FilteringQuery fq = getKeywords(ctx);
		if (fq==null) return "";
		StringBuilder sb = new StringBuilder();
		for (Topic s :	fq.getKeywords()){
			if (sb.length()!=0) sb.append(", ");
			sb.append(s.keyword);
		}
		return sb.toString();
	}
	
	public static Button getSetupButton(ContextWrapper ctx) {
		Button followingKeywords = new Button(ctx);
		followingKeywords.setPadding(10, 10, 10, 10);
		followingKeywords.setMinHeight(55);
		followingKeywords.setTextColor(Color.parseColor("#2b2b2b"));
		//followingKeywords.setBackgroundColor(R.drawable.button_transparent_overlay);
		followingKeywords.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_menu_edit, 0);
		followingKeywords.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
		refreshKeywordsButton(ctx,followingKeywords);		
		return followingKeywords;
	}
	
	public static void refreshKeywordsButton(ContextWrapper ctx, Button button) {
		String k = NotificationKeywordsHelper.getKeywordsString(ctx);
		if (k.length()>0) {
			button.setText("Get Notifications For: "+k);
		} else {
			button.setText("Set Up Keywords Notifications");
		}
	}
	
	public static void saveKeywords(ContextWrapper ctx, Collection<Topic> keywords) {
		SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(ctx);
		
		StringBuilder sb =new StringBuilder();
		for (Topic s : keywords){
			if (sb.length()!=0) sb.append("\t");
			sb.append((s.notificationEnabled?"1":"0") + s.keyword);
		}
		
		Editor edit = prefs.edit();
		edit.putString(KEYWORDS_PREFS_KEY, sb.toString());
		edit.commit();
		
		AnalyticsManager.incrementGoal(ctx, "save-keyword");
		
	}
	
	static class KeywordsAdapter extends BaseAdapter {
		final Activity context;
		public KeywordsAdapter(Activity context, List<Topic> items) { 
			this.keywords = items;
			this.context=context; 
		}
		TextView text;
		CheckBox checkbox;
		ImageButton removeButton;
		List<Topic> keywords;
		
		@Override
		public int getCount() {
			return keywords.size();
		}
		@Override
		public Object getItem(int pos) {
			return keywords.get(pos);
		}
		@Override
		public long getItemId(int pos) {
			return pos;
		}
		
		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {
			if (convertView==null)
				convertView = (View) context.getLayoutInflater().inflate(R.layout.keyword_item, parent, false);
		
			text = (TextView) convertView.findViewById(R.id.keyword_text);
			checkbox = (CheckBox) convertView.findViewById(R.id.keyword_checkbox);
			removeButton = (ImageButton) convertView.findViewById(R.id.keyword_remove);
			
			final Topic topic = keywords.get(position);
			
			text.setText(topic.keyword);
			
			checkbox.setChecked(topic.notificationEnabled);
			checkbox.setFocusable(false);
			
			checkbox.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					topic.notificationEnabled = !topic.notificationEnabled;
					Toast.makeText(context, "Notifications for '"+topic.keyword+"' "+(topic.notificationEnabled?"ON":"OFF"), Toast.LENGTH_SHORT).show();
					//saveKeywords(ctx, keywords)
					//notifyDataSetInvalidated();
					notifyDataSetChanged();
				}
			});
			
			removeButton.setFocusable(false);
			
			removeButton.setOnClickListener( new View.OnClickListener() {				
				@Override
				public void onClick(View arg0) {
					keywords.remove(position);
					//saveKeywords(context, keywords);
					//notifyDataSetInvalidated();
					notifyDataSetChanged();
				}
			});
			
			return convertView;
		}
	}
	
	
	public static AlertDialog buildEditDialog(final Activity context, final List<Topic> keywords, final String newKeyword) {
		final View view = context.getLayoutInflater().inflate(R.layout.notification_keywords_edit, null);
		
		final View header = context.getLayoutInflater().inflate(R.layout.notification_keywords_edit_header, null);
		
		final ListView lv = (ListView) view.findViewById(R.id.notification_keywords_edit_list);
		
		final EditText addText = (EditText) view.findViewById(R.id.notification_keywords_edit_add_text);

		final Button addButton = (Button) view.findViewById(R.id.notification_keywords_edit_button);

		lv.addHeaderView(header);
		
		final KeywordsAdapter kAdapter = new KeywordsAdapter(context, keywords);
		kAdapter.registerDataSetObserver( new DataSetObserver() {
			@Override
			public void onInvalidated() {
				saveKeywords(context, keywords);
				super.onInvalidated();
			}
			
			@Override
			public void onChanged() {
				saveKeywords(context, keywords);
				super.onChanged();
			}
		});
		
		addButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String k = addText.getText().toString();
				if (k.length()>0) {
					Topic t = new Topic(k.trim());
					t.notificationEnabled = true;

					if (!keywords.contains(t)) {
						keywords.add(0,t);
					
						Toast.makeText(context, "Keyword '"+t.keyword+"' saved and Notifications ON", Toast.LENGTH_SHORT).show();
	
						kAdapter.notifyDataSetChanged();
					} else {
						Toast.makeText(context, "Keyword '"+t.keyword+"' already in your list", Toast.LENGTH_SHORT).show();
					}
					addText.setText("");
					addText.clearFocus();
					try {
						InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromInputMethod(addText.getWindowToken(), 0);
					} catch (Exception e) {}
				}
			}
		});
		lv.setAdapter(kAdapter);
		lv.setOnItemClickListener( new AdapterView.OnItemClickListener() {
			public void onItemClick(android.widget.AdapterView<?> arg0, View arg1, int pos, long id) {
				pos = pos - 1; // because of the header
				if (pos>=0) {
//					Topic t = keywords.get(pos);
//										
//					Intent i = new Intent(context, StoriesListActivity.class);
//					i.putExtra("storiesVertical" , Vertical.search);
//					i.putExtra("q", t.keyword);
//					i.putExtra("onlyTitle", false);
//					context.startActivity(i);
					// TODO close / dismiss dialog?
				}
			};
		});
			
		final AlertDialog.Builder builder = new AlertDialog.Builder(context)
		.setCancelable(true)
		.setTitle("Edit Keywords")
		.setPositiveButton("Close", null)
		.setView(view);
		
		AlertDialog d = builder.create();
		if (newKeyword!=null) {
			addText.setText(newKeyword);
		}
		
		return d;
	}
	
	private static void addKeyword(final ContextWrapper ctx, final String q){
		Collection<Topic> current = new HashSet<Topic>();
		FilteringQuery fq = NotificationKeywordsHelper.getKeywords(ctx);
		if (fq != null) {			
			for (Topic s : fq.getKeywords()) {
				current.add(s);
			}
		}
		Topic newTopic = new Topic(q);
		newTopic.notificationEnabled = true;
		current.add( newTopic );
		NotificationKeywordsHelper.saveKeywords(ctx, current);

		Toast.makeText(ctx, "'"+q+"' added as a Notification.", Toast.LENGTH_LONG).show();
	}
	
}
