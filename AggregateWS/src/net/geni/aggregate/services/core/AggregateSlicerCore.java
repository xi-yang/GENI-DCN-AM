/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.util.logging.Level;
import net.geni.aggregate.services.api.AggregateGENISkeleton;

/**
 *
 * @author jflidr
 */
public class AggregateSlicerCore implements Runnable
{

    private boolean axisRunning = true;
    private AggregateSQLStatements sql;

    public AggregateSlicerCore() {
        try {
            AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + 
                    AggregateState.getSlicerTab() + " ( " +
                    "sliceID VARCHAR(255), " + // job ID 1
                    "launchTime BIGINT, " + // launch timestamp
                    "PRIMARY KEY (sliceID)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }

        //initialize the interface
        AggregateState.setSkeletonAPI(new AggregateGENISkeleton());

        sql = AggregateState.getSqlStatements();
    }

    public void run() {

        // before we start make sure that the database is in coherent state
        // TODO: AggregateUtils.checkStateCoherence() - in a central class;

        //main loop
        while(axisRunning) {
            try {


                Thread.sleep(AggregateState.getPollInterval());
            } catch(InterruptedException ex) {
                AggregateState.logger.log(Level.INFO, "user interrupt: terminating ...", ex);
                axisRunning = false;
            }
//            } catch(AggregateException ex) {
//                // try to clean up (no other exceptions should make it all the way to here
//                if(ex.getType() == AggregateException.FATAL) {
//                    AggregateState.logger.log(Level.SEVERE, "FATAL error: terminating ...", ex);
//                    axisRunning = false;
//                }
//            }
        }
    }

    public void stopCore() {
        axisRunning = false;
    }
    
}
