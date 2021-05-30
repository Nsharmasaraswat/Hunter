package com.gtp.hunter.common.util;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PeriodicIntervalExecutorService extends ScheduledThreadPoolExecutor {
	private final ExecutorService	reexecutor	= Executors.newSingleThreadExecutor();
	private Calendar				start		= Calendar.getInstance();
	private Calendar				end			= Calendar.getInstance();
	private long					period;

	/**
	 * @param startHour
	 * @param endHour
	 * @param periodInMinutes
	 */
	public PeriodicIntervalExecutorService(int startHour, int endHour, int periodInMinutes) {
		super(1);
		Calendar now = Calendar.getInstance();

		if (now.get(Calendar.HOUR_OF_DAY) >= startHour) {//start next day
			start.add(Calendar.DAY_OF_YEAR, 1);
			end.add(Calendar.DAY_OF_YEAR, 1);
		}
		start.set(Calendar.HOUR_OF_DAY, startHour);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		end.set(Calendar.HOUR_OF_DAY, endHour);
		end.set(Calendar.MINUTE, 0);
		end.set(Calendar.SECOND, 0);
		this.period = periodInMinutes * 60 * 1000;
	}

	public void execute(Runnable command) {
		ScheduledFuture<?> future = scheduleWithFixedDelay(command, start.getTimeInMillis() - Calendar.getInstance().getTimeInMillis(), period, TimeUnit.MILLISECONDS);

		schedule(() -> {
			future.cancel(false);
			reexecutor.execute(() -> {
				start.add(Calendar.DAY_OF_YEAR, 1);
				end.add(Calendar.DAY_OF_YEAR, 1);
				this.execute(command);
			});
		}, end.getTimeInMillis() - Calendar.getInstance().getTimeInMillis(), TimeUnit.MILLISECONDS);
	}

	public static void main(String... args) {
		PeriodicIntervalExecutorService poc = new PeriodicIntervalExecutorService(20, 21, 1);
		poc.execute(() -> {
			System.out.println("BEEP");
		});
	}
}
