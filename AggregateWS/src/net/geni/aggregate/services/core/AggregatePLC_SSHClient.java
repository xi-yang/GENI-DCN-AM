/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;


import java.util.regex.Pattern;
import java.util.Vector;
import java.util.HashMap;

/**
 *
 * @author Xi Yang
 */
public class AggregatePLC_SSHClient extends AggregateCLIClient {
    private String plcHost;
    private String plcLogin;
    private String sshPort;
    private String sshKeyfile;
    private String sshKeypass;
    private String sshExecPrefix;

    private AggregatePLC_SSHClient() {
        super("[^#]*#\\s");
    }

    public AggregatePLC_SSHClient(String h, String l, String p, String kf, String kp, String ep) {
        super("[^#]*#\\s");
        plcHost = h;
        plcLogin = l;
        sshPort = p;
        sshKeyfile = kf;
        sshKeypass = kp;
        sshExecPrefix = ep;
        setTimeout(5000); //timeout 5 seconds
    }

    static public AggregatePLC_SSHClient getPLCClient() {
        return (new AggregatePLC_SSHClient(AggregateState.getPlcSshHost(), AggregateState.getPlcSshLogin(),
                AggregateState.getPlcSshPort(), AggregateState.getPlcSshKeyfile(),
                AggregateState.getPlcSshKeypass(), AggregateState.getPlcSshExecPrefix()));
    }

    public boolean login() {
        if (!super.login("expect", "-c", "spawn ssh -i "+sshKeyfile + " -l "+plcLogin+" "+plcHost, "-c", "interact"))
            return false;

        readUntil(promptPattern);
        if (buffer.contains("Are you sure")) {
            sendCommand("yes");
            readUntil(promptPattern);
        }
        if (buffer.contains("Enter passphrase for key")) {
            sendCommand("yes");
            readUntil(promptPattern);
        }

        return true;
    }

    public boolean logoff() {
        return super.logoff("exit");
    }

    public boolean vconfigVlan(String node, String iface, String vlan, boolean add) {
        if (!alive()) {
            if (!login())
                return false;
        }

        String cmd = sshExecPrefix + node + " vconfig " + (add ? "add ":"rem ") + iface + (add?" ":".") + vlan;
        this.sendCommand(cmd);
        int ret = this.readPattern(promptPattern, ".*ERROR|.*Invalid", promptPattern);
        if (ret == 1) {
            return true;
        }

        return false;
    }

    public boolean ifconfigIp(String node, String iface, String ipaddr, String netmask) {
        if (!alive()) {
            if (!login())
                return false;
        }

        String cmd = sshExecPrefix + node + " ifconfig " + iface + " " + ipaddr + " netmask " + netmask;
        this.sendCommand(cmd);
        int ret = this.readPattern(promptPattern, ".*No such device|.*Invalid argument", promptPattern);
        if (ret == 1) {
            return true;
        }

        return false;
    }
}
