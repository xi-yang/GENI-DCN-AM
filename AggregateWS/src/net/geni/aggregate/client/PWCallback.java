package net.geni.aggregate.client;

import org.apache.ws.security.WSPasswordCallback;

import javax.security.auth.callback.*;
import java.io.*;
import javax.xml.stream.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import javax.xml.namespace.QName;
import java.util.Iterator;

import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.RampartConfig;

/**
 * Borrowed from the ESnet OSCARS Project
 *
 */
public class PWCallback  implements CallbackHandler {
    /*
     *
     * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
     *
     * This handler is called by the rampartSender digital signature builder to get password
     * for the user who is sending the message. The password is used to access the user's private key
     * in the keystore which is used to sign the message.
     * This code assumes that all user passwords are the same as the keystore password. If you
     * wish to have different user passwords you will need to change this code to return a different
     * passwords for different users. See the commented out code at the end of the file for a clue.
     *
     * @param callbacks in/out param, the password field WSPasswordCallback is set here.
     * @throws java.io.IOException
     * @throws javax.security.auth.callback.UnsupportedCallbackException
     */

    public void handle(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException {

        String keyPass = null;
        try {
            /* Get keystore password  from repo/rampConfig.xml  */
            String rampConfigFname = "repo/rampConfig.xml";
            FileInputStream fis = new FileInputStream(rampConfigFname);
            XMLInputFactory xif= XMLInputFactory.newInstance();
            XMLStreamReader reader= xif.createXMLStreamReader(fis);
            StAXOMBuilder builder= new StAXOMBuilder(reader);

            OMElement rampConfig= builder.getDocumentElement();
            OMElement sigCrypto = null;
            for (Iterator<OMElement> elementIter = rampConfig.getChildElements(); elementIter.hasNext();) {
                sigCrypto = elementIter.next();
                QName prop = new QName(RampartConfig.NS, RampartConfig.SIG_CRYPTO_LN);
                if (prop.equals(sigCrypto.getQName()) ) {
                    break;
                }
            }

            OMElement crypto = sigCrypto.getFirstElement();
            for (Iterator<OMElement> elementIter = crypto.getChildElements(); elementIter.hasNext();) {
                OMElement element = elementIter.next();
                OMAttribute nameAttr = element.getAttribute(new QName("",CryptoConfig.PROPERTY_NAME_ATTR));
                if (nameAttr.getAttributeValue().equals("org.apache.ws.security.crypto.merlin.keystore.password")) {
                    keyPass = element.getText().trim();
                }
            }
            //System.out.println("password= " + keyPass);
        } catch (XMLStreamException e){
            throw new IOException (e.getMessage());
        }

        /* assume we are getting a WSPasswordCall instance from Rampart */
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                if (keyPass != null) {
                    pc.setPassword(keyPass);
                } else {
                    pc.setPassword("password");
                }
            } else {
                throw new UnsupportedCallbackException(callbacks[i],
                "Unrecognized Callback");
            }
        }
    }
}

