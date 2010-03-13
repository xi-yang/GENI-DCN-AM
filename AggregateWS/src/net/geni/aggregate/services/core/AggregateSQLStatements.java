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

    private AggregateSQLConstraint nodeCapC = new AggregateSQLConstraint("capabilities", "?");
    public AggregateSQLStatement aggCaps_Stmt;
    public AggregateSQLStatement aggNodes_Stmt;
    public AggregateSQLStatement aggSlices_Stmt;
    public AggregateSQLStatement aggUsers_Stmt;
    public AggregateSQLStatement aggP2PVlans_Stmt;

    // statements devinitions
    public AggregateSQLStatements() {
        aggCaps_Stmt = new AggregateSQLStatement(
                AggregateState.getAggregateDB(),
                AggregateState.getCapsTab(),
                new AggregateSQLColumns("name", "urn", "id", "description", "controllerURL"));
        aggNodes_Stmt = new AggregateSQLStatement(
                AggregateState.getAggregateDB(),
                AggregateState.getNodesTab(),
                new AggregateSQLColumns("urn", "id", "description", "capabilities"));
        aggSlices_Stmt = new AggregateSQLStatement(
                AggregateState.getAggregateDB(),
                AggregateState.getSlicesTab(),
                new AggregateSQLColumns("sliceName", "id", "url", "description", "members", "creatorId", "createdTime", "expiredTime"));
        aggP2PVlans_Stmt = new AggregateSQLStatement(
                AggregateState.getAggregateDB(),
                AggregateState.getP2PVlansTab(),
                new AggregateSQLColumns("id", "vlanTag", "sliceId", "source", "destination", "bandwidth", "globalReservationId", "status"));
        aggUsers_Stmt = new AggregateSQLStatement(
                AggregateState.getAggregateDB(),
                AggregateState.getUsersTab(),
                new AggregateSQLColumns("id", "name", "firstName", "lastName", "email", "description"));
    }
}
