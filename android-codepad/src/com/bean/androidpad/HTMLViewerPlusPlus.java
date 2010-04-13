package com.bean.androidpad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author Cosme Zamudio - Android SDK Examples
 * I Grabbed this class from the SDK Examples, i actually made a lot of changes, so it doesnt look like the original one.
 * it has the prettify functionality inside the class, it may look ugly.. but it works fine and its well organized (thats what i think)
 * Note: Im Leaving the original comments, they might come in handy for other users reading the code 
 * 
 * Wraps a WebView widget within an Activity. When launched, it uses the 
 * URI from the intent as the URL to load into the WebView. 
 * It supports all URLs schemes that a standard WebView supports, as well as
 * loading the top level markup using the file scheme.
 * The WebView default settings are used with the exception of normal layout 
 * is set.
 * This activity shows a loading progress bar in the window title and sets
 * the window title to the title of the content.
 *
 */
public class HTMLViewerPlusPlus extends Activity {
    
	/**
     * The WebView that is placed in this Activity
     */
    private WebView mWebView;
    
    /**
     * As the file content is loaded completely into RAM first, set 
     * a limitation on the file size so we don't use too much RAM. If someone
     * wants to load content that is larger than this, then a content
     * provider should be used.
     */
    static final int MAXFILESIZE = 16172;
    static final String LOGTAG = "HTMLViewerPlusPlus";
	private static final int PICK_REQUEST_CODE = 0;
	private boolean isSearcihng = false;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_menu, menu);
    	return true;
    	
    }
    
    /**
     * Modify the menus according to the searching mode and matches
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	if (isSearcihng) {
    		menu.findItem(R.id.next_menu).setEnabled(true);
    		menu.findItem(R.id.clear_menu).setEnabled(true);
    	}
    	else {
    		menu.findItem(R.id.next_menu).setEnabled(false);
    		menu.findItem(R.id.clear_menu).setEnabled(false);    		
    	}
    		
    	
    	return super.onPrepareOptionsMenu(menu);
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId())
    	{
    	case R.id.open_menu:
    		openFileIntent();
    		break;
    	case R.id.search_menu:
    		showSearchDialog();
    		break;
    	case R.id.next_menu:
    		nextSearch();
    		break;
    	case R.id.clear_menu:
    		clearSearch();
    		break;
    	case R.id.select_menu:
    		selectAndCopyText();
    		break;
    	case R.id.home_menu:
    		loadHomeScreen();
    		break;
    	case R.id.help_menu:
    		loadHelpScreen();
    		break;
    	case R.id.quit_menu:
    		quitApplication();
    		break;
    	}
    	return false;
    	
    }
    
    /**
     * Added to avoid refreshing the page on orientation change
     * saw it on stackoverflow, dont remember wich article
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    }
    
    /**
     * Gets the result from the file picker activity
     * thats the only intent im actually calling (and expecting results from) right now
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
	    if (requestCode == PICK_REQUEST_CODE)
	    {
		    if (resultCode == RESULT_OK)
		    {
			    Uri uri = intent.getData();
			    if (uri != null)
			    {
			    	String path = uri.toString();
			    	if (path.toLowerCase().startsWith("file://"))
			    	{
			    		path = (new File(URI.create(path))).getAbsolutePath();
			    		loadFile(Uri.parse(path), "text/html");
			    	}
			    }
		    }
		    else{}
	    }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CookieSyncManager.createInstance(this);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        mWebView = new WebView(this);
        setContentView(mWebView);
        
        // Setup callback support for title and progress bar
        //mWebView.setWebChromeClient( new WebChrome() );
        mWebView.setWebViewClient(new WebChrome2());
        
        // Configure the webview
        WebSettings s = mWebView.getSettings();
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        s.setUseWideViewPort(false);
        s.setAllowFileAccess(true);
        s.setBuiltInZoomControls(true);
        s.setLightTouchEnabled(true);
        s.setLoadsImagesAutomatically(true);
        s.setPluginsEnabled(false);
        s.setSupportZoom(true);
        s.setSupportMultipleWindows(true);
        s.setJavaScriptEnabled(true);
        
        
        // Restore a webview if we are meant to restore
        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            Intent intent = getIntent();
            if (intent.getData() != null) {
                Uri uri = intent.getData();
                if ("file".equals(uri.getScheme())) { //are we opening a file, or some data?
                    loadFile(uri, intent.getType());
                } else {
                    mWebView.loadUrl(intent.getData().toString());
                }
            } else {
            	//Home Screen, Simple explanation
            	mWebView.loadUrl("file:///android_asset/home.html");
            }
        }
    }
    
    /**
     * Get a Document Handler Depending on the filename extension
     * @param filename The filename to retrieve the handler from
     * @return The new document handler
     */
    public DocumentHandler getHandlerByExtension(String filename) {
    	DocumentHandler handler = null;
    	
    	if (filename.endsWith(".java")) handler = new JavaDocumentHandler();
    	if (filename.endsWith(".cpp") || filename.endsWith(".cc")) handler = new CppDocumentHandler();
    	if (filename.endsWith(".c")) handler = new CDocumentHandler();
    	if (filename.endsWith(".html") || filename.endsWith(".htm") || filename.endsWith(".xhtml")) handler = new HtmlDocumentHandler();
    	if (filename.endsWith(".js")) handler = new JavascriptDocumentHandler();
    	if (filename.endsWith(".mxml")) handler = new MxmlDocumentHandler();
    	if (filename.endsWith(".pl")) handler = new PerlDocumentHandler();
    	if (filename.endsWith(".py")) handler = new PythonDocumentHandler();
    	if (filename.endsWith(".rb")) handler = new RubyDocumentHandler();
    	if (filename.endsWith(".xml")) handler = new XmlDocumentHandler();
    	if (filename.endsWith(".css")) handler = new CssDocumentHandler();
    	if (filename.endsWith(".el") || filename.endsWith(".lisp") || filename.endsWith(".scm")) handler = new LispDocumentHandler();
    	if (filename.endsWith(".lua")) handler = new LuaDocumentHandler();
    	if (filename.endsWith(".ml")) handler = new MlDocumentHandler();
    	if (filename.endsWith(".vb") || filename.endsWith(".bas")) handler = new VbDocumentHandler();
    	if (filename.endsWith(".sql")) handler = new SqlDocumentHandler();
    	
    	if (handler == null) handler = new TextDocumentHandler();
    	Log.v(LOGTAG," Handler: " +filename);
    	return handler;
    }
    
    /**
     * Call the intent to open files
     */
    public void openFileIntent() {
    	Intent fileIntent = new Intent(HTMLViewerPlusPlus.this,FileBrowser.class);
    	startActivityForResult(fileIntent, PICK_REQUEST_CODE);
    	
    	/* Next Version Feature, support more explorer intents besides the built in one
    	Intent intent = new Intent();
		Uri startDir = Uri.fromFile(Environment.getExternalStorageDirectory());

		if (isIntentAvailable(this,"vnd.android.cursor.dir/lysesoft.andexplorer.file")) { //AndExplorer
			intent.setAction(Intent.ACTION_PICK);
			intent.setDataAndType(startDir, "vnd.android.cursor.dir/lysesoft.andexplorer.file");
			intent.putExtra("explorer_title", "Select a file");
			intent.putExtra("browser_title_background_color", "440000AA");
			intent.putExtra("browser_title_foreground_color", "FFFFFFFF");
			intent.putExtra("browser_list_background_color", "00000066");
			intent.putExtra("browser_list_fontscale", "120%");
			intent.putExtra("browser_list_layout", "2");
			startActivityForResult(intent, PICK_REQUEST_CODE);    	
		}
		else if (isIntentAvailable(this,"org.openintents.action.PICK_FILE")) { //OIFileManager
			intent.setType("org.openintents.action.PICK_FILE");
			intent.setData(startDir);
			startActivityForResult(intent, PICK_REQUEST_CODE);    	
		} else  {
			Toast.makeText(getApplicationContext(), "No File Manager Detected", 2000).show();
		}
		*/

    }
    /**
     * Function found in the android sdk examples, checks if an action has an intent associated
     * going to be used to check for other filebrowser intents
     * @param context usually this, the context we are working on
     * @param action the action we want to check the intents
     * @return true if there is at least one intent for that action
     */
    private boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
        	packageManager.queryIntentServices(intent, 0);
        
        return list.size() > 0;
    }    
    
    /***
     * Closes the application
     */
    public void quitApplication() {
    	finish();
    }
    
    /**
     * Loads the home screen
     */
    public void loadHomeScreen() {
    	mWebView.getSettings().setUseWideViewPort(false);
    	mWebView.loadUrl("file:///android_asset/home.html");
    }
    
    /**
     * Loads the help screen
     */
    public void loadHelpScreen() {
    	mWebView.getSettings().setUseWideViewPort(false);
    	mWebView.loadUrl("file:///android_asset/home.html"); //Need to change for the help screen
    }
    
    /**
     * Select Text in the webview and automatically sends the selected text to the clipboard
     */
    public void selectAndCopyText() {
        try {
        	KeyEvent shiftPressEvent = new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_SHIFT_LEFT,0,0);
        	shiftPressEvent.dispatch(mWebView);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }  
    
    /**
     * Clear all the matches in the search
     */
    public void clearSearch() {
    	isSearcihng = false;
    	mWebView.clearMatches();
    }
    
    /**
     * Find Next Match in Search
     */
    public void nextSearch() {
    	mWebView.findNext(true);
    }    
        
    /**
     * Search inside the webview
     */
    public void showSearchDialog() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    	alert.setTitle("Find Text");
    	alert.setMessage("Enter text to find:");

    	// Set an EditText view to get user input 
    	final EditText inputText = new EditText(this);
    	alert.setView(inputText);

    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			String value = inputText.getText().toString();
    			isSearcihng = true;
    			mWebView.findAll(value);
    		}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    	    
    		}
    	});

    	alert.show();    	
    }
    
    
    
    /**
     * Load the HTML file into the webview by converting it to a data:
     * URL. If there were any relative URLs, then they will fail as the
     * webview does not allow access to the file:/// scheme for accessing 
     * the local file system, 
     * 
     * Note: Before actually loading the info in webview, i add the prettify libraries to do the syntax highlight
     * also i organize the data where it has to be. works fine now but it needs some work
     * 
     * @param uri file URI pointing to the content to be loaded
     * @param mimeType mimetype provided
     */
    private void loadFile(Uri uri, String mimeType) {
        String path = uri.getPath();
        DocumentHandler handler = getHandlerByExtension(path);
        
        File f = new File(path);
        final long length = f.length();
        if (!f.exists()) {
        	Log.e(LOGTAG, "File doesnt exists: " + path);
            return;
        }
        
        if (handler == null) {
        	Log.e(LOGTAG,"Filetype not supported");
        	Toast.makeText(HTMLViewerPlusPlus.this, "Filetype not supported", 2000);
        	return;
        }
        
        // typecast to int is safe as long as MAXFILESIZE < MAXINT
        byte[] array = new byte[(int)length];
        
        try {
            InputStream is = new FileInputStream(f);
            is.read(array);
            is.close();
        } catch (FileNotFoundException ex) {
            // Checked for file existance already, so this should not happen
        	Log.e(LOGTAG, "Failed to access file: " + path, ex);
            return;
        } catch (IOException ex) {
            // read or close failed
            Log.e(LOGTAG, "Failed to access file: " + path, ex);
            return;
        }
        String contentString = "";
        setTitle("Android CodePad - " + path);
        contentString += "<html><head><title>" + path + "</title>";
        contentString += "<link href='file:///android_asset/prettify.css' rel='stylesheet' type='text/css'/> ";
        contentString += "<script src='file:///android_asset/prettify.js' type='text/javascript'></script> ";
        contentString += handler.getFileScriptFiles();
        contentString +=  "</head><body onload='prettyPrint()'><code class='" + handler.getFilePrettifyClass() + "'>";
        String sourceString = new String(array);
        
        //contentString += sourceString.replace("\n", "<br>");
        contentString += handler.getFileFormattedString(sourceString);
        contentString += "</code> </html> ";
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.loadDataWithBaseURL("file:///android_asset/", contentString, handler.getFileMimeType(), "", "");
        Log.v(LOGTAG, "File Loaded: " + path);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        CookieSyncManager.getInstance().startSync(); 
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        mWebView.saveState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        
        CookieSyncManager.getInstance().stopSync(); 
        mWebView.stopLoading();       
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
    }  
    
}
