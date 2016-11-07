//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.11.07 at 03:09:35 PM EST 
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
 * <p>Java class for NodeReference complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NodeReference">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.geni.net/resources/rspec/ext/sdx/1/}route" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.geni.net/resources/rspec/ext/sdx/1/}ceph_rbd" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.geni.net/resources/rspec/ext/sdx/1/}quagga_bgp" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="client_id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="host" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NodeReference", propOrder = {
    "route",
    "cephRbd",
    "quaggaBgp"
})
public class NodeReference {

    protected List<RouteContent> route;
    @XmlElement(name = "ceph_rbd")
    protected List<CephRbdContent> cephRbd;
    @XmlElement(name = "quagga_bgp")
    protected List<QuaggaBgpContent> quaggaBgp;
    @XmlAttribute(name = "client_id", required = true)
    protected String clientId;
    @XmlAttribute(name = "type", required = true)
    protected String type;
    @XmlAttribute(name = "host")
    protected String host;

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
     * Gets the value of the cephRbd property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cephRbd property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCephRbd().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CephRbdContent }
     * 
     * 
     */
    public List<CephRbdContent> getCephRbd() {
        if (cephRbd == null) {
            cephRbd = new ArrayList<CephRbdContent>();
        }
        return this.cephRbd;
    }

    /**
     * Gets the value of the quaggaBgp property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the quaggaBgp property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getQuaggaBgp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QuaggaBgpContent }
     * 
     * 
     */
    public List<QuaggaBgpContent> getQuaggaBgp() {
        if (quaggaBgp == null) {
            quaggaBgp = new ArrayList<QuaggaBgpContent>();
        }
        return this.quaggaBgp;
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
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the host property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the value of the host property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHost(String value) {
        this.host = value;
    }

}
