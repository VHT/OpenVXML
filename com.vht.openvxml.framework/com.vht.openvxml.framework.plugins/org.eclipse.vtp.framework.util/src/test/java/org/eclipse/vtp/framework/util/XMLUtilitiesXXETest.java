package org.eclipse.vtp.framework.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

/**
 * Verifies the XXE (CWE-611) hardening of {@link XMLUtilities}: a legitimate
 * Genesys attached-data payload still parses, while a DOCTYPE payload is
 * rejected. Not run by {@code mvn verify} (Tycho compiles only src/main/java);
 * run from an IDE or with the bundled junit/hamcrest jars on the classpath.
 */
public class XMLUtilitiesXXETest {

	/** Real-shape Genesys GetAttachedData response. */
	private static final String LEGITIMATE = "<userdata>"
			+ "<key name=\"CustomerSegment\" value=\"VQ_Runteam\"/>"
			+ "<key name=\"VH_ANI\" value=\"2167025451\"/>"
			+ "<key name=\"VH_OFFER\" value=\"1:Y\"/>" + "</userdata>";

	/** Same shape, weaponized with an external-entity DOCTYPE. */
	private static final String XXE = "<?xml version=\"1.0\"?>"
			+ "<!DOCTYPE userdata [ <!ENTITY xxe SYSTEM \"file:///c:/windows/win.ini\"> ]>"
			+ "<userdata><key name=\"CustomerSegment\" value=\"&xxe;\"/></userdata>";

	private static Document parse(String xml) throws Exception {
		DocumentBuilder builder = XMLUtilities.getDocumentBuilder();
		return builder.parse(new ByteArrayInputStream(xml
				.getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	public void legitimatePayloadStillParses() throws Exception {
		Document doc = parse(LEGITIMATE);
		NodeList keys = doc.getDocumentElement().getElementsByTagName("key");
		assertEquals(3, keys.getLength());
		assertEquals("VQ_Runteam",
				((Element) keys.item(0)).getAttribute("value"));
	}

	@Test
	public void doctypePayloadIsRejected() throws Exception {
		try {
			parse(XXE);
			fail("Expected SAXParseException: DOCTYPE must be disallowed");
		} catch (SAXParseException expected) {
			// Rejected before &xxe; is resolved.
		}
	}

	@Test
	public void namespaceAwareOverloadResolvesLocalNames() throws Exception {
		String ns = "<soap:Envelope "
				+ "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+ "<soap:Body/></soap:Envelope>";
		DocumentBuilder builder = XMLUtilities.getDocumentBuilder(true);
		Document doc = builder.parse(new ByteArrayInputStream(ns
				.getBytes(StandardCharsets.UTF_8)));
		assertNotNull(doc.getDocumentElement().getLocalName());
		assertEquals("Envelope", doc.getDocumentElement().getLocalName());
	}
}
