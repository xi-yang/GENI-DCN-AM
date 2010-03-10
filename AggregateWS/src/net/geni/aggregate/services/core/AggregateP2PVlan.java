/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.util.*;
import java.rmi.RemoteException;
import org.apache.axis2.AxisFault;
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
    String source;
    String destination;
    int vtag;
    float bandwidth;
    String description;
    long startTime;
    long endTime;

    String errMessage;
    /**
    * constructor
    */
    public AggregateP2PVlan(String s, String d, int v, float b, String desc, long st, long et) {
        apiClient = null;
        source = s;
        destination = d;
        vtag = v;
        bandwidth = b;
        description = desc;
        startTime = st;
        endTime = et;
        errMessage = "";
    }

    public String getGlobalReservationId() {
        if (apiClient == null)
            return "";
        return apiClient.getGlobalReservationId();
    }

    public String getErrorMessage() {
        return errMessage;
    }

    /**
     * setup p2p vlan
     * @param
     * @return
     */
    public String setupVlan() {
        if (apiClient == null)
            apiClient = AggregateIDCClient.getIDCClient();
        String status = "failed";
        try {
            status = apiClient.createReservation(source, destination, vtag, bandwidth, description, startTime, endTime);
        }
        catch (AxisFault e) {
            errMessage = "AxisFault from createReservation: " +e.getMessage();
        } catch (AAAFaultMessage e) {
            errMessage = "AAAFaultMessage from createReservation: " +e.getFaultMessage().getMsg();
        } catch (BSSFaultMessage e) {
            errMessage = "BSSFaultMessage from createReservation: " +e.getFaultMessage().getMsg();
        } catch (java.rmi.RemoteException e) {
            errMessage = "RemoteException returned from createReservation: " +e.getMessage();
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
        String status = "failed";
        try {
            status = apiClient.cancelReservation();
        }
        catch (AxisFault e) {
            errMessage = "AxisFault from cancelReservation: " +e.getMessage();
        } catch (AAAFaultMessage e) {
            errMessage = "AAAFaultMessage from cancelReservation: " +e.getFaultMessage().getMsg();
        } catch (BSSFaultMessage e) {
            errMessage = "BSSFaultMessage from cancelReservation: " +e.getFaultMessage().getMsg();
        } catch (java.rmi.RemoteException e) {
            errMessage = "RemoteException returned from cancelReservation: " +e.getMessage();
        } catch (Exception e) {
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
        hmRet.put("status", "failed");
        try {
            hmRet = apiClient.queryReservation();
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
}
