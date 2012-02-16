/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

/**
 *
 * @author xyang
 */
public class RspecHandler_GENIv3 implements AggregateRspecHandler {
    public AggregateRspec parseRspecXml(String rspecXml) throws AggregateException {
        AggregateRspec rspec = new AggregateRspec();
        
        return rspec;
    }

    public AggregateRspec configRspecFromFile(String filePath) throws AggregateException {
        AggregateRspec rspec = new AggregateRspec();
        
        return rspec;
    }

    public String getRspecManifest(AggregateRspec rspec) throws AggregateException {
        String rspecMan = "";

        return rspecMan;
    }
}
