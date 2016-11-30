//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.11.30 at 04:24:19 PM EST 
//


package net.geni.www.resources.rspec.ext.sdx._1;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SDXContent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SDXContent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.geni.net/resources/rspec/ext/sdx/1/}virtual_clouds" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.geni.net/resources/rspec/ext/sdx/1/}subnets" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.geni.net/resources/rspec/ext/sdx/1/}virtual_machines" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="lastUpdateTime" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="component_manager_id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="component_id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SDXContent", propOrder = {
    "virtualClouds",
    "subnets",
    "virtualMachines"
})
public class SDXContent {

    @XmlElement(name = "virtual_clouds")
    protected List<VirtualCloudContent> virtualClouds;
    protected List<SubnetContent> subnets;
    @XmlElement(name = "virtual_machines")
    protected List<VirtualMachine> virtualMachines;
    @XmlAttribute(name = "lastUpdateTime", required = true)
    protected String lastUpdateTime;
    @XmlAttribute(name = "component_manager_id", required = true)
    protected String componentManagerId;
    @XmlAttribute(name = "component_id")
    protected String componentId;

    /**
     * Gets the value of the virtualClouds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the virtualClouds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVirtualClouds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link VirtualCloudContent }
     * 
     * 
     */
    public List<VirtualCloudContent> getVirtualClouds() {
        if (virtualClouds == null) {
            virtualClouds = new ArrayList<VirtualCloudContent>();
        }
        return this.virtualClouds;
    }

    /**
     * Gets the value of the subnets property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subnets property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubnets().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SubnetContent }
     * 
     * 
     */
    public List<SubnetContent> getSubnets() {
        if (subnets == null) {
            subnets = new ArrayList<SubnetContent>();
        }
        return this.subnets;
    }

    /**
     * Gets the value of the virtualMachines property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the virtualMachines property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVirtualMachines().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link VirtualMachine }
     * 
     * 
     */
    public List<VirtualMachine> getVirtualMachines() {
        if (virtualMachines == null) {
            virtualMachines = new ArrayList<VirtualMachine>();
        }
        return this.virtualMachines;
    }

    /**
     * Gets the value of the lastUpdateTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * Sets the value of the lastUpdateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastUpdateTime(String value) {
        this.lastUpdateTime = value;
    }

    /**
     * Gets the value of the componentManagerId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComponentManagerId() {
        return componentManagerId;
    }

    /**
     * Sets the value of the componentManagerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComponentManagerId(String value) {
        this.componentManagerId = value;
    }

    /**
     * Gets the value of the componentId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * Sets the value of the componentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComponentId(String value) {
        this.componentId = value;
    }

}
