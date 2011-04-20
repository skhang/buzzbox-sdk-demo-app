package com.bb.android.sdk.demo;

import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.bb.android.sdk.demo.task.MyRssReaderTask;
import com.bb.android.sdk.demo.util.FilteringQuery;
import com.bb.android.sdk.demo.util.UIHelper;
import com.buzzbox.mob.android.scheduler.SchedulerManager;
import com.buzzbox.mob.android.scheduler.analytics.AnalyticsManager;
import com.buzzbox.mob.android.scheduler.task.RssReaderTask;
import com.buzzbox.mob.android.scheduler.ui.SchedulerLogActivity;


/*
 * I suggest you to start from the HelloWorldActivity, which is an empty activity 
 * meant to show how to call the analytics and how to schedule a task, it schedules
 * the HelloWorldTask.
 * 
 * The BrowserActivity also shows how to start the scheduler configuration activity
 * and the log activity.  
 */

public class BrowserActivity extends Activity {
    private WebView wv;
    private static final String PREFS_NAME = "smartBrowserPrefs";
    private boolean enableContentRewriting;
    private String lastEnteredURL = "";
    private String secretScriptKey = generateSecretScriptKey();
    
    static final int MENU_GO_TO = 1;
    static final int MENU_RELOAD = 2;
    static final int MENU_SITE_SEARCH = 3;
    static final int MENU_SMART_ACTIONS = 4;
    static final int MENU_SETTINGS = 5;
    static final int MENU_MANAGE_SCRIPTS = 6;
	
    public static final String WEB_VIEW_URL = "http://news.google.com/news/i?pz=1&cf=all&ned=us&ict=imsp";
    private static final String RSS_URL = "http://news.google.com/?output=rss&as_scoring=n";
    	
    SharedPreferences prefs;
     
	LinearLayout toolbarShareBySMS;
	LinearLayout toolbarShareByEmail;
	LinearLayout toolbarSchedulerLog;
	
	LinearLayout toolbarSchedulerConfiguration;
	LinearLayout toolbarEditKeywordsNotification;
	
	AlertDialog editKeywordsDialog = null;
	
	String title;
    String source;
    String url;	
    
