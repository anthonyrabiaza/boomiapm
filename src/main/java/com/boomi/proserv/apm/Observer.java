package com.boomi.proserv.apm;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import com.boomi.util.IOUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class Observer {
    public final static String s_prefix = "boomi.";

    protected String postRequest(String url, String body, Map<String, String> headers) throws Exception {
        URL urlTarget = new URL(url);
        HttpURLConnection httpConnection = (HttpURLConnection) urlTarget.openConnection();
        httpConnection.setUseCaches(false);
        httpConnection.setDoOutput(true);
        httpConnection.setRequestMethod("POST");
        byte[] bytes = body.getBytes();
        String bodyUTF8 = new String(bytes, StandardCharsets.UTF_8);
        httpConnection.setFixedLengthStreamingMode(bodyUTF8.length());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpConnection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        OutputStreamWriter writer = new OutputStreamWriter(httpConnection.getOutputStream(), StandardCharsets.UTF_8);
        writer.write(bodyUTF8);
        writer.flush();
        String response = "";
        InputStreamReader in = new InputStreamReader(httpConnection.getInputStream());
        BufferedReader br = new BufferedReader(in);
        String text = "";
        while ((text = br.readLine()) != null) {
            response += text;
        }
        httpConnection.disconnect();

        return response;
    }

    protected String getHostname() {
        try {
            String os       = System.getProperty("os.name").toLowerCase();
            String hostname = "localhost";

            if (os.contains("win")) {
                hostname = System.getenv("COMPUTERNAME");
            }else if (os.contains("nix") || os.contains("nux") || os.contains("mac os x")) {
                hostname = System.getenv("HOSTNAME");
            }
            if(hostname == null || "".equals(hostname) || "null".equals(hostname)){
                hostname = InetAddress.getLocalHost().getHostName();
            }
            return hostname;
        } catch (Exception e){
            return "unknown";
        }
    }

    protected String getTimestamp() {
        return String.valueOf(System.currentTimeMillis() / 1000L);
    }

    protected String getTimestampMinusOrPlusSeconds(int seconds) {
        return String.valueOf(((System.currentTimeMillis() / 1000L) + seconds));
    }

    protected String convertStackTraceToString(Throwable throwable) {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw))
        {
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
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

    public static List<Element> getNodes(Document doc, String xpath) throws Exception {
        List<Element> elements = Collections.synchronizedList(new ArrayList<Element>());
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList)xPath.evaluate(xpath, doc, XPathConstants.NODESET);
        for(int i=0;i<xpath.length();i++) {
            Element e = (Element) nodes.item(i);
            elements.add(e);
        }
        return elements;
    }

    public static String getFirstNodeTextContent(Document doc, String xpath) throws Exception {
        try {
            List<Element> elements = getNodes(doc, xpath);
            return elements.get(0).getTextContent();
        } catch (Exception e) {
            return "";
        }
    }

    public static String toString(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }
}
