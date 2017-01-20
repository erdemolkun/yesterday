package com.protel.network;

import android.support.annotation.NonNull;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Created by erdemmac on 18/05/15.
 */
public class ProtelExecuterService extends ThreadPoolExecutor {
    private static final int DEFAULT_THREAD_COUNT = 2;

    private static ProtelExecuterService protelExecuterService;

    ProtelExecuterService() {
        super(DEFAULT_THREAD_COUNT, DEFAULT_THREAD_COUNT, 1, TimeUnit.MINUTES,
                new PriorityBlockingQueue<Runnable>(), new ProtelThreadFactory());
    }

    public static ProtelExecuterService get() {
        if (protelExecuterService == null) {
            protelExecuterService = new ProtelExecuterService();
        }
        return protelExecuterService;
    }

    @NonNull
    @Override
    public Future<?> submit(Runnable task) {
        ProtelFutureTask ftask = new ProtelFutureTask(task);
        execute(ftask);
        return ftask;
    }

    private static class ProtelThreadFactory implements ThreadFactory {
        @SuppressWarnings("NullableProblems")
        public Thread newThread(Runnable r) {
            return new ProtelThread(r);
        }
    }

    private static class ProtelThread extends Thread {
        public ProtelThread(Runnable r) {
            super(r);
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
            super.run();
        }
    }

    private static final class ProtelFutureTask extends FutureTask<Runnable>
            implements Comparable<ProtelFutureTask> {

        public ProtelFutureTask(Runnable runnable) {
            super(runnable, null);
        }

        @Override
        public int compareTo(@NonNull ProtelFutureTask other) {
            return 0;
        }
    }
}
