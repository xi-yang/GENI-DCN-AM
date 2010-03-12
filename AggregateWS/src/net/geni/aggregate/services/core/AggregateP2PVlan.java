/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.util.Vector;
import java.util.HashMap;
import java.rmi.RemoteException;
import org.apache.axis2.AxisFault;
import org.apache.log4j.*;
import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.Client;
import net.es.oscars.PropHandler;

/**
 *
 * @author xyang
 */
public class AggregateP2PVlan {

    //IDCAPIClient
    AggregateIDCClient apiClient;
    //Reservation parameters
    String gri;
    String sliceName;
    String source;
    String destination;
    int vtag;
    float bandwidth;
    String description;
    long startTime;
    long endTime;
    String errMessage;
    String status;

    private Logger log;
    /**
    * constructor
    */
    public AggregateP2PVlan(String sl, String s, String d, int v, float b, String desc, long st, long et) {
        apiClient = null;
        gri = "";
        sliceName = sl;
        source = s;
        destination = d;
        vtag = v;
        bandwidth = b;
        description = desc;
        startTime = st;
        endTime = et;
        errMessage = "";
        status = "";
        log = Logger.getLogger("net.geni.aggregate");
    }

    public AggregateP2PVlan(int sid, int v, String s, String d, float b, String g, String ss) {
        apiClient = null;
        sliceName = AggregateState.getSliceNameById(sid);
        source = s;
        destination = d;
        vtag = v;
        bandwidth = b;
        gri = g;
        status = ss;
        description = "";
        errMessage = "";
        startTime = 0;
        endTime = 0;
        log = Logger.getLogger("net.geni.aggregate");
    }


    public String getSliceName() {
        return sliceName;
    }

    public String getGlobalReservationId() {
        if (!gri.equals(""))
            return gri;
        if (apiClient == null)
            return "";
        gri = apiClient.getGlobalReservationId();
        return gri;
    }

    public int getVlanTag() {
        return vtag;
    }

    public String getErrorMessage() {
        return errMessage;
    }

    public String getStatus() {
        return status;
    }
    /**
     * setup p2p vlan
     * @param
     * @return
     */
    public String setupVlan() {
        if (apiClient == null)
            apiClient = AggregateIDCClient.getIDCClient();
        else if (!gri.equals("")) {
            status = "failed";
            errMessage = "Error: cannot recreate an existing circuit";
            return status;
        }
        status = "failed";
        try {
            status = apiClient.createReservation(source, destination, vtag, bandwidth, description, startTime, endTime);
            errMessage = "";
            gri = apiClient.getGlobalReservationId();
            this.saveVlanInDB();
        }
        catch (AxisFault e) {
            errMessage = "AxisFault from createReservation: " +e.getMessage();
        } catch (AAAFaultMessage e) {
            errMessage = "AAAFaultMessage from createReservation: " +e.getFaultMessage().getMsg();
        } catch (BSSFaultMessage e) {
            errMessage = "BSSFaultMessage from createReservation: " +e.getFaultMessage().getMsg();
        } catch (java.rmi.RemoteException e) {
            errMessage = "RemoteException returned from createReservation: " +e.getMessage();
        } catch (AggregateException e) {
            errMessage = "AggregateException returned from createReservation: " +e.getMessage();
            this.log.error("Error occurs in AggregateP2PVlan::saveVlanInDB: " + errMessage);
        } catch (Exception e) {
            errMessage = "OSCARSStub threw exception in createReservation: " +e.getMessage();
        }

        return status;
     }

     /**
     * teardown p2p vlan
     * @param
     * @return
     */
     public String teardownVlan() {
        if (apiClient == null)
            apiClient = AggregateIDCClient.getIDCClient();
        status = "failed";
        try {
            status = apiClient.cancelReservation(gri);
            errMessage = "";
        }
        catch (AxisFault e) {
            errMessage = "AxisFault from cancelReservation: " +e.getMessage();
        } catch (AAAFaultMessage e) {
            errMessage = "AAAFaultMessage from cancelReservation: " +e.getFaultMessage().getMsg();
        } catch (BSSFaultMessage e) {
            errMessage = "BSSFaultMessage from cancelReservation: " +e.getFaultMessage().getMsg();
        } catch (java.rmi.RemoteException e) {
            errMessage = "RemoteException returned from cancelReservation: " +e.getMessage();
        }catch (Exception e) {
            errMessage = "OSCARSStub threw exception in cancelReservation: " +e.getMessage();
        }

        return status;
     }

     /**
     * Query p2p vlan status
     * @param
     * @return
     */
     public HashMap queryVlan() {
        if (apiClient == null)
            apiClient = AggregateIDCClient.getIDCClient();
        HashMap hmRet = new HashMap();
        status = "failed";
        hmRet.put("status", status);
        try {
            hmRet = apiClient.queryReservation(gri);
            status = hmRet.get("status").toString();
        }
        catch (AxisFault e) {
            errMessage = "AxisFault from queryReservation: " +e.getMessage();
        } catch (AAAFaultMessage e) {
            errMessage = "AAAFaultMessage from queryReservation: " +e.getFaultMessage().getMsg();
        } catch (BSSFaultMessage e) {
            errMessage = "BSSFaultMessage from queryReservation: " +e.getFaultMessage().getMsg();
        } catch (java.rmi.RemoteException e) {
            errMessage = "RemoteException returned from queryReservation: " +e.getMessage();
        } catch (Exception e) {
            errMessage = "OSCARSStub threw exception in queryReservation: " +e.getMessage();
        }

        return hmRet;
     }

    public void saveVlanInDB()
        throws AggregateException {
        String sqlStmt = "INSERT INTO " + AggregateState.getP2PVlansTab() +
                " (vlanTag, sliceId, source, destination, bandwidth, globalReservationId, status) " +
                "VALUES ( '" + Integer.toString(vtag) + "'," +
                " '" + Integer.toString(AggregateState.getSliceIdByName(sliceName)) +"'," +
                " '" + source + "'," +
                " '" + destination + "'," +
                " '" + Float.toString(bandwidth) + "'," +
                " '" + gri + "'," +
                " '" + status + "')";
        //this.log.info("SQL command: " + sqlStmt);
        AggregateUtils.executeDirectStatement(sqlStmt);
    }

    public void deleteVlanFromDB()
        throws AggregateException {
        String sqlStmt = "DELETE FROM " + AggregateState.getP2PVlansTab() +
                " WHERE sliceId='" + Integer.toString(AggregateState.getSliceIdByName(sliceName)) +
                "' and vlanTag='" + Integer.toString(vtag) + "'";
        //this.log.info("SQL command: " + sqlStmt);
        AggregateUtils.executeDirectStatement(sqlStmt);
    }
}