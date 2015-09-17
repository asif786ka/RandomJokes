package com.randomjokes.app;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Cache.Entry;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.randomjokes.app.controller.JokesController;
import com.randomjokes.app.model.JokesModel;

/**
 * @author Asif
 * This class uses the timer to invoke repeated getRandomJokes().
 * Using Alarm manager along with broadcast reciever is also a good option.
 *
 */
public class RandomJokesActivity extends Activity {

	// Log tag
	private static final String TAG = RandomJokesActivity.class.getSimpleName();

	// Random jokes url
	private static final String RANDOM_JOKES_URL = "http://api.icndb.com/jokes/random";

	private ProgressDialog pDialog;
	private TextView jokesView;
	private JokesModel jokeItem = new JokesModel();

	// private PendingIntent jokePi;
	// private BroadcastReceiver jokeReceiver;
	// private AlarmManager jokeAlarmManager;

	Timer jokeTimer;
	TimerTask jokeTimerTask;
	// we are going to use a handler to be able to run in our TimerTask
	final Handler jokeHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jokes);

		jokesView = (TextView) findViewById(R.id.jokecontent);
		showProgressDialog();

		// We first check for cached request
		Cache cache = JokesController.getInstance().getRequestQueue()
				.getCache();
		Entry entry = cache.get(RANDOM_JOKES_URL);
		if (entry != null) {
			// fetch the data from cache
			try {
				String data = new String(entry.data, "UTF-8");
				try {
					parseJsonFeed(new JSONObject(data));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		} else {
			getRandomJoke();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		startJokeTimer();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		hideProgressDialog();
		stopJokeTimer();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void showProgressDialog() {
		pDialog = new ProgressDialog(this);
		// Showing progress dialog before making http request
		pDialog.setMessage("Loading...");
		pDialog.show();
	}

	private void hideProgressDialog() {
		if (pDialog != null) {
			pDialog.dismiss();
			pDialog = null;
		}
	}

	/**
	 * Parsing json response and passing the random jokes to textview.
	 * */
	private void parseJsonFeed(JSONObject response) {

		JSONObject jokeObj;
		try {
			jokeObj = response.getJSONObject("value");
			jokeItem = new JokesModel();

			jokeItem.setJokeContent(jokeObj.getString("joke"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void getRandomJoke() {
		// making fresh volley request and getting json
		JsonObjectRequest jsonReq = new JsonObjectRequest(Method.GET,
				RANDOM_JOKES_URL, null, new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						hideProgressDialog();
						VolleyLog.d(TAG,
								"Jokes Response: " + response.toString());
						if (response != null) {
							parseJsonFeed(response);
							if (jokeItem.getJokeContent() != null) {
								jokesView.setText(jokeItem.getJokeContent());
							}
							setProgressBarIndeterminateVisibility(false);
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						hideProgressDialog();
						VolleyLog.d(TAG, "Error: " + error.getMessage());
					}

				});

		// Adding request to volley request queue
		JokesController.getInstance().addToRequestQueue(jsonReq);

	}

	// Broadcast Receiver approach.
	/*
	 * private void jokeAlarmSetup() { jokeReceiver = new BroadcastReceiver() {
	 * 
	 * @Override public void onReceive(Context c, Intent i) { getRandomJoke(); }
	 * }; registerReceiver(jokeReceiver, new
	 * IntentFilter("com.randomjokes.app")); jokePi =
	 * PendingIntent.getBroadcast(this, 0, new Intent( "com.randomjokes.app"),
	 * 0); jokeAlarmManager = (AlarmManager) (this
	 * .getSystemService(Context.ALARM_SERVICE)); }
	 */

	// For this test timer is used.
	public void startJokeTimer() {

		// set a new joke Timer

		jokeTimer = new Timer();

		// initialize the TimerTask's job

		initializeTimerTask();

		// schedule the timer, after the first 3000ms the TimerTask will run
		// every 5000ms

		jokeTimer.schedule(jokeTimerTask, 4000, 5000); //

	}

	public void stopJokeTimer() {

		// stop the timer, if it's not already null

		if (jokeTimer != null) {

			jokeTimer.cancel();

			jokeTimer = null;

		}

	}

	public void initializeTimerTask() {

		jokeTimerTask = new TimerTask() {

			public void run() {

				// use a handler to run a toast that shows the current timestamp

				jokeHandler.post(new Runnable() {

					public void run() {

						getRandomJoke();

					}

				});

			}

		};

	}

}
