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
public class AggregateNode extends AggregateResource {
    private int nodeId = 0;
    private String urn;
    private String description;
    private String capabilities;

    public AggregateNode() {
        urn = "";
        description = "";
        capabilities = "";
    }

    public AggregateNode(String u, int i, String d, String c) {
        urn = u;
        nodeId = i;
        description = d;
        capabilities = c;
    }

    public void setNodeId(int id) {
        this.nodeId = id;
    }

    public int getNodeId() {
        return this.nodeId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }
    
    public String getDescription() {
        return description;
    }

    public String getUrn() {
        return urn;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public boolean hasAllCaps(Vector<String> caps) {
        int capCnt = 0;
        for(int i = 0; i < caps.size(); i++) {
            if (capabilities.contains(caps.get(i))) {
                capCnt++;
            }
        }
        return (capCnt == caps.size());
    }

    public NodeDescriptorTypeSequence_type0 getCapTypeSeq() {
        NodeDescriptorTypeSequence_type0 ndT = new NodeDescriptorTypeSequence_type0();
        Vector<CapabilityType> ctV = new Vector<CapabilityType>();
        String[] caps = capabilities.split("\\s*,\\s*");
        AggregateCapabilities aggregateCaps = AggregateState.getAggregateCaps();
        for(String c: caps) {
            CapabilityType cT = new CapabilityType();
            AggregateCapability cap = aggregateCaps.getByUrn(c);
            cT.setName(cap.getName());
            cT.setUrn(cap.getUrn());
            cT.setId(cap.getId());
            cT.setDescription(cap.getDescription());
            cT.setControllerURL(cap.getControllerURL());
            ctV.add(cT);
        }
        ndT.setCapability((CapabilityType[]) ctV.toArray(new CapabilityType[]{}));
        return ndT;
    }
}
