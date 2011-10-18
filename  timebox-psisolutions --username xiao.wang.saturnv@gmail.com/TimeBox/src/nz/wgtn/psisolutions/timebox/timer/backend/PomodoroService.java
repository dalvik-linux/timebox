package nz.wgtn.psisolutions.timebox.timer.backend;

import nz.wgtn.psisolutions.timebox.Debug;
import nz.wgtn.psisolutions.timebox.R;
import nz.wgtn.psisolutions.timebox.preferences.Preferences;
import nz.wgtn.psisolutions.timebox.presets.backend.PomodoroPreset;
import nz.wgtn.psisolutions.timebox.timer.gui.TimerActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

public class PomodoroService extends Service {

	public static final String TAG = "timebox.PomoService";

	// notification stuff
	private NotificationManager notificationManager;
	private final static int NOTIFICATION_ID = 0x101CA75;
	private final static int POMODORO_START = 0xB00B1E5;
	private final static int POMODORO_PAUSE = 0x1337CAFE;
	private final static int POMODORO_RESUME = 0xB3A7135;
	private final static int POMODORO_TICK = 0;
	private final static int POMODORO_STATE_CHANGE = 1;

	private Notification lastNotification;

	private PomodoroPreset preset;
	private PomodoroTimer pTimer;

	// to update gui
	// private Handler mHandler;

	// private int NOTIF_ID = R.string.p;

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new PomoBinder();

	public class PomoBinder extends Binder {
		public PomodoroService getService() {
			return PomodoroService.this;
		}

		public void setPreset(PomodoroPreset preset) {
			PomodoroService.this.preset = preset;
		}

		/**
		 * Starts the current Pomodoro timer.
		 */
		public void start() {
			if (pTimer == null)
				pTimer = new PomodoroTimer(preset, new ServiceCallback());
			pTimer.start();
		}

		/**
		 * Pause the current Pomodoro timer.
		 */
		public void pause() {
			pTimer.pause();
		}

		/**
		 * Resume the current Pomodoro timer
		 */
		public void resume() {
			pTimer.resume();
		}

		/**
		 * Cancels the current Pomodoro timer
		 */
		public void cancel() {
			pTimer.cancel();
			notificationManager.cancel(NOTIFICATION_ID);
			Debug.i(TAG, "Pomodoro cancelled...");
		}

		/**
		 * Return the Pomodoro timer
		 * @return PomodoroTimer
		 */
		public PomodoroTimer getTimer() {
			return pTimer;
		}
	}

	@Override
	public IBinder onBind(Intent i) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Debug.i(TAG, "PomoService created...");
	}

	// Notifying all the things.
	private void doNotify(int type, PomodoroTimer.TimerState state, int minutes) {
		if(state == PomodoroTimer.TimerState.NONE)//timer cancelled
			return;

		int icon = 0;
		int ledColour = 0;

		CharSequence tickerText = null, contentTitle = null, contentText = null;
		long when = System.currentTimeMillis();

		if (minutes > 1)
			contentText = String.format(
					getString(R.string.notification_remaining_plural), minutes);
		else
			contentText = getString(R.string.notification_remaining_less_one);

		boolean alert = false;

		//choose content title text and icon
		switch (state) {
		case WORK:
			contentTitle = preset.getPresetName();
			if (type == POMODORO_PAUSE)
				icon = R.drawable.notify_pause_work;
			else
				icon = R.drawable.notify_play_work;
			break;
		case BREAK:
			contentTitle = preset.getPresetName() + " ("
			+ getString(R.string.pomodoro_break) + ")";
			if (type == POMODORO_PAUSE)
				icon = R.drawable.notify_pause_break;
			else
				icon = R.drawable.notify_play_break;
			break;
		case EX_BREAK:
			contentTitle = getString(R.string.pomodoro_ex_break);
			if (type == POMODORO_PAUSE)
				icon = R.drawable.notify_pause_ex_break;
			else
				icon = R.drawable.notify_play_ex_break;
			break;
		default: //timer has been cancelled
			return;
		}

		switch (type) {
		case POMODORO_START:
			tickerText = getString(R.string.notification_start_ticker);

			break;

		case POMODORO_PAUSE:
			tickerText = getString(R.string.notification_pause_ticker);
			contentText = getString(R.string.pomodoro_pause);

			break;
		case POMODORO_RESUME:
			tickerText = getString(R.string.notification_resume_ticker);

			break;
		case POMODORO_TICK:

			break;
		case POMODORO_STATE_CHANGE:

			switch (state) {
			case WORK:
				alert = true;
				ledColour = getResources().getColor(R.color.color_work);
				tickerText = getString(R.string.notification_break_finish);
				break;
			case BREAK:
				alert = true;
				ledColour = getResources().getColor(R.color.color_break);
				tickerText = getString(R.string.notification_break_start);
				break;
			case EX_BREAK:
				ledColour = getResources().getColor(R.color.color_ex_break);
				tickerText = getString(R.string.notification_ex_break_start);
				break;
			}

			break;
		}

		if (lastNotification == null) {
			lastNotification = new Notification(icon, tickerText, when);
			lastNotification.flags |= Notification.FLAG_ONGOING_EVENT;
			lastNotification.ledOnMS = 100;
			lastNotification.ledOffMS = 100;
		} else {
			if (icon != 0)
				lastNotification.icon = icon;
			if (tickerText != null)
				lastNotification.tickerText = tickerText;
			lastNotification.when = when;			
		}
		lastNotification.ledARGB = ledColour;
		if(alert){

			lastNotification.defaults |= Notification.DEFAULT_VIBRATE;
			//play alarm sound
			if(!Preferences.isSilent()){
				try{ //Try catch - removes error only present on emulator as the emulator contains no sound files.
					boolean forceAudio = Preferences.isForceAudio();
					String ringtoneUri = Preferences.getAlertTone();
					Debug.d(TAG, "Playing alert tone: " + ringtoneUri);
					Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(ringtoneUri));
					if(forceAudio)
						ringtone.setStreamType(AudioManager.STREAM_ALARM);
					else

						ringtone.setStreamType(AudioManager.STREAM_NOTIFICATION);


					ringtone.play();
				}catch(NullPointerException e){
					Debug.e(TAG, "No default ringtone found for notification");
				}
			}
		} else{
			lastNotification.ledARGB = 0;
			lastNotification.defaults &= ~Notification.DEFAULT_VIBRATE;
		}

		// if(type == POMODORO_TICK)
		// notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

		Context context = getApplicationContext();

		Intent notificationIntent = new Intent(this, TimerActivity.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		lastNotification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		notificationManager.notify(NOTIFICATION_ID, lastNotification);
		Debug.d(TAG, "doNotify() ... Notification posted.");
	}

	private class ServiceCallback implements PomodoroTimerCallback {

		int timeRemaining;

		@Override
		public void onTimerStateChanged(PomodoroTimer timer) {
			updateTimeRemaining(timer);
			doNotify(POMODORO_STATE_CHANGE, timer.getState(), timeRemaining);
		}

		@Override
		public void onTimerTicked(PomodoroTimer timer) {
			if (updateTimeRemaining(timer))
				doNotify(POMODORO_TICK, timer.getState(), timeRemaining);
		}

		@Override
		public void onStart(PomodoroTimer timer) {
			updateTimeRemaining(timer);
			doNotify(POMODORO_START, timer.getState(), timeRemaining);
		}

		@Override
		public void onPause(PomodoroTimer timer) {
			updateTimeRemaining(timer);
			doNotify(POMODORO_PAUSE, timer.getState(), timeRemaining);
		}

		@Override
		public void onResume(PomodoroTimer timer) {
			updateTimeRemaining(timer);
			doNotify(POMODORO_RESUME, timer.getState(), timeRemaining);
		}

		@Override
		public void onCancel(PomodoroTimer timer) {}

		boolean updateTimeRemaining(PomodoroTimer timer) {
			int minutes = timer.getMinutesRemaining();
			int seconds = timer.getSecondsRemaining();
			if (seconds != 0)
				minutes++;
			if (minutes == timeRemaining)
				return false;
			else {
				timeRemaining = minutes;
				return true;
			}
		}
	}
}
