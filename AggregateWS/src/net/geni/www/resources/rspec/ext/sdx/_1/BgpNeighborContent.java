//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.11.07 at 03:04:02 PM EST 
//


package net.geni.www.resources.rspec.ext.sdx._1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BgpNeighborContent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BgpNeighborContent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="remote_asn" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="bgp_authkey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BgpNeighborContent", propOrder = {
    "remoteAsn",
    "bgpAuthkey"
})
public class BgpNeighborContent {

    @XmlElement(name = "remote_asn", required = true)
    protected String remoteAsn;
    @XmlElement(name = "bgp_authkey")
    protected String bgpAuthkey;

    /**
     * Gets the value of the remoteAsn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemoteAsn() {
        return remoteAsn;
    }

    /**
     * Sets the value of the remoteAsn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemoteAsn(String value) {
        this.remoteAsn = value;
    }

    /**
     * Gets the value of the bgpAuthkey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBgpAuthkey() {
        return bgpAuthkey;
    }

    /**
     * Sets the value of the bgpAuthkey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBgpAuthkey(String value) {
        this.bgpAuthkey = value;
    }

}
