package com.eurodyn.qlack2.be.forms.impl.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.eurodyn.qlack2.be.forms.api.exception.QFormsRuntimeException;

public class XmlUtil {
	private static final Logger LOGGER = Logger.getLogger(XmlUtil.class
			.getName());

	private BundleContext context;

	private ConverterUtil converterUtil;

	private XPath xpath;

	public XmlUtil() {
		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
	}

	public String getTemplateContent(String appName, String formName)
			throws IOException, ParserConfigurationException, SAXException,
			XPathExpressionException {
		String content = null;

		Bundle bundle = context.getBundle();
		URL entry = bundle.getEntry("template.xml");

		try (InputStream inputStream = entry.openStream()) {

			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true);
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document inputDoc = builder.parse(inputStream);

			Node appNode = evaluateXPath(inputDoc, "//application-name");
			appNode.setTextContent(appName);

			Node formNode = evaluateXPath(inputDoc, "//form-name");
			formNode.setTextContent(formName);

			content = converterUtil.xmlDocumentToString(inputDoc);
		}
		return content;
	}

	/**
	 * Replace the data sent by the client application to the form definition
	 * 
	 * @param xmlContent
	 * @param fragment
	 * @return
	 */
	public String loadInitialData(String xmlContent, String fragment, boolean viewMode) {
		String content = null;
		try {
			InputStream is = new ByteArrayInputStream(
					xmlContent.getBytes("UTF-8"));
			DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			domFactory.setNamespaceAware(true);
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			// parse form definition
			Document inputDoc = builder.parse(is);

			// parse xml with data sent by the client application
			Document fragmentDoc = builder.parse(new ByteArrayInputStream(
					fragment.getBytes("UTF-8")));
			
			XPath xPath = XPathFactory.newInstance().newXPath();

			// To be able to use XPath expressions for elements with a prefix on
			// XML content defined in a (default) namespace, we need to specify
			// a namespace prefix mapping
			xPath.setNamespaceContext(new NamespaceContext() {
				@Override
				public String getNamespaceURI(String prefix) {
					if (prefix == null)
						throw new NullPointerException("Null prefix");
					else if ("xf".equals(prefix))
						return "http://www.w3.org/2002/xforms";
					else if ("xh".equals(prefix))
						return "http://www.w3.org/1999/xhtml";
					else if ("xml".equals(prefix))
						return XMLConstants.XML_NS_URI;
					return XMLConstants.NULL_NS_URI;
				}

				// This method isn't necessary for XPath processing.
				@Override
				public String getPrefix(String uri) {
					throw new UnsupportedOperationException();
				}

				// This method isn't necessary for XPath processing either.
				@Override
				public Iterator getPrefixes(String uri) {
					throw new UnsupportedOperationException();
				}
			});

			// Get the leaf nodes of the instance node with id
			// 'fr-form-instance' from the form definition xml
			XPathExpression expr = xPath
					.compile("/xh:html/xh:head/xf:model/xf:instance[@id='fr-form-instance']/form//*[not(*)]");
			NodeList lista = (NodeList) expr.evaluate(inputDoc,
					XPathConstants.NODESET);
			
			if (viewMode) {
				XPathExpression exprView = xPath
					.compile("/xh:html/xh:head/xf:model/xf:bind[@id='fr-form-binds']");
				Node formBindNode = (Node) exprView.evaluate(inputDoc,
					XPathConstants.NODE);
				if (formBindNode != null)
					setReadonly(formBindNode);
			}

			// To be able to use XPath expressions for elements with a prefix on
			// XML content defined in a (default) namespace, we need to specify
			// a namespace prefix mapping
			NamespaceContext context = null;
			try {
				context = new XMLNamespaceContext(fragment);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,
						"Error loading initial data to form definition", e);
				throw new QFormsRuntimeException(
						"Error loading initial data to form definition");
			}

			// Visit all leaf nodes of the fr-form-instance and replace its text
			// content with the respective value that the client application has
			// sent
			replaceLeafNodes(lista, fragmentDoc, inputDoc, context);

			// Transform the modified xml source back to string
			DOMSource domSource = new DOMSource(inputDoc);
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			StreamResult result = new StreamResult(new StringWriter());
			transformer.transform(domSource, result);

			content = result.getWriter().toString();
		} catch (IOException | ParserConfigurationException | SAXException
				| TransformerFactoryConfigurationError | TransformerException
				| XPathExpressionException e) {
			LOGGER.log(Level.SEVERE,
					"Error loading initial data to form definition", e);
			throw new QFormsRuntimeException(
					"Error loading initial data to form definition");
		}
		return content;
	}

	private void setReadonly(Node node) {

		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				//set element readonly 
				if (((Element)node).getAttribute("viewModeEditable") != null){
					if (! ((Element)currentNode).getAttribute("viewModeEditable").equals("true")){
						((Element)currentNode).setAttribute("readonly", "true()");
					}
				}
				setReadonly(currentNode);
			}
		}
	}
	
	private void addParentNodeInXpath(Node parentNode, StringBuilder builder,
			NamespaceContext context) {
		if (parentNode.getParentNode() != null
				&& !"form".equals(parentNode.getParentNode().getLocalName())) {
			addParentNodeInXpath(parentNode.getParentNode(), builder, context);
		}
		String parentPrefix = context.getPrefix(parentNode.getNamespaceURI());
		if (parentPrefix != null) {
			builder.append(parentPrefix).append(':')
					.append(parentNode.getLocalName()).append('/');
		} else {
			builder.append(parentNode.getLocalName()).append('/');
		}
	}

	private void replaceLeafNodes(NodeList list, Document data,
			Document newDoc, NamespaceContext context)
			throws XPathExpressionException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(context);

		for (int i = 0; i < list.getLength(); i++) {
			// get child node
			Node childNode = list.item(i);

			StringBuilder builder = new StringBuilder("/");
			try {
				// construct absolute xpath, so not to replace nodes with
				// the same name but under different path
				if (childNode.getParentNode() != null) {
					addParentNodeInXpath(childNode.getParentNode(), builder,
							context);
				}

				String prefix = context.getPrefix(childNode.getNamespaceURI());
				if (prefix != null) {
					builder.append(prefix).append(':')
							.append(childNode.getLocalName());
				} else {
					builder.append(childNode.getLocalName());
				}

				XPathExpression expr = xPath.compile(builder.toString());

				handleNodes(data, newDoc, childNode, expr);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
				throw e;
			}

		}
	}

	private void handleNodes(Document data, Document newDoc, Node childNode, XPathExpression expr)
			throws XPathExpressionException {
		NodeList dataNodeList = (NodeList) expr.evaluate(data,
				XPathConstants.NODESET);
		if (dataNodeList.getLength() > 0) {
			for (int j = 0; j < dataNodeList.getLength(); j++) {
				Node dataNode = dataNodeList.item(j);
				if (dataNode != null) {
					Node newNode = newDoc.importNode(dataNode, true);
					childNode.getParentNode().appendChild(newNode);
				}
			}
			//remove initial empty node:
			childNode.getParentNode().removeChild(childNode);
		}
	}

	private Node evaluateXPath(Document document, String expression)
			throws XPathExpressionException {
		return (Node) xpath.evaluate(expression, document, XPathConstants.NODE);
	}

	public void setContext(BundleContext context) {
		this.context = context;
	}

	public void setConverterUtil(ConverterUtil converterUtil) {
		this.converterUtil = converterUtil;
	}

}