package com.dyslexiasupport.ui.reader;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.dyslexiasupport.R;
import com.dyslexiasupport.ui.home.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Foreground service for reading text aloud with playback controls and media notifications.
 */
public class ReaderForegroundService extends Service {

    public static final String CHANNEL_ID = "reader_tts_channel";
    public static final int NOTIFICATION_ID = 1001;

    public static final String ACTION_START = "com.dyslexiasupport.action.START";
    public static final String ACTION_PLAY_PAUSE = "com.dyslexiasupport.action.PLAY_PAUSE";
    public static final String ACTION_PREV = "com.dyslexiasupport.action.PREV";
    public static final String ACTION_NEXT = "com.dyslexiasupport.action.NEXT";
    public static final String ACTION_STOP = "com.dyslexiasupport.action.STOP";

    public static final String EXTRA_TEXT_TITLE = "extra_text_title";
    public static final String EXTRA_TEXT_BODY = "extra_text_body";

    public interface ReadAloudListener {
        void onStateChanged(boolean isPlaying, int sentenceIndex, float speedRate);
        void onSentenceHighlighted(int sentenceIndex, int startOffset, int endOffset);
        void onWordHighlighted(int wordStartOffset, int wordEndOffset);
    }

    public class LocalBinder extends Binder {
        public ReaderForegroundService getService() {
            return ReaderForegroundService.this;
        }
    }

    private final IBinder binder = new LocalBinder();
    private TextToSpeech tts;
    private boolean ttsReady;
    private MediaSessionCompat mediaSession;

    private String textTitle = "";
    private String textBody = "";
    private List<SentenceSplitter.Sentence> sentences = new ArrayList<>();
    private int currentSentenceIndex = 0;
    private boolean isPlaying = false;
    private float speedRate = 1.0f;

    @Nullable
    private ReadAloudListener listener;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        mediaSession = new MediaSessionCompat(this, "ReaderMediaSession");

        tts = new TextToSpeech(getApplicationContext(), status -> {
            ttsReady = status == TextToSpeech.SUCCESS;
            if (ttsReady) {
                setupTTSListener();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                String title = intent.getStringExtra(EXTRA_TEXT_TITLE);
                String body = intent.getStringExtra(EXTRA_TEXT_BODY);
                if (title != null) textTitle = title;
                if (body != null) {
                    textBody = body;
                    sentences = SentenceSplitter.split(textBody);
                    currentSentenceIndex = 0;
                }
                play();
            } else if (ACTION_PLAY_PAUSE.equals(action)) {
                if (isPlaying) {
                    pause();
                } else {
                    play();
                }
            } else if (ACTION_PREV.equals(action)) {
                previousSentence();
            } else if (ACTION_NEXT.equals(action)) {
                nextSentence();
            } else if (ACTION_STOP.equals(action)) {
                stopSelfService();
            }
        }
        return START_NOT_STICKY;
    }

    public void setListener(@Nullable ReadAloudListener listener) {
        this.listener = listener;
        if (listener != null) {
            listener.onStateChanged(isPlaying, currentSentenceIndex, speedRate);
            if (currentSentenceIndex < sentences.size()) {
                SentenceSplitter.Sentence s = sentences.get(currentSentenceIndex);
                listener.onSentenceHighlighted(s.index, s.start, s.end);
            }
        }
    }

    public void initText(String title, String body) {
        this.textTitle = title;
        this.textBody = body;
        this.sentences = SentenceSplitter.split(textBody);
        this.currentSentenceIndex = 0;
    }

    public void play() {
        if (sentences.isEmpty()) {
            return;
        }
        isPlaying = true;
        startForeground(NOTIFICATION_ID, buildNotification());
        speakCurrentSentence();
        notifyState();
    }

    public void pause() {
        isPlaying = false;
        if (tts != null) {
            tts.stop();
        }
        startForeground(NOTIFICATION_ID, buildNotification());
        notifyState();
    }

