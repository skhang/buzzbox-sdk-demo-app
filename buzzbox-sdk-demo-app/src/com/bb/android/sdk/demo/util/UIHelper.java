package com.bb.android.sdk.demo.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;


public class UIHelper {
	
	public static void openFirstTimePopup(final Context context) {
		final String html = "Welcome to the BuzzBox Demo App\n\n" +
			"For more information visit\n http://hub.buzzbox.com//\n" +
			
			"\nPlease send feedback and comments at\n contact@buzzbox.com\n\n" +
			"Thank you!";
		final AlertDialog.Builder builder = new AlertDialog.Builder(context).
		setCancelable(true).
		setTitle("Welcome!").
		setMessage(html).
		setNegativeButton("Close",null);

		final AlertDialog di = builder.create();
		
		di.show();
	}
	public static void openChangelogPopup(final Context context) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(context).
		setCancelable(true).
		setTitle("Changelog").
		setMessage("Thanks for updating the BuzzBox Demo App!\n\n" +
				"1.0\n" +
				"- First Release!\n" +
				"\n" +
				"Please email us with feedback, issues and ideas.\n").
		setNeutralButton("Rate App", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				final Intent intent = new Intent(Intent.ACTION_VIEW);
			    intent.setData(Uri.parse("market://details?id="+context.getPackageName()));
			    context.startActivity(intent);	
			}
		}).
	
		setPositiveButton("Close",null);
		
		final AlertDialog di = builder.create();
		
		di.show();
	}
	public static void startEmailIntent(final Context ctx, 
			final String emailto, 
			final String subject,
			final String text) {
		final Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/html");

		if (emailto != null)
			i.putExtra(Intent.EXTRA_EMAIL, new String[]{emailto});

		i.putExtra(Intent.EXTRA_SUBJECT, subject);

		i.putExtra(Intent.EXTRA_TEXT, text);
		ctx.startActivity(Intent.createChooser(i, "Select email application"));
	}
	
    public static void startHtmlEmailIntent(final Context ctx, final String email, final String subject, final String text, final String url) {
		final Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/html");

		if (email != null)
			i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});

		i.putExtra(Intent.EXTRA_SUBJECT, subject);
		
		boolean useHtmlEmail = true;
		if (useHtmlEmail) {
			final StringBuilder body = new StringBuilder("<br/>");
			body.append(text).append("<br/><br/>").
			append(url).append("<br/><br/>").
			append("<br/><br/>found with BuzzBox Demo App!");
			
			i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(body.toString()));
		} else {
			final StringBuilder body = new StringBuilder("\n");
			body.append(text).append("\n\n").
			append(url).append("\n\n").
			append("\n\nfound with found with BuzzBox Demo App!");
			
			i.putExtra(Intent.EXTRA_TEXT, body.toString());
		}
		ctx.startActivity(Intent.createChooser(i, "Select email application"));
	}

    public static void startMMSIntent(final Context ctx, final String subject, final String description, final String source, final String url) {
		final Intent i = new Intent(Intent.ACTION_SEND);
		final StringBuilder body = new StringBuilder();
		body.append("\n\nRead on:\n ").
		append(url);

		final int spaceLeft = 250 - description.length()+3;
		String toAppend = subject + "\n~ " + description;
		if (toAppend != null && toAppend.length() > spaceLeft)
			toAppend = toAppend.substring(0, spaceLeft)+ "...";
		body.insert(0, toAppend);

		/* TODO: THE SUBJECT DOESNT WORK */
		i.putExtra(Intent.EXTRA_TITLE, subject);
		i.putExtra(Intent.EXTRA_SUBJECT, source);
		//i.putExtra(, this.title);
		i.putExtra("sms_body", body.toString());
		i.setType("image/png");
		ctx.startActivity(Intent.createChooser(i, "Select MMS application"));
	}

	public static void startSMSIntent(final Context ctx, final String subject, final String source, final String url) {
		final StringBuilder body = new StringBuilder();
		if (subject != null && !"".equals(subject.trim()))
			body.append(subject);
		
		body.append("\n\nRead on:\n ").append(url);

		final Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		
		sendIntent.putExtra("sms_body", body.toString());
		sendIntent.setType("vnd.android-dir/mms-sms");
		ctx.startActivity(sendIntent);
	}
}
