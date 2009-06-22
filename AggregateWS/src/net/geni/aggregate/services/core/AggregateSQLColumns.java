/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.util.Vector;

/**
 *
 * @author jflidr
 */
public class AggregateSQLColumns extends Vector<String>
{

    public AggregateSQLColumns(final String... v) {
        for(int i = 0; i < v.length; i++) {
            add(v[i]);
        }
    }
}
