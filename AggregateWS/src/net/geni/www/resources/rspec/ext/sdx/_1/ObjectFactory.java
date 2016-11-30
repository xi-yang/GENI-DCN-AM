//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.11.30 at 04:24:19 PM EST 
//


package net.geni.www.resources.rspec.ext.sdx._1;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.geni.www.resources.rspec.ext.sdx._1 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _QuaggaBgp_QNAME = new QName("http://www.geni.net/resources/rspec/ext/sdx/1/", "quagga_bgp");
    private final static QName _VirtualMachines_QNAME = new QName("http://www.geni.net/resources/rspec/ext/sdx/1/", "virtual_machines");
    private final static QName _CephRbds_QNAME = new QName("http://www.geni.net/resources/rspec/ext/sdx/1/", "ceph_rbds");
    private final static QName _VirtualClouds_QNAME = new QName("http://www.geni.net/resources/rspec/ext/sdx/1/", "virtual_clouds");
    private final static QName _Subnets_QNAME = new QName("http://www.geni.net/resources/rspec/ext/sdx/1/", "subnets");
    private final static QName _Sdx_QNAME = new QName("http://www.geni.net/resources/rspec/ext/sdx/1/", "sdx");
    private final static QName _Gateways_QNAME = new QName("http://www.geni.net/resources/rspec/ext/sdx/1/", "gateways");
    private final static QName _Routes_QNAME = new QName("http://www.geni.net/resources/rspec/ext/sdx/1/", "routes");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.geni.www.resources.rspec.ext.sdx._1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GatewayContent }
     * 
     */
    public GatewayContent createGatewayContent() {
        return new GatewayContent();
    }

    /**
     * Create an instance of {@link RouteContent }
     * 
     */
    public RouteContent createRouteContent() {
        return new RouteContent();
    }

    /**
     * Create an instance of {@link SDXContent }
     * 
     */
    public SDXContent createSDXContent() {
        return new SDXContent();
    }

    /**
     * Create an instance of {@link VirtualCloudContent }
     * 
     */
    public VirtualCloudContent createVirtualCloudContent() {
        return new VirtualCloudContent();
    }

    /**
     * Create an instance of {@link SubnetContent }
     * 
     */
    public SubnetContent createSubnetContent() {
        return new SubnetContent();
    }

    /**
     * Create an instance of {@link CephRbdContent }
     * 
     */
    public CephRbdContent createCephRbdContent() {
        return new CephRbdContent();
    }

    /**
     * Create an instance of {@link QuaggaBgpContent }
     * 
     */
    public QuaggaBgpContent createQuaggaBgpContent() {
        return new QuaggaBgpContent();
    }

    /**
     * Create an instance of {@link VirtualMachine }
     * 
     */
    public VirtualMachine createVirtualMachine() {
        return new VirtualMachine();
    }

    /**
     * Create an instance of {@link NetworkAddressContent }
     * 
     */
    public NetworkAddressContent createNetworkAddressContent() {
        return new NetworkAddressContent();
    }

    /**
     * Create an instance of {@link BgpNeighborContent }
     * 
     */
    public BgpNeighborContent createBgpNeighborContent() {
        return new BgpNeighborContent();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QuaggaBgpContent }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.geni.net/resources/rspec/ext/sdx/1/", name = "quagga_bgp")
    public JAXBElement<QuaggaBgpContent> createQuaggaBgp(QuaggaBgpContent value) {
        return new JAXBElement<QuaggaBgpContent>(_QuaggaBgp_QNAME, QuaggaBgpContent.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VirtualMachine }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.geni.net/resources/rspec/ext/sdx/1/", name = "virtual_machines")
    public JAXBElement<VirtualMachine> createVirtualMachines(VirtualMachine value) {
        return new JAXBElement<VirtualMachine>(_VirtualMachines_QNAME, VirtualMachine.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CephRbdContent }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.geni.net/resources/rspec/ext/sdx/1/", name = "ceph_rbds")
    public JAXBElement<CephRbdContent> createCephRbds(CephRbdContent value) {
        return new JAXBElement<CephRbdContent>(_CephRbds_QNAME, CephRbdContent.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VirtualCloudContent }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.geni.net/resources/rspec/ext/sdx/1/", name = "virtual_clouds")
    public JAXBElement<VirtualCloudContent> createVirtualClouds(VirtualCloudContent value) {
        return new JAXBElement<VirtualCloudContent>(_VirtualClouds_QNAME, VirtualCloudContent.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubnetContent }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.geni.net/resources/rspec/ext/sdx/1/", name = "subnets")
    public JAXBElement<SubnetContent> createSubnets(SubnetContent value) {
        return new JAXBElement<SubnetContent>(_Subnets_QNAME, SubnetContent.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SDXContent }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.geni.net/resources/rspec/ext/sdx/1/", name = "sdx")
    public JAXBElement<SDXContent> createSdx(SDXContent value) {
        return new JAXBElement<SDXContent>(_Sdx_QNAME, SDXContent.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GatewayContent }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.geni.net/resources/rspec/ext/sdx/1/", name = "gateways")
    public JAXBElement<GatewayContent> createGateways(GatewayContent value) {
        return new JAXBElement<GatewayContent>(_Gateways_QNAME, GatewayContent.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RouteContent }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.geni.net/resources/rspec/ext/sdx/1/", name = "routes")
    public JAXBElement<RouteContent> createRoutes(RouteContent value) {
        return new JAXBElement<RouteContent>(_Routes_QNAME, RouteContent.class, null, value);
    }

}
