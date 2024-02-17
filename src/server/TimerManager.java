/*
 * This file is part of the OdinMS Maple Story Server
 * Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 * Matthias Butz <matze@odinms.de>
 * Jan Christian Meyer <vimes@odinms.de>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation version 3 as published by
 * the Free Software Foundation. You may not use, modify or distribute
 * this program under any other version of the GNU Affero General Public
 * License.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package server;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class TimerManager implements TimerManagerMBean{

	private static TimerManager instance = new TimerManager();
	private ScheduledThreadPoolExecutor ses;

	private TimerManager(){
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		try{
			mBeanServer.registerMBean(this, new ObjectName("server:type=TimerManger"));
		}catch(Exception e){
			Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
		}
	}

	public static TimerManager getInstance(){
		return instance;
	}

	public void start(){
		if(ses != null && !ses.isShutdown() && !ses.isTerminated()) return;
		ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(4, new ThreadFactory(){

			private final AtomicInteger threadNumber = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r){
				Thread t = new Thread(r);
				t.setName("TimerManager-Worker-" + threadNumber.getAndIncrement());
				return t;
			}
		});
		// this is a no-no, it actually does nothing..then why the fuck are you doing it?
		stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
		stpe.setRemoveOnCancelPolicy(true);
		stpe.setKeepAliveTime(15, TimeUnit.MINUTES);
		// stpe
		ses = stpe;
	}

	public void stop(){
		ses.shutdownNow();
	}

	private final AtomicInteger threadNumber = new AtomicInteger(1);

	public ScheduledFuture<?> register(String name, Runnable r, long period, long initialDelay){
		return ses.scheduleAtFixedRate(new LoggingSaveRunnable(name + "-" + threadNumber.getAndIncrement(), r), initialDelay, period, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> register(String name, Runnable r, long period){
		return ses.scheduleAtFixedRate(new LoggingSaveRunnable(name + "-" + threadNumber.getAndIncrement(), r), 0, period, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> schedule(String name, Runnable r, long delay){
		if(ses == null) return null;
		return ses.schedule(new LoggingSaveRunnable(name + "-" + threadNumber.getAndIncrement(), r), delay, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> scheduleAtTimestamp(String name, Runnable r, long timestamp){
		return ses.schedule(new LoggingSaveRunnable(name + "-" + threadNumber.getAndIncrement(), r), timestamp - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	public void execute(String name, Runnable r){
		ses.execute(new LoggingSaveRunnable(name + "-" + threadNumber.getAndIncrement(), r));
	}

	@Override
	public long getActiveCount(){
		return ses.getActiveCount();
	}

	@Override
	public long getCompletedTaskCount(){
		return ses.getCompletedTaskCount();
	}

	@Override
	public int getQueuedTasks(){
		return ses.getQueue().toArray().length;
	}

	@Override
	public long getTaskCount(){
		return ses.getTaskCount();
	}

	@Override
	public boolean isShutdown(){
		return ses.isShutdown();
	}

	@Override
	public boolean isTerminated(){
		return ses.isTerminated();
	}

	public ScheduledThreadPoolExecutor getExecutor(){
		return ses;
	}

	private static class LoggingSaveRunnable implements Runnable{

		Runnable r;
		String name;

		public LoggingSaveRunnable(String name, Runnable r){
			this.name = name;
			this.r = r;
		}

		@Override
		public void run(){
			try{
				r.run();
			}catch(Throwable t){
				Logger.log(LogType.ERROR, LogFile.EXCEPTION, t);
			}
		}
	}
}
