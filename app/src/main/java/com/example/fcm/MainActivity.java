package com.example.fcm;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
/*import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;*/
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static com.example.fcm.R.id.txt;

public class MainActivity extends AppCompatActivity {
	private static final String AUTH_KEY = "key=52551582214-68tos4rg27rqdt4f081rjp3dnpetkil7.apps.googleusercontent.com";
	private TextView mTextView;
	private String token;

	public static final String CHANNEL_ID = "my_channel_01";
	public static final String CHANNEL_NAME = "Simplified Coding Notification";
	public static final String CHANNEL_DESCRIPTION = "www.simplifiedcoding.net";
	public static final int NOTIFICATION_ID = 888;

	private FirebaseAuth mAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTextView = findViewById(txt);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			String tmp = "";
			for (String key : bundle.keySet()) {
				Object value = bundle.get(key);
				tmp += key + ": " + value + "\n\n";
			}
			mTextView.setText(tmp);
		}


		/*Intent notifyIntent = new Intent(this, SecondActivity.class);
		// Set the Activity to start in a new, empty task
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		// Create the PendingIntent
		PendingIntent notifyPendingIntent = PendingIntent.getActivity(
				this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
		);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
		builder.setContentIntent(notifyPendingIntent);

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
		notificationManager.notify(NOTIFICATION_ID, builder.build());*/

		/*FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
			@Override
			public void onComplete(@NonNull Task<InstanceIdResult> task) {
				if (!task.isSuccessful()) {
					token = task.getException().getMessage();
					Log.w("FCM TOKEN Failed", task.getException());
				} else {
					token = task.getResult().getToken();
					Log.i("FCM TOKEN", token);
				}
			}
		});*/




		FirebaseMessaging.getInstance().getToken()
				.addOnCompleteListener(new OnCompleteListener<String>() {
					@Override
					public void onComplete(@NonNull Task<String> task) {
						if (!task.isSuccessful()) {
							Log.w(TAG, "Fetching FCM registration token failed", task.getException());
							return;
						}

						// Get new FCM registration token
						token = task.getResult();

						// Log and toast
						//String msg = getString(R.string.msg_token_fmt, token);
						//Log.d(TAG, msg);
						//Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
					}
				});

	}


	public void showToken(View view) {
		mTextView.setText(token);
	}

	public void Authentication(View view) {
		GoogleSignInOptions gso = new GoogleSignInOptions
				.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken("52551582214-68tos4rg27rqdt4f081rjp3dnpetkil7.apps.googleusercontent.com")
				.requestEmail()
				.build();




		mAuth = FirebaseAuth.getInstance();

		FirebaseMessaging.getInstance().getToken()
				.addOnCompleteListener(new OnCompleteListener<String>() {
					@Override
					public void onComplete(@NonNull Task<String> task) {
						if (!task.isSuccessful()) {
							Log.w(TAG, "Fetching FCM registration token failed", task.getException());
							return;
						}

						// Get new FCM registration token
						String token = task.getResult();

						// Log and toast
						//String msg = getString(R.string.msg_token_fmt, token);
						//Log.d(TAG, msg);
						//Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
					}
				});

		/*// Create an Intent for the activity you want to start
		Intent resultIntent = new Intent(this, SecondActivity.class);
		// Create the TaskStackBuilder and add the intent, which inflates the back stack
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addNextIntentWithParentStack(resultIntent);
		// Get the PendingIntent containing the entire back stack
		PendingIntent resultPendingIntent =
				stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
		builder.setContentIntent(resultPendingIntent);

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
		notificationManager.notify(NOTIFICATION_ID, builder.build());*/

	}

	public void subscribe(View view) {
		FirebaseMessaging.getInstance().subscribeToTopic("news");
		mTextView.setText(R.string.subscribed);
	}

	public void unsubscribe(View view) {
		FirebaseMessaging.getInstance().unsubscribeFromTopic("news");
		mTextView.setText(R.string.unsubscribed);
	}

	public void sendToken(View view) {
		sendWithOtherThread("token");
	}

	public void sendTokens(View view) {
		sendWithOtherThread("tokens");
	}

	public void sendTopic(View view) {
		sendWithOtherThread("topic");
	}

	private void sendWithOtherThread(final String type) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				pushNotification(type);
			}
		}).start();
	}

	private void pushNotification(String type) {
		JSONObject jPayload = new JSONObject();
		JSONObject jNotification = new JSONObject();
		JSONObject jData = new JSONObject();
		try {
			jNotification.put("title", "Google I/O 2016");
			jNotification.put("body", "Firebase Cloud Messaging (App)");
			jNotification.put("sound", "default");
			jNotification.put("badge", "1");
			jNotification.put("click_action", "OPEN_ACTIVITY_1");
			jNotification.put("icon", "ic_notification");

			jData.put("picture", "https://miro.medium.com/max/1400/1*QyVPcBbT_jENl8TGblk52w.png");

			switch(type) {
				case "tokens":
					JSONArray ja = new JSONArray();
					ja.put("AAAADDxRggY:APA91bGSyRdcs32aJ8W3lLxkuXKxDr-RHQ9LKwcIhRn6yhmZCx2ax8SP6meX3-Tz3koe22hR57UbiIKtKKFYZ_N-EvkLxo9V-itSQhKXK0U64Z0ICHbmQVOnFnlp6A6hVorvgYV14URi");
					ja.put(token);
					jPayload.put("registration_ids", ja);
					break;
				case "topic":
					jPayload.put("to", "/topics/news");
					break;
				case "condition":
					jPayload.put("condition", "'sport' in topics || 'news' in topics");
					break;
				default:
					jPayload.put("to", token);
			}

			jPayload.put("priority", "high");
			jPayload.put("notification", jNotification);
			jPayload.put("data", jData);

			URL url = new URL("https://fcm.googleapis.com/fcm/send");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", AUTH_KEY);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);

			// Send FCM message content.
			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(jPayload.toString().getBytes());

			// Read FCM response.
			InputStream inputStream = conn.getInputStream();
			final String resp = convertStreamToString(inputStream);

			Handler h = new Handler(Looper.getMainLooper());
			h.post(new Runnable() {
				@Override
				public void run() {
					mTextView.setText(resp);
				}
			});
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}

	private String convertStreamToString(InputStream is) {
		Scanner s = new Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next().replace(",", ",\n") : "";
	}
}