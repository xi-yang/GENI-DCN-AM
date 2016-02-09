//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.09 at 12:03:47 PM EST 
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

    private final static QName _Gateway_QNAME = new QName("http://www.geni.net/resources/rspec/ext/sdx/1/", "gateway");
    private final static QName _Route_QNAME = new QName("http://www.geni.net/resources/rspec/ext/sdx/1/", "route");
    private final static QName _Sdx_QNAME = new QName("http://www.geni.net/resources/rspec/ext/sdx/1/", "sdx");
    private final static QName _VirtualCloud_QNAME = new QName("http://www.geni.net/resources/rspec/ext/sdx/1/", "virtual_cloud");
    private final static QName _Subnet_QNAME = new QName("http://www.geni.net/resources/rspec/ext/sdx/1/", "subnet");
    private final static QName _Node_QNAME = new QName("http://www.geni.net/resources/rspec/ext/sdx/1/", "node");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.geni.www.resources.rspec.ext.sdx._1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SDXContent }
     * 
     */
    public SDXContent createSDXContent() {
        return new SDXContent();
    }

    /**
     * Create an instance of {@link NodeReference }
     * 
     */
    public NodeReference createNodeReference() {
        return new NodeReference();
    }

    /**
     * Create an instance of {@link SubnetContent }
     * 
     */
    public SubnetContent createSubnetContent() {
        return new SubnetContent();
    }

    /**
     * Create an instance of {@link VirtualCloudContent }
     * 
     */
    public VirtualCloudContent createVirtualCloudContent() {
        return new VirtualCloudContent();
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
     * Create an instance of {@link NetworkAddressContent }
     * 
     */
    public NetworkAddressContent createNetworkAddressContent() {
        return new NetworkAddressContent();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GatewayContent }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.geni.net/resources/rspec/ext/sdx/1/", name = "gateway")
    public JAXBElement<GatewayContent> createGateway(GatewayContent value) {
        return new JAXBElement<GatewayContent>(_Gateway_QNAME, GatewayContent.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RouteContent }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.geni.net/resources/rspec/ext/sdx/1/", name = "route")
    public JAXBElement<RouteContent> createRoute(RouteContent value) {
        return new JAXBElement<RouteContent>(_Route_QNAME, RouteContent.class, null, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link VirtualCloudContent }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.geni.net/resources/rspec/ext/sdx/1/", name = "virtual_cloud")
    public JAXBElement<VirtualCloudContent> createVirtualCloud(VirtualCloudContent value) {
        return new JAXBElement<VirtualCloudContent>(_VirtualCloud_QNAME, VirtualCloudContent.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SubnetContent }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.geni.net/resources/rspec/ext/sdx/1/", name = "subnet")
    public JAXBElement<SubnetContent> createSubnet(SubnetContent value) {
        return new JAXBElement<SubnetContent>(_Subnet_QNAME, SubnetContent.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NodeReference }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.geni.net/resources/rspec/ext/sdx/1/", name = "node")
    public JAXBElement<NodeReference> createNode(NodeReference value) {
        return new JAXBElement<NodeReference>(_Node_QNAME, NodeReference.class, null, value);
    }

}