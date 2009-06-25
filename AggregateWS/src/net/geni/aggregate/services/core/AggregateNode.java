/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.util.Vector;
import net.geni.aggregate.services.api.CapabilityType;
import net.geni.aggregate.services.api.NodeDescriptorTypeSequence_type0;

/**
 *
 * @author jflidr
 */
public class AggregateNode
{

    private String urn;
    private int id;
    private String description;
    private Vector<AggregateCapability> caps = new Vector<AggregateCapability>();

    public AggregateNode(String u, int i, String d, String c) {
        urn = u;
        id = i;
        description = d;
        String[] a = c.split("\\s*,\\s*");
        for(int j = 0; j < a.length; j++) {
            AggregateCapability cap = AggregateState.getAggregateCaps().getCap(a[j]);
            if(cap != null) {
                caps.add(cap);
            }
        }
    }

    public Vector<AggregateCapability> getCaps() {
        return caps;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public String getUrn() {
        return urn;
    }

    public boolean hasAll(Vector<String> u) {
        int capCnt = 0;
        for(int i = 0; i < u.size(); i++) {
            for(int j = 0; j < caps.size(); j++) {
                if(caps.get(j).getUrn().matches(u.get(i))) {
                    capCnt++;
                    break;
                }
            }

        }
        return (capCnt == u.size());
    }

    public NodeDescriptorTypeSequence_type0 getCapTypeSeq() {
        NodeDescriptorTypeSequence_type0 ndT = new NodeDescriptorTypeSequence_type0();
        Vector<CapabilityType> ctV = new Vector<CapabilityType>();
        for(int i = 0; i < caps.size(); i++) {
            CapabilityType cT = new CapabilityType();
            cT.setName(caps.get(i).getName());
            cT.setUrn(caps.get(i).getUrn());
            cT.setId(caps.get(i).getId());
            cT.setDescription(caps.get(i).getDescription());
            cT.setControllerURL(caps.get(i).getControllerURL());
            ctV.add(cT);
        }
        ndT.setCapability((CapabilityType[]) ctV.toArray(new CapabilityType[]{}));
        return ndT;
    }
}