    class MyJavaScriptInterface  
    { 
         @SuppressWarnings("unused")  
         public void showHTML(String html)  
         {  
             new AlertDialog.Builder(BrowserActivity.this)  
                 .setTitle("HTML")  
                 .setMessage(html)  
                 .setPositiveButton(android.R.string.ok, null)  
             .setCancelable(false)  
             .create()  
             .show();  
         }  
     }  
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && wv.canGoBack()) {
            wv.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
        getWindow().requestFeature(Window.FEATURE_RIGHT_ICON);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        
        setContentView(R.layout.browser);
        updateTitleIcons();
        
        prefs = getSharedPreferences(PREFS_NAME, 0);
        
        title = getIntent().getStringExtra("title");
        url = getIntent().getStringExtra("url");
        
        RssReaderTask.setRssUrl(this, RSS_URL);
        
        int openAppStatus = AnalyticsManager.onOpenApp(this);
        if (openAppStatus==AnalyticsManager.OPEN_APP_FIRST_TIME) { // very first time
        	UIHelper.openFirstTimePopup(this);
        	SchedulerManager.getInstance().saveTask(this, "*/30 9-20 * * 1,2,3,4,5", MyRssReaderTask.class);
            SchedulerManager.getInstance().restart(this, MyRssReaderTask.class);
        } else if (openAppStatus==AnalyticsManager.OPEN_APP_UPGRADE){
        	UIHelper.openChangelogPopup(this);
        	SchedulerManager.getInstance().restartAll(this); // they need to be rescheduled
        }
        
        // 	toolbar
        this.toolbarSchedulerConfiguration = (LinearLayout) this.findViewById(R.id.toolbar_notification);
        this.toolbarSchedulerConfiguration.setVisibility(View.VISIBLE);
		this.toolbarSchedulerConfiguration.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View arg0) {
				SchedulerManager.getInstance().startConfigurationActivity(BrowserActivity.this, MyRssReaderTask.class);					
			}
		});
		
		this.toolbarEditKeywordsNotification = (LinearLayout) this.findViewById(R.id.toolbar_filter);
		this.toolbarEditKeywordsNotification.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View arg0) {
				FilteringQuery fq = NotificationKeywordsHelper.getKeywords(BrowserActivity.this);
				if (fq == null) fq = new FilteringQuery();
				editKeywordsDialog = NotificationKeywordsHelper.buildEditDialog(BrowserActivity.this, 
						fq.getKeywords(), "");
				editKeywordsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface arg0) {
						/* we do not need to update the text on 
						 * the button as we do in PickSourceActivity */
					}
				});
				editKeywordsDialog.show();
			
			}
		});
		
		this.toolbarShareByEmail = (LinearLayout) this.findViewById(R.id.toolbar_share_email);
		this.toolbarShareByEmail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View arg0) {
				UIHelper.startHtmlEmailIntent(BrowserActivity.this, null, wv.getTitle(), "http://hub.buzzbox.com/", wv.getUrl());
			}
		});
		
		this.toolbarShareBySMS = (LinearLayout) this.findViewById(R.id.toolbar_share_sms);
		this.toolbarShareBySMS.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View arg0) {
				UIHelper.startSMSIntent(BrowserActivity.this, wv.getTitle(), "http://hub.buzzbox.com/", wv.getUrl());
			}
		});
		
		this.toolbarSchedulerLog = (LinearLayout) this.findViewById(R.id.toolbar_not_log);
        this.toolbarSchedulerLog.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View arg0) {
				Intent intent = new Intent(BrowserActivity.this, SchedulerLogActivity.class);
				startActivity(intent);					
			}
		});
		
		enableContentRewriting = prefs.getBoolean("enableContentRewriting", true);
       
        wv = (WebView)findViewById(R.id.browser);
        
        WebSettings websettings = wv.getSettings();
        websettings.setJavaScriptEnabled(true);
        
        /* Enable zooming */
        websettings.setSupportZoom(false);
        websettings.setBuiltInZoomControls(false); 

        websettings.setCacheMode(WebSettings.LOAD_NORMAL);

        
        //websettings.
        
       // wv.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        
        wv.setWebViewClient(new WebViewClient()
        {
            /** 
             * This method is called after a page finishes loading.
             * 
             * It reads all the JS microformat parsers and injects them into the web page which has just 
             * finished loading. This is achieved by calling loadUrl("javascript:<js-code-here>"),
             * which is the exact same method used by bookmarklets.
             */
            @Override
            public void onPageFinished(WebView view, String url)
            {
            	//wv.loadUrl("javascript:(function() { document.getElementsByTagName('img')[0].style.display='none' })()");
            	//wv.loadUrl("javascript:(function() { document.getElementsByTagName('input')[0].style.display='none' })()");
            	super.onPageFinished(view, url);
            }
            
            
            @Override
            public void onLoadResource(WebView view, String url) {
            	//Dbg.debug("loading resources:"+url);
            	//if (url.contains("pagead")) throw new RuntimeException();
            	super.onLoadResource(view, url);
            }
            
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
            	return false;
            }
            
          
        });
        
        wv.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress)
            {
                updateProgress(newProgress);
                super.onProgressChanged(view, newProgress);
            }
            
            @Override
            public void onReceivedTitle(WebView view, String title)
            {
                setTitle(title);
                super.onReceivedTitle(view, title);
            }
        });
        
        if (url==null) {
        	url = WEB_VIEW_URL;
        }
    
        loadWebPage(url);
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
		super.onActivityResult(requestCode, resultCode, data);		
		if (SchedulerManager.SCHEDULER_CONFIG_REQ_CODE == requestCode && data!=null) {
			SchedulerManager.getInstance().handleConfigurationResult(this, data);		
		}
	}
    
    public void loadWebPage(String targetURL)
    {
        if (targetURL == null) {
            return;
        }
        
        /* Fix URL if it doesn't begin with 'http' or 'file:'. 
         * WebView will not load URLs which do not specify protocol. */
        if (targetURL.indexOf("http") != 0 && targetURL.indexOf("file:") != 0) {
            targetURL = "http://" + targetURL;
        }
        
        lastEnteredURL = targetURL;
        setTitle("Loading "+targetURL);
        
        getWebView().loadUrl(targetURL);
        
        //Ads.showAds("adSense", this, null, targetURL);
        
    }
    
	public WebView getWebView()
    {
        return wv;
    }

    
    public boolean getEnableContentRewriting()
    {
        return enableContentRewriting;
    }
    
    public void setEnableContentRewriting(boolean enable)
    {
        enableContentRewriting = enable;
    }
    
    public String getLastEnteredUrl()
    {
        return lastEnteredURL;
    }
    
    public void updateProgress(int progress)
    {
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, progress * 100);
    }
    
    @Override
    protected void onStop()
    {
        super.onStop();
        
        savePreferences();
    }
    
    public void savePreferences()
    {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("enableContentRewriting", enableContentRewriting);
        editor.commit();
    }
    
    /**
     * Updates active/inactive state of icons in the title bar 
     */
    public void updateTitleIcons() {}

    private String generateSecretScriptKey()
    {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Checks if the given string is a valid secret key
     * @param scriptSecretKey A generated key which is available only to installed JS scripts. Prevents other scripts from calling this function.
     * @return true if valid, false otherwise
     */
    public boolean isValidScriptKey(String scriptSecretKey)
    {
        return scriptSecretKey.equals(this.secretScriptKey);
    }
    
    /**
     * Checks if a given file URL looks like it is pointing to an action script file
     * @param url URL of file
     * @return true if URL ends with ".action.js", false otherwise
     */
    boolean looksLikeActionScript(String url)
    {
        if (url.endsWith(".action.js")) {
            return true;
        }
        return false;
    }

    
    /**
     * Indicates whether the specified action can be used as an intent. This
     * method queries the package manager for installed packages that can
     * respond to an intent with the specified action. If no suitable package is
     * found, this method returns false.
     *
     * @param context The application's environment.
     * @param action The Intent action to check for availability.
     * @return True if an Intent with the specified action can be sent and
     *         responded to, false otherwise.
     */
    public boolean isIntentAvailable(String action, String value) 
    {
        try {
            String intentAction = (String)Intent.class.getField(action).get(null);
            Intent i = new Intent(intentAction, Uri.parse(value));
            List<ResolveInfo> list = 
                getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        }
        catch (Exception e) {
            return false;
        }
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return super.onCreateOptionsMenu(menu);
    }
    
    /**
     * Changes enabled/disabled state of menu items
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        return false;
    }
    
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		this.wv.destroy();
		
		if (editKeywordsDialog!=null && editKeywordsDialog.isShowing()) {
			editKeywordsDialog.dismiss();
		}
		
		super.onDestroy();
	}
	
}
