//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.09 at 12:03:47 PM EST 
//


package net.geni.www.resources.rspec.ext.sdx._1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RouteContent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RouteContent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="to" type="{http://www.geni.net/resources/rspec/ext/sdx/1/}NetworkAddressContent" minOccurs="0"/>
 *         &lt;element name="from" type="{http://www.geni.net/resources/rspec/ext/sdx/1/}NetworkAddressContent" minOccurs="0"/>
 *         &lt;element name="next_hop" type="{http://www.geni.net/resources/rspec/ext/sdx/1/}NetworkAddressContent" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="client_id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RouteContent", propOrder = {
    "to",
    "from",
    "nextHop"
})
public class RouteContent {

    protected NetworkAddressContent to;
    protected NetworkAddressContent from;
    @XmlElement(name = "next_hop")
    protected NetworkAddressContent nextHop;
    @XmlAttribute(name = "client_id")
    protected String clientId;
    @XmlAttribute(name = "type")
    protected String type;

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link NetworkAddressContent }
     *     
     */
    public NetworkAddressContent getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link NetworkAddressContent }
     *     
     */
    public void setTo(NetworkAddressContent value) {
        this.to = value;
    }

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link NetworkAddressContent }
     *     
     */
    public NetworkAddressContent getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link NetworkAddressContent }
     *     
     */
    public void setFrom(NetworkAddressContent value) {
        this.from = value;
    }

    /**
     * Gets the value of the nextHop property.
     * 
     * @return
     *     possible object is
     *     {@link NetworkAddressContent }
     *     
     */
    public NetworkAddressContent getNextHop() {
        return nextHop;
    }

    /**
     * Sets the value of the nextHop property.
     * 
     * @param value
     *     allowed object is
     *     {@link NetworkAddressContent }
     *     
     */
    public void setNextHop(NetworkAddressContent value) {
        this.nextHop = value;
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

}
