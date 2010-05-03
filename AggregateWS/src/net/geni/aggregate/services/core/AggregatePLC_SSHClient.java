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

    public void finalize() {
        logoff();
    }

    public boolean login() {
        if (!super.login("expect", "-c", "spawn ssh -i "+sshKeyfile +" -p "+sshPort+" -l "+plcLogin+" "+plcHost, "-c", "interact"))
            return false;

        readUntil(promptPattern);
        log.debug("PLCSSH login dump#1 " + buffer);
        if (buffer.contains("Are you sure")) {
            sendCommand("yes");
            readUntil(promptPattern);
            log.debug("PLCSSH login dump#2 " + buffer);
        }
        if (buffer.contains("Enter passphrase for key")) {
            sendCommand(sshKeypass);
            readUntil(promptPattern);
            log.debug("PLCSSH login dump#3 " + buffer);
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
        log.debug("vconfigVlan command: "+cmd);
        int ret = this.readPattern(promptPattern, ".*ERROR|.*Invalid", promptPattern);
        log.debug("vconfigVlan result: "+buffer);
        if (ret == 1) {
            return true;
        }

        return false;
    }

    public boolean ifconfigIp(String node, String iface, String ipAndMask) {
        if (!alive()) {
            if (!login())
                return false;
        }
        String ipaddr = ipAndMask.substring(0, ipAndMask.indexOf('/'));
        String netmask = ipAndMask.substring(ipAndMask.indexOf('/')+1, ipAndMask.length());
        if (netmask.matches("^\\d+$")) {
            int suffix = Integer.valueOf(netmask);
            if (suffix < 0 || suffix > 32)
                return false;
            int mask = ~(0x7fffffff >> (suffix-1));
            netmask = "0x"+Integer.toHexString(mask);
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
