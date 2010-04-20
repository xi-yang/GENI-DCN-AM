/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.util.logging.Level;
import net.geni.aggregate.services.api.AggregateGENISkeleton;


/**
 *
 * @author Xi Yang
 */
public class AggregateWSRunner implements Runnable {

    private boolean axisRunning = true;

    public AggregateWSRunner() {

        //initialize the interface
        AggregateState.setSkeletonAPI(new AggregateGENISkeleton());

    }

    public void run() {

        // before we start make sure that the database is in coherent state
        // TODO: AggregateUtils.checkStateCoherence() - in a central class;

        //main loop
        while(axisRunning) {
            try {

                Thread.sleep(AggregateState.getPollInterval());

            } catch(InterruptedException ex) {
                //AggregateState.logger.log(Level.INFO, "user interrupt: terminating ...", ex);
                axisRunning = false;
            }
        }
    }

    public void stop() {
        axisRunning = false;
    }

}
