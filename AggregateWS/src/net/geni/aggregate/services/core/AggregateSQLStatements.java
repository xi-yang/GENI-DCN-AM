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

    private AggregateSQLConstraint instIDC = new AggregateSQLConstraint("instanceId", "?");
    public AggregateSQLStatement aggCaps_Stmt;

    // statements devinitions
    public AggregateSQLStatements() {
        aggCaps_Stmt = new AggregateSQLStatement(
                AggregateState.getAggregateDB(),
                AggregateState.getCapsTab(),
                new AggregateSQLColumns("name", "urn", "id", "description", "controllerURL"));
    }
}
