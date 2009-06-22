/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

/**
 *
 * @author jflidr
 */
public class AggregateException extends java.lang.Exception
{

    public final static int INFO = 0;
    public final static int FLOW = 1; // should cause change in exec. flow control
    public final static int WARNING = 2; // non-critical problem
    public final static int ERROR = 6; // locally-critical problem
    public final static int FATAL = 7; // will cause system clean-up and restart
    private int type = INFO;

    public AggregateException(String msg) {
        super(msg);
    }

    public AggregateException(String msg, int t) {
        super(msg);
        type = t;
    }

    public AggregateException(Throwable ex, int t) {
        super(ex);
        type = t;
    }

    public AggregateException(Throwable ex) {
        super(ex);
    }

    public int getType() {
        return type;
    }
}
