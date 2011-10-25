/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

/**
 *
 * @author Xi Yang
 */
public class AggregateSlicesPoller extends Thread {
    private volatile int pollInterval = 300000; //300 secs by default
    private volatile boolean goRun = true;
    public void run() {
        while (goRun) {
            try {
                this.sleep(pollInterval);
                //AggregateState.getAggregateSlices().pollSlices();
            } catch (InterruptedException e) {
                if (!goRun) {
                    break;
                }
            }
        }
    }

    public boolean isGoRun() {
        return goRun;
    }

    public void setGoRun(boolean goRun) {
        this.goRun = goRun;
    }

    public int getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(int pollInterval) {
        this.pollInterval = pollInterval;
    }
}
