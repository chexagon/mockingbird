package com.crimsonhexagon.mockingbird;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;

public class Launch {

    public static void main(String[] args) {
        System.out.println("MockingbirdClient initializing.");

        MockingbirdClient mock = new MockingbirdClient();

        BlockingQueue<String> Q = new LinkedBlockingDeque<>();
        new Thread(new Reporter(Q)).start();
        mock.start(Q);
    }

    private static class Reporter implements Runnable {

        private final BlockingQueue<String> Q;

        Reporter(BlockingQueue<String> Q) {
            this.Q = Q;
        }

        @Override
        public void run() {
            int count = 0;
            long start = currentTimeMillis();

            while (true) {
                try {
                    String msg = Q.poll(1, TimeUnit.SECONDS);
                    count++;
                    System.out.println(msg);

                    if (msg != null && count % 100 == 0) {
                        long end = currentTimeMillis();
                        System.out.println("Created " + count + " messages. (" + ((end-start) / 1000f) + ")");
                        start = currentTimeMillis();
                    }
                } catch (InterruptedException e) {
                    // no handling; keep trying poll
                }
            }
        }
    }
}
