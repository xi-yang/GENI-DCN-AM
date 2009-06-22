/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

/**
 *
 * @author jflidr
 */
public class AggregateSQLCombLogic
{

    private String comb = " AND ";

    public AggregateSQLCombLogic() {
        comb = " AND ";
    }

    public AggregateSQLCombLogic(String c) {
        if(c.equalsIgnoreCase("and")) {
            comb = " AND ";
        } else if(c.equalsIgnoreCase("or")) {
            comb = " OR ";
        } else {
            System.err.println("only \"and\" and \"or\" are allowed as arguments. Defaulting to \"AND\"");
        }
    }

    public String getComb() {
        return comb;
    }
}
