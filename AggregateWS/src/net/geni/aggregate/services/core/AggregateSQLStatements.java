/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

/**
 *
 * @author jflidr
 */
public class AggregateSQLStatements
{

    public AggregateSQLStatement aggCaps_SelStmt;
    public AggregateSQLStatement aggNodes_SelStmt;
    public AggregateSQLStatement coreCreate_Slice_InsStmt;

    // statements devinitions
    public AggregateSQLStatements() {
        // load aggregate config statemets
        aggCaps_SelStmt = new AggregateSQLStatement(
                AggregateState.getAggregateDB(),
                AggregateState.getCapsTab(),
                new AggregateSQLColumns("name", "urn", "id", "description", "controllerURL"));
        aggNodes_SelStmt = new AggregateSQLStatement(
                AggregateState.getAggregateDB(),
                AggregateState.getNodesTab(),
                new AggregateSQLColumns("urn", "id", "description", "capabilities"));
        // core table statements
        coreCreate_Slice_InsStmt = new AggregateSQLStatement(
                AggregateState.getAggregateDB(),
                AggregateState.getNodesTab(),
                new AggregateSQLColumns("sliceID", "startTime", "endTime", "createTime", "status", "statusMsg"),
                new AggregateSQLValues("?", "?", "?", "?", "?", "?"));
    }
}
