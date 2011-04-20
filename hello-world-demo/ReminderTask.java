package com.buzzbox.demo.helloworld;

import android.content.ContextWrapper;
import android.content.Intent;
import android.os.SystemClock;

import com.buzzbox.mob.android.scheduler.NotificationMessage;
import com.buzzbox.mob.android.scheduler.Task;
import com.buzzbox.mob.android.scheduler.TaskResult;

/**
 * Recurring Task that implements your business logic.
 * The BuzzBox SDK Scheduler will take care of running the doWork method according to
 * the scheduling.   -- for BuzzBox SDK 0.6 --
 * 
 */
public class ReminderTask implements Task {

	@Override
    public String getTitle() {                        
        return "Reminder";
    }
    
    @Override
    public String getId() {                        
        return "reminder"; // give it an ID
    }
    
    static int count=0;
    
    @Override
    public TaskResult doWork(ContextWrapper ctx) {
        TaskResult res = new TaskResult();
         
        SystemClock.sleep(500);
        long now = System.currentTimeMillis();
        
        NotificationMessage notification = new NotificationMessage(
        		"type 1",
        		null,
        		count+") BuzzBox Hello World",
        		"Don't forget to open Hello World App Don't forget to open Hello World App", 
        		0);
        notification.setNotificationIconResource(R.drawable.icon_notification_cards_clubs);
        notification.setNotificationClickIntentClass(HelloWorldActivity.class);

        notification.setBgColor("#55ffffcc"); // <-- set a custom background color, make it 55 transparent
        notification.setFlagResource(R.drawable.tag_blue); // <-- set a custom flag image
        notification.setTime( now - 3600000 * 24 * count - count*5000);
        notification.setNotificationId(1);
        
        /* */
        final Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/html");
		i.putExtra(Intent.EXTRA_EMAIL, new String[]{"emailto@gmail.com"});
		i.putExtra(Intent.EXTRA_SUBJECT, "subject");
		i.putExtra(Intent.EXTRA_TEXT, "text");
		notification.setIntent(i);
        /* */
        
        res.addMessage( notification );    
        
        /* ----------------------------------------------------------------------------------- */
        
        NotificationMessage notification2 = new NotificationMessage(
        		"type 2",
        		null,
        		count+")BuzzBox Hello Office",
        		"Don't forget to open Hello Office App", 
        		0);
        
        notification2.setNotificationIconResource(R.drawable.icon_notification_cards_clubs);
        notification2.setNotificationClickIntentClass(HelloWorldActivity.class);

        notification2.setBgColor("#55ffffcc"); // <-- set a custom background color
        notification2.setFlagResource(R.drawable.tag_red); // <-- set a custom flag image
        notification2.setTime(now-(5 * 60 * 1000 ));
        
        notification2.setNotificationId(2);
        res.addMessage( notification2 );    
        
        
        
        NotificationMessage group = new NotificationMessage(
        		"mail",
        		"You have 2 messages",
        		count+") You have 2 new messages",
        		"Don't forget to open Hello Office App", 
        		0);
        group.setTime(now);
        group.setFlagResource(R.drawable.tag_gold);
        NotificationMessage group1 = new NotificationMessage(
        		"mail",
        		"Hello World",
        		count+") Hello",
        		"How are you doing?", 
        		0);
        group1.setTime(now-1000);
        group.addNotificationElement(group1);
        
        NotificationMessage group2 = new NotificationMessage(
        		"mail",
        		"What's up?",
        		count+") What's up?",
        		"Hey, what's up?", 
        		0);
        group2.setTime(now-2000);
        group.addNotificationElement(group2);
        
        res.addMessage(group);
        
        
        
        NotificationMessage groupSingle = new NotificationMessage(
        		"mail",
        		"You have X messages",
        		count+") You have X new messages",
        		"Don't forget to open Hello Office App", 
        		0);
        groupSingle.setTime(now);
        NotificationMessage groupSingle1 = new NotificationMessage(
        		"mail",
        		"This is the notification",
        		count+") This is the notification",
        		"Hey, what's up?", 
        		0);
        groupSingle1.setTime(now-2000);
        groupSingle.addNotificationElement(groupSingle1);
        res.addMessage(groupSingle);
        
        count++;
        
        return res;
    }
	
}
