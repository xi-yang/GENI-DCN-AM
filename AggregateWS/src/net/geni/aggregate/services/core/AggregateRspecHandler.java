/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;


/**
 *
 * @author xyang
 */
public interface AggregateRspecHandler {
    AggregateRspec parseRspecXml(String rspecXml) throws AggregateException;
    AggregateRspec configRspecFromFile(String filePath) throws AggregateException;
    String generateRspecManifest(AggregateRspec rspec) throws AggregateException;
}
