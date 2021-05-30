package com.gtp.hunter.structure.websocket;

import android.os.Handler;

import com.gtp.hunter.BuildConfig;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import timber.log.Timber;

public abstract class WebSocketWrapper extends WebSocketListener {
    //    private static final int GOING_AWAY = 1001;
    private final AtomicInteger sessionCounter = new AtomicInteger();

    private int sessionId;
    private long initialDelay;
    private long pingInterval;

    private boolean paused;
    private boolean online;

    // WebSocket
    private WebSocket webSocket;

    private String name;
    private Handler timeoutHandler;
    private Runnable timedOutRunnable;
    private Timer resetTimer;
    private long pingInit = 0L;

    public WebSocketWrapper initialize(WebSocket ws, Handler timeout, String name, long initial, long interval) {
        this.name = name;
        this.timeoutHandler = timeout;
        this.webSocket = ws;
        this.initialDelay = initial;
        this.pingInterval = interval;
        this.sessionId = sessionCounter.incrementAndGet();
        return this;
    }

    public void start() {
        online = true;
        pingInit = sendPing(initialDelay);
        if (BuildConfig.DEBUG)
            Timber.d("%s PINGER %d STARTED!", name, sessionId);
    }

    public void pause() {
        paused = true;
        timeoutHandler.removeCallbacks(timedOutRunnable);
    }

    public void resume() {
        paused = false;
        sendPing(initialDelay);
    }

    public void keepAlive(long delay) {
        online = true;
        reset(delay);
    }

    public void reset(long delay) {
        timeoutHandler.removeCallbacks(timedOutRunnable);
        if (BuildConfig.DEBUG)
            Timber.d("%s PINGER %d Duration: %d Reset: %d Paused: %s Online: %s", name, sessionId, (System.currentTimeMillis() - pingInit), delay, paused, online);
        setupReset(delay);
    }

    private void setupReset(long delay) {
        if (resetTimer != null) {
            resetTimer.cancel();
            resetTimer = null;
        }
        pingInit = System.currentTimeMillis();
        resetTimer = new Timer();
        resetTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                pingInit = sendPing(pingInterval);
                if (resetTimer != null)
                    resetTimer.cancel();
                resetTimer = null;
            }
        }, delay);
    }

    public void stop(String reason) {
        if (BuildConfig.DEBUG)
            Timber.d("%s PINGER %d STOPPED: %s Paused %s Online %s", name, sessionId, reason, paused, online);
        if (timeoutHandler != null)
            timeoutHandler.removeCallbacks(timedOutRunnable);
        online = false;
    }

    private long sendPing(long delay) {
        if (!paused && timeoutHandler != null && webSocket != null) {
            timeoutHandler.removeCallbacks(timedOutRunnable);
            timedOutRunnable = () -> {
                if (!paused) //webSocket.close(GOING_AWAY, "Restarting Connection");
                    Timber.e("%s PINGER should reconnect", name);
                if (webSocket != null) {
                    stop("Listener Timeout");
                    setupReset(delay);
                }
            };
            timeoutHandler.postDelayed(timedOutRunnable, delay);
            webSocket.send("PING");
        } else if (webSocket == null) {
            Timber.d("%s PINGER %d WebSocket is NULL", name, sessionId);
            online = false;
        } else if (BuildConfig.DEBUG)
            Timber.d("%s PINGER %d Not Sending Ping", name, sessionId);
        return System.currentTimeMillis();
    }

    public void sendString(String msg) {
        if (webSocket != null) {
            reset((msg.length() < Byte.MAX_VALUE * 1024) ? pingInterval : msg.length() * 2);
            webSocket.send(msg);
        } else
            online = false;
    }

    public boolean close(int code, String reason) {
        stop(reason);
        return webSocket.close(code, reason);
    }

    public boolean isOnline() {
        return online;
    }

    public void destroy() {
        if (this.webSocket != null)
            this.webSocket.cancel();
        this.webSocket = null;
        if (this.timeoutHandler != null && this.timedOutRunnable != null)
            timeoutHandler.removeCallbacks(timedOutRunnable);
    }
}
