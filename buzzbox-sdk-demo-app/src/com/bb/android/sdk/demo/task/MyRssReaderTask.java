package com.bb.android.sdk.demo.task;

import android.content.ContextWrapper;
import android.os.Bundle;
import android.util.Log;

import com.bb.android.sdk.demo.BrowserActivity;
import com.bb.android.sdk.demo.NotificationKeywordsHelper;
import com.bb.android.sdk.demo.R;
import com.bb.android.sdk.demo.util.FilteringQuery;
import com.buzzbox.mob.android.scheduler.NotificationMessage;
import com.buzzbox.mob.android.scheduler.GenericRssParser.SingleResult;
import com.buzzbox.mob.android.scheduler.task.RssReaderTask;

/*
 * This task is using an pre-implemented task called "RssReaderTask" included in the BuzzBox SDK.
 * You can use it if you need to monitor an rss feed.
 * As far as a standard task goes, you can check out the HelloWorldTask, it shows you 
 * the basic steps for creating a task.
 */

public class MyRssReaderTask extends RssReaderTask {
	
	public static final int ALL_NOTIFICATION_ID = 149192333;
	public static final int KEYWORD_NOTIFICATION_ID = 149192334;
	
	@Override
	public Bundle getBundle(boolean isFromNotification, String url, int resultsCount) {
		Bundle b = new Bundle();
		b.putString("url", url);
		return b;
	}

	@Override
	public Class getIntentClass() {
		return BrowserActivity.class;
	}
	
	@Override
	public void createNotificationMessages(ContextWrapper ctx) {
		if (parsedResults != null) {
			if (parsedResults.size()==0) {
				Log.i(LOG_TAG, "no new stories");
			} else {
				final String feedLink = myXMLHandler.getResult().link; // it is not used now, but you have it available
				final String feedTitle = myXMLHandler.getResult().title;
				
				FilteringQuery fq = NotificationKeywordsHelper.getKeywords(ctx);
				
				String storyUrl = "";
				int keywordStoriesCount = 0;
				StringBuilder keywords = new StringBuilder();
				for (int i=0; i< parsedResults.size(); i++){
					SingleResult item = parsedResults.get(i);
					
					if (fq != null) {
						FilteringQuery.FilteringQueryResult fqr = FilteringQuery.processText(item.title, fq, null);
						if (fqr.isNotification()){
							storyUrl = item.link;
							keywordStoriesCount++;
							keywords.append(fqr.getKeywordsFound() + " ");
						}
					}
				}
				
				res.addMessage(new NotificationMessage("all", "New stories from " + feedTitle, feedTitle +": you've got " + parsedResults.size() + " new Stories", 
																						parsedResults.get(0).title, R.drawable.notification_icon)				
					.setNotificationClickIntentBundle(getBundle(true, (parsedResults.size() == 1 ? storyUrl : BrowserActivity.WEB_VIEW_URL), 1))
					.setNotificationId(ALL_NOTIFICATION_ID)
					.setNotificationClickIntentClass(BrowserActivity.class));
				
				if (keywordStoriesCount > 0){
					res.addMessage(new NotificationMessage("keyword", "New stories from " + feedTitle, feedTitle +": you've got " + keywordStoriesCount + " new Stories", 
																						"Keywords: " + keywords,    R.drawable.notification_icon)				
					.setNotificationClickIntentBundle(getBundle(true, (keywordStoriesCount == 1 ? storyUrl : BrowserActivity.WEB_VIEW_URL), 1))
					.setNotificationId(KEYWORD_NOTIFICATION_ID)
					.setNotificationClickIntentClass(BrowserActivity.class));
				}
				
			}
		}
	}
	
}
