package com.boomi.connector.apm;

import com.boomi.connector.api.*;
import com.boomi.connector.util.BaseBrowser;
import com.boomi.util.IOUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

/**
 * Browser
 * @author Anthony Rabiaza
 *
 */
public class BoomiAPMBrowser extends BaseBrowser {

	private static final String TYPE_ELEMENT = "type";
    protected BoomiAPMBrowser(BoomiAPMConnection conn) {
        super(conn);
    }

	@Override
	public ObjectDefinitions getObjectDefinitions(String objectTypeId,
			Collection<ObjectDefinitionRole> roles) {
		try {
			URL url = this.getClass().getClassLoader().getResource(objectTypeId.toLowerCase() + ".xsd");
			Document defDoc = parse(url.openStream());
			ObjectDefinitions defs = new ObjectDefinitions();
			ObjectDefinition def = new ObjectDefinition();
			def.setSchema(defDoc.getDocumentElement());
			def.setElementName(objectTypeId);
			defs.getDefinitions().add(def);

			return defs;

		}
		catch (Exception e) {
			throw new ConnectorException(e);
		}
	}

	@Override
	public ObjectTypes getObjectTypes() {
		try {
			URL url = this.getClass().getClassLoader().getResource("metadata.xml");
			Document typeDoc = parse(url.openStream());
			NodeList typeList = typeDoc.getElementsByTagName(TYPE_ELEMENT);
			ObjectTypes types = new ObjectTypes();
			for (int i = 0; i < typeList.getLength(); ++i) {
				Element typeEl = (Element) typeList.item(i);
				String typeName = typeEl.getTextContent().trim();
				ObjectType type = new ObjectType();
				type.setId(typeName);
				types.getTypes().add(type);
			}
			return types;
		} catch (Exception e) {
			throw new ConnectorException(e);
		}
	}

	@Override
    public BoomiAPMConnection getConnection() {
        return (BoomiAPMConnection) super.getConnection();
    }

	public static Document parse(InputStream input) throws ParserConfigurationException, SAXException, IOException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			return dbf.newDocumentBuilder().parse(input);
		}
		finally {
			IOUtil.closeQuietly(input);
		}
	}
}