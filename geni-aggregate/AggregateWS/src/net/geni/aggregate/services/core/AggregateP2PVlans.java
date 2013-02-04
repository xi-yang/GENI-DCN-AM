/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.util.*;
import org.hibernate.*;
import org.apache.log4j.*;

/**
 *
 * @author Xi Yang
 */
public class AggregateP2PVlans {
    private static Session session;
    private static org.hibernate.Transaction tx;
    private org.apache.log4j.Logger log;

    public AggregateP2PVlans() {
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public boolean add(AggregateP2PVlan p2pv) {
        synchronized(this) {
            if (!(p2pv.getSliceName() == null)) {
                try {
                    session = HibernateUtil.getSessionFactory().openSession();
                    tx = session.beginTransaction();
                    session.save(p2pv);
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    e.printStackTrace();
                    return false;
                } finally {
                    if (session.isOpen()) session.close();
                }
            } else {
                throw new IllegalArgumentException("the P2PVlan object must be associated with a valid sliceName");
            }
        }
        return true;
    }


    public boolean update(AggregateP2PVlan p2pv) {
        synchronized(this) {
            if (!(p2pv.getSliceName() == null)) {
                try {
                    session = HibernateUtil.getSessionFactory().openSession();
                    tx = session.beginTransaction();
                    session.update(p2pv);
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    e.printStackTrace();
                    return false;
                } finally {
                    if (session.isOpen()) session.close();
                }
            } else {
                throw new IllegalArgumentException("the P2PVlan object must be associated with a valid sliceName");
            }
        }
        return true;
    }

    public boolean delete(AggregateP2PVlan p2pv) {
        if (p2pv == null)
            return false;
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                session.delete(p2pv);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
                return false;
            } finally {
                if (session.isOpen()) session.close();
            }
        }
        return true;
    }

    public boolean delete(String name, String vtag) {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                AggregateP2PVlan p2pv = this.getBySliceAndVtag(name, vtag);
                if (p2pv == null) {
                    return false;
                }
                session.delete(p2pv);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
                return false;
            } finally {
                if (session.isOpen()) session.close();
            }
        }
        return true;
    }

    public List<AggregateP2PVlan> getAll() {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                Query q = session.createQuery("from AggregateP2PVlan");
                return (List<AggregateP2PVlan>) q.list();
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
                if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public AggregateP2PVlan getBySliceAndVtag(String name, String vtag) {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                String hql = "from AggregateP2PVlan as p2pvlan where p2pvlan.sliceName='" + name + "'";
                if (!vtag.equalsIgnoreCase("any"))
                    hql = hql + " and p2pvlan.vtag='" + vtag + "'";
                Query q = session.createQuery(hql);
                if (q.list().size() == 0) {
                    return null;
                }
                return (AggregateP2PVlan) q.list().get(0);
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
                if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public AggregateP2PVlan getByGRI(String gri) {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                Query q = session.createQuery("from AggregateP2PVlan as p2pvlan where p2pvlan.globalReservationId='" + gri + "'");
                if (q.list().size() == 0) {
                    return null;
                }
                return (AggregateP2PVlan) q.list().get(0);
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
                if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public synchronized AggregateP2PVlan createVlan(String sliceName, String source, String srcInterface,
            String srcIpAndMask, String destination, String dstInterface, String dstIpAndMask,
            String vtag, float bw, String description, long startTime, long endTime, HashMap hm) {
        AggregateP2PVlan p2pvlan = this.getBySliceAndVtag(sliceName, vtag);
        String status = "";
        String message = "";
        if (p2pvlan != null && !vtag.equalsIgnoreCase("any")) {
            status = "FAILED";
            message = "GRI=" + p2pvlan.getGlobalReservationId() + ", Status=" + p2pvlan.getStatus() +
                    "\nNote: You may delete the VLAN and re-create.";
        } else {
            AggregateSlices slices = AggregateState.getAggregateSlices();
            AggregateSlice slice = slices.getByName(sliceName);
            //TODO update with RSPEC times
            if (slice != null) {
                if (slice.getCreatedTime() > startTime) {
                    startTime = slice.getCreatedTime();
                }
                if (slice.getExpiredTime() >= endTime) {
                    endTime = slice.getExpiredTime();
                } else {//the slice would expire bfore VLAN
                    //status = "FAILED";
                    //message = "Slice=" + sliceName + " would be expired before the VLAN ends. No VLAN created.";
                    log.warn("Slice=" + sliceName + " would be expired before the VLAN ends. but VLAN created anyway.");
                }
            } 
            // We no longer fail the vlanCreate due to nonexistence of slice. 
            // There is valid cases asking for stitching only p2pVlan. 
            //else {
            //    status = "FAILED";
            //    message = "Slice=" + sliceName + " does not exist. No VLAN created.";
            //}
            if (!status.matches("FAILED")) {
                p2pvlan = new AggregateP2PVlan(sliceName, source, destination, vtag, bw, description, startTime, endTime);
                p2pvlan.setSrcInterface(srcInterface);
                p2pvlan.setSrcIpAndMask(srcIpAndMask);
                p2pvlan.setDstInterface(dstInterface);
                p2pvlan.setDstIpAndMask(dstIpAndMask);
                status = p2pvlan.setupVlan();
                if (status.equalsIgnoreCase("FAILED")) {
                    message = "Error=" + p2pvlan.getErrorMessage();
                } else {
                    message = "GRI=" + p2pvlan.getGlobalReservationId() + ",VLAN=" + p2pvlan.getVtag();
                }
                //DB insert
                if (!AggregateState.getAggregateP2PVlans().add(p2pvlan)) {
                    status = "FAILED";
                    message += "\nFailed to add the p2pvlan into database";
                }
            }
        }
        hm.put("status", status);
        hm.put("message", message);
        return p2pvlan;
    }

    public synchronized HashMap deleteVlan(String sliceName, String vtag) {
        HashMap ret = new HashMap();
        // look for slice
        AggregateP2PVlan p2pvlan = this.getBySliceAndVtag(sliceName, vtag);
        String status = "";
        String message = "";
        if (p2pvlan != null) {
            status = p2pvlan.teardownVlan();

            if (status.matches("(?i)FAILED")) {
                message = "Error=" + p2pvlan.getErrorMessage();
            } else {
                message = "GRI=" + p2pvlan.getGlobalReservationId();
            }

            if (message.equals("")) {
                message = "GRI=" + p2pvlan.getGlobalReservationId();
            }
            //DB delete
            if (!AggregateState.getAggregateP2PVlans().delete(p2pvlan)) {
                status = "FAILED";
                message += "\nFailed to delete the p2pvlan from database";
            }
        } else {
            status = "FAILED";
            message = "Unkown SliceVLAN: " + sliceName + vtag;
        }
        ret.put("status", status);
        ret.put("message", message);
        return ret;
    }
}
