package com.example.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 *
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 *
 * <intent-filter>
 *   <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

	private static final String TAG = "MyFirebaseMsgService";

	/**
	 * Called when message is received.
	 *
	 * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
	 */
	// [START receive_message]
	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		if (remoteMessage.getData().size() > 0) {
			Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());
			try {
				JSONObject json = new JSONObject(remoteMessage.getData().toString());
				sendPushNotification(json);
			} catch (Exception e) {
				Log.e(TAG, "Exception: " + e.getMessage());
			}
		Log.d(TAG, "From: " + remoteMessage.getFrom());

		// Check if message contains a data payload.
		/*if (remoteMessage.getData().size() > 0) {
			Log.d(TAG, "Message data payload: " + remoteMessage.getData());

			if (*//* Check if data needs to be processed by long running job *//* true) {
				// For long-running tasks (10 seconds or more) use WorkManager.
				scheduleJob();
			} else {
				// Handle message within 10 seconds
				handleNow();
			}
*/
		}

		// Check if message contains a notification payload.
		if (remoteMessage.getNotification() != null) {
			Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
		}

		// Also if you intend on generating your own notifications as a result of a received FCM
		// message, here is where that should be initiated. See sendNotification method below.
	}

	private void sendPushNotification(JSONObject json) {
		//optionally we can display the json into log
		Log.e(TAG, "Notification JSON " + json.toString());
		try {
			//getting the json data
			JSONObject data = json.getJSONObject("data");

			//parsing json data
			String title = data.getString("title");
			String message = data.getString("message");
			String imageUrl = data.getString("image");

			//creating MyNotificationManager object
			MyNotificationManager mNotificationManager = new MyNotificationManager(getApplicationContext());

			//creating an intent for the notification
			Intent intent = new Intent(getApplicationContext(), SecondActivity.class);

			//if there is no image
			if(imageUrl.equals("null")){
				//displaying small notification
				mNotificationManager.showSmallNotification(title, message, intent);
			}else{
				//if there is an image
				//displaying a big notification
				mNotificationManager.showBigNotification(title, message, imageUrl, intent);
			}
		} catch (JSONException e) {
			Log.e(TAG, "Json Exception: " + e.getMessage());
		} catch (Exception e) {
			Log.e(TAG, "Exception: " + e.getMessage());
		}
	}

	// [END receive_message]


	// [START on_new_token]
	/**
	 * There are two scenarios when onNewToken is called:
	 * 1) When a new token is generated on initial app startup
	 * 2) Whenever an existing token is changed
	 * Under #2, there are three scenarios when the existing token is changed:
	 * A) App is restored to a new device
	 * B) User uninstalls/reinstalls the app
	 * C) User clears app data
	 */
	@Override
	public void onNewToken(String token) {
		Log.d(TAG, "Refreshed token: " + token);

		// If you want to send messages to this application instance or
		// manage this apps subscriptions on the server side, send the
		// FCM registration token to your app server.
		sendRegistrationToServer(token);
	}
	// [END on_new_token]

	/**
	 * Schedule async work using WorkManager.
	 */
	private void scheduleJob() {
		// [START dispatch_job]
		/*OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
				.build();
		WorkManager.getInstance(this).beginWith(work).enqueue();*/
		// [END dispatch_job]
	}

	/**
	 * Handle time allotted to BroadcastReceivers.
	 */
	private void handleNow() {
		Log.d(TAG, "Short lived task is done.");
	}

	/**
	 * Persist token to third-party servers.
	 *
	 * Modify this method to associate the user's FCM registration token with any
	 * server-side account maintained by your application.
	 *
	 * @param token The new token.
	 */
	private void sendRegistrationToServer(String token) {
		// TODO: Implement this method to send token to your app server.


	}

	/**
	 * Create and show a simple notification containing the received FCM message.
	 *
	 * @param messageBody FCM message body received.
	 */
	private void sendNotification(String messageBody) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
				PendingIntent.FLAG_ONE_SHOT);

		String channelId = getString(R.string.notification_channel_id);
		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder notificationBuilder =
				new NotificationCompat.Builder(this, channelId)
						.setSmallIcon(R.drawable.ic_notification)
						.setContentTitle(getString(R.string.msg_token_fmt))
						.setContentText(messageBody)
						.setAutoCancel(true)
						.setSound(defaultSoundUri)
						.setContentIntent(pendingIntent);

		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Since android Oreo notification channel is needed.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(channelId,
					"Channel human readable title",
					NotificationManager.IMPORTANCE_DEFAULT);
			notificationManager.createNotificationChannel(channel);
		}

		notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
	}
}