    public void stopSelfService() {
        isPlaying = false;
        if (tts != null) {
            tts.stop();
        }
        if (listener != null) {
            listener.onStateChanged(false, currentSentenceIndex, speedRate);
        }
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    public void nextSentence() {
        if (currentSentenceIndex < sentences.size() - 1) {
            currentSentenceIndex++;
            if (isPlaying) {
                speakCurrentSentence();
            } else {
                notifySentence();
            }
            notifyState();
            startForeground(NOTIFICATION_ID, buildNotification());
        }
    }

    public void previousSentence() {
        if (currentSentenceIndex > 0) {
            currentSentenceIndex--;
            if (isPlaying) {
                speakCurrentSentence();
            } else {
                notifySentence();
            }
            notifyState();
            startForeground(NOTIFICATION_ID, buildNotification());
        }
    }

    public void setSpeedRate(float rate) {
        this.speedRate = rate;
        if (ttsReady && tts != null) {
            tts.setSpeechRate(rate);
        }
        if (isPlaying) {
            speakCurrentSentence();
        }
        notifyState();
    }

    public float getSpeedRate() {
        return speedRate;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public int getCurrentSentenceIndex() {
        return currentSentenceIndex;
    }

    private void speakCurrentSentence() {
        if (!ttsReady || tts == null || currentSentenceIndex >= sentences.size()) {
            return;
        }

        SentenceSplitter.Sentence sentence = sentences.get(currentSentenceIndex);
        boolean isBn = BanglaText.containsBangla(sentence.text);
        Locale locale = Locale.forLanguageTag(isBn ? "bn-BD" : "en-US");
        tts.setLanguage(locale);
        tts.setSpeechRate(speedRate);

        String utteranceId = "sentence_" + currentSentenceIndex;
        tts.speak(sentence.text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        notifySentence();
    }

    private void setupTTSListener() {
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                isPlaying = true;
                notifyState();
            }

            @Override
            public void onDone(String utteranceId) {
                if (isPlaying) {
                    if (currentSentenceIndex < sentences.size() - 1) {
                        currentSentenceIndex++;
                        speakCurrentSentence();
                        startForeground(NOTIFICATION_ID, buildNotification());
                    } else {
                        isPlaying = false;
                        notifyState();
                        startForeground(NOTIFICATION_ID, buildNotification());
                    }
                }
            }

            @Override
            public void onError(String utteranceId) {
                isPlaying = false;
                notifyState();
            }

            @Override
            public void onRangeStart(String utteranceId, int start, int end, int frame) {
                if (listener != null && currentSentenceIndex < sentences.size()) {
                    SentenceSplitter.Sentence s = sentences.get(currentSentenceIndex);
                    listener.onWordHighlighted(s.start + start, s.start + end);
                }
            }
        });
    }

    private void notifyState() {
        if (listener != null) {
            listener.onStateChanged(isPlaying, currentSentenceIndex, speedRate);
        }
    }

    private void notifySentence() {
        if (listener != null && currentSentenceIndex < sentences.size()) {
            SentenceSplitter.Sentence s = sentences.get(currentSentenceIndex);
            listener.onSentenceHighlighted(s.index, s.start, s.end);
        }
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        String currentText = currentSentenceIndex < sentences.size()
                ? sentences.get(currentSentenceIndex).text
                : textTitle;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(textTitle)
                .setContentText(currentText)
                .setContentIntent(contentIntent)
                .setOngoing(isPlaying)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2));

        // Action 0: Prev
        Intent prevIntent = new Intent(this, ReaderForegroundService.class).setAction(ACTION_PREV);
        PendingIntent prevPending = PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(android.R.drawable.ic_media_previous, "Previous", prevPending);

        // Action 1: Play/Pause
        Intent playIntent = new Intent(this, ReaderForegroundService.class).setAction(ACTION_PLAY_PAUSE);
        PendingIntent playPending = PendingIntent.getService(this, 2, playIntent, PendingIntent.FLAG_IMMUTABLE);
        int playIcon = isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
        String playTitle = isPlaying ? "Pause" : "Play";
        builder.addAction(playIcon, playTitle, playPending);

        // Action 2: Next
        Intent nextIntent = new Intent(this, ReaderForegroundService.class).setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(android.R.drawable.ic_media_next, "Next", nextPending);

        // Action 3: Stop
        Intent stopIntent = new Intent(this, ReaderForegroundService.class).setAction(ACTION_STOP);
        PendingIntent stopPending = PendingIntent.getService(this, 4, stopIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPending);

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Reader Read Aloud",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows controls when text is being read aloud.");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (mediaSession != null) {
            mediaSession.release();
        }
    }
}
