//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.09 at 12:03:47 PM EST 
//


package net.geni.www.resources.rspec.ext.sdx._1;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VirtualCloudContent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VirtualCloudContent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cidr" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element ref="{http://www.geni.net/resources/rspec/ext/sdx/1/}subnet" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.geni.net/resources/rspec/ext/sdx/1/}route" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.geni.net/resources/rspec/ext/sdx/1/}gateway" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="client_id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="provider_id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VirtualCloudContent", propOrder = {
    "cidr",
    "subnet",
    "route",
    "gateway"
})
public class VirtualCloudContent {

    protected String cidr;
    protected List<SubnetContent> subnet;
    protected List<RouteContent> route;
    protected List<GatewayContent> gateway;
    @XmlAttribute(name = "client_id", required = true)
    protected String clientId;
    @XmlAttribute(name = "provider_id", required = true)
    protected String providerId;

    /**
     * Gets the value of the cidr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCidr() {
        return cidr;
    }

    /**
     * Sets the value of the cidr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCidr(String value) {
        this.cidr = value;
    }

    /**
     * Gets the value of the subnet property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subnet property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubnet().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SubnetContent }
     * 
     * 
     */
    public List<SubnetContent> getSubnet() {
        if (subnet == null) {
            subnet = new ArrayList<SubnetContent>();
        }
        return this.subnet;
    }

    /**
     * Gets the value of the route property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the route property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRoute().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RouteContent }
     * 
     * 
     */
    public List<RouteContent> getRoute() {
        if (route == null) {
            route = new ArrayList<RouteContent>();
        }
        return this.route;
    }

    /**
     * Gets the value of the gateway property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gateway property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGateway().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GatewayContent }
     * 
     * 
     */
    public List<GatewayContent> getGateway() {
        if (gateway == null) {
            gateway = new ArrayList<GatewayContent>();
        }
        return this.gateway;
    }

    /**
     * Gets the value of the clientId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the value of the clientId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientId(String value) {
        this.clientId = value;
    }

    /**
     * Gets the value of the providerId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProviderId() {
        return providerId;
    }

    /**
     * Sets the value of the providerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProviderId(String value) {
        this.providerId = value;
    }

}