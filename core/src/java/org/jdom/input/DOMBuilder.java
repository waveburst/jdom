/*-- 

 Copyright (C) 2000 Brett McLaughlin & Jason Hunter.
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions, and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions, and the disclaimer that follows 
    these conditions in the documentation and/or other materials 
    provided with the distribution.

 3. The name "JDOM" must not be used to endorse or promote products
    derived from this software without prior written permission.  For
    written permission, please contact license@jdom.org.
 
 4. Products derived from this software may not be called "JDOM", nor
    may "JDOM" appear in their name, without prior written permission
    from the JDOM Project Management (pm@jdom.org).
 
 In addition, we request (but do not require) that you include in the 
 end-user documentation provided with the redistribution and/or in the 
 software itself an acknowledgement equivalent to the following:
     "This product includes software developed by the
      JDOM Project (http://www.jdom.org/)."
 Alternatively, the acknowledgment may be graphical using the logos 
 available at http://www.jdom.org/images/logos.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 This software consists of voluntary contributions made by many 
 individuals on behalf of the JDOM Project and was originally 
 created by Brett McLaughlin <brett@jdom.org> and 
 Jason Hunter <jhunter@jdom.org>.  For more information on the 
 JDOM Project, please see <http://www.jdom.org/>.
 
 */

package org.jdom.input;

import java.io.*;
import java.net.*;
import java.util.*;

import org.jdom.*;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Entity;
import org.jdom.ProcessingInstruction;
import org.jdom.adapters.*;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

/**
 * <p><code>DOMBuilder</code> builds a JDOM tree using DOM.</p>
 *
 * @author Brett McLaughlin
 * @author Jason Hunter
 * @author Philip Nelson
 * @author Kevin Regan
 * @author Yusuf Goolamabbas
 * @author Dan Schaffer
 * @version 1.0
 */
public class DOMBuilder {

    /** Default adapter class */
    private static final String DEFAULT_ADAPTER_CLASS =
        "org.jdom.adapters.XercesDOMAdapter";

    /** Whether validation should occur */
    private boolean validate;

    /** Adapter class to use */
    private String adapterClass;

    /**
     * <p>
     * This allows the validation features to be turned on/off
     *   in the builder at creation, as well as set the
     *   DOM Adapter class to use.
     * </p>
     *
     * @param adapterClass <code>String</code> name of class
     *                     to use for DOM building.
     * @param validate <code>boolean</code> indicating if
     *                 validation should occur.
     */
    public DOMBuilder(String adapterClass, boolean validate) {
        this.adapterClass = adapterClass;
        this.validate = validate;
    }

    /**
     * <p>
     * This sets the DOM Adapter class to use, and leaves
     *   validation off.
     * </p>
     *
     * @param validate <code>boolean</code> indicating if
     *                 validation should occur.
     */
    public DOMBuilder(String adapterClass) {
        this(adapterClass, false);
    }

    /**
     * <p>
     * This sets validation for the <code>Builder</code>.
     * </p>
     *
     * @param validate <code>boolean</code> indicating if
     *                 validation should occur.
     */
    public DOMBuilder(boolean validate) {
        this(DEFAULT_ADAPTER_CLASS, validate);
    }

    /**
     * <p>
     * This creates a <code>DOMBuilder</code> with
     *   the default parser and no validation.
     * </p>
     */
    public DOMBuilder() {
        this(DEFAULT_ADAPTER_CLASS, false);
    }

    /**
     * <p>
     * This builds a document from the supplied
     *   input stream by constructing a DOM tree and reading information from
     *   the DOM to create a JDOM document, a slower approach than SAXBuilder 
     *   but useful for debugging.
     * </p>
     *
     * @param in <code>InputStream</code> to read from.
     * @return <code>Document</code> - resultant Document object.
     * @throws <code>JDOMException</code> when errors occur in
     *                                    parsing.
     */
    public Document build(InputStream in) throws JDOMException {
        Document doc = new Document(null);
        try {
            DOMAdapter adapter =
                (DOMAdapter)Class.forName(adapterClass).newInstance();

            org.w3c.dom.Document domDoc = adapter.getDocument(in, validate);

            // Start out at root level
            buildTree(domDoc, doc, null, true);
        }
        catch (Exception e) {
            throw new JDOMException("Error in building from stream", e);
        }

        return doc;
    }

    /**
     * <p>
     * This builds a document from the supplied
     *   filename by constructing a DOM tree and reading information from the
     *   DOM to create a JDOM document, a slower approach than SAXBuilder but 
     *   useful for debugging.
     * </p>
     *
     * @param file <code>File</code> to read from.
     * @return <code>Document</code> - resultant Document object.
     * @throws <code>JDOMException</code> when errors occur in
     *                                    parsing.
     */
    public Document build(File file) throws JDOMException {
        try {
            FileInputStream in = new FileInputStream(file);
            return build(in);
        }
        catch (FileNotFoundException e) {
            throw new JDOMException("Error in building from " + file, e);
        }
    }

    /**
     * <p>
     * This builds a document from the supplied
     *   URL by constructing a DOM tree and reading information from the
     *   DOM to create a JDOM document, a slower approach than SAXBuilder but 
     *   useful for debugging.
     * </p>
     *
     * @param url <code>URL</code> to read from.
     * @return <code>Document</code> - resultant Document object.
     * @throws <code>JDOMException</code> when errors occur in
     *                                    parsing.
     */
    public Document build(URL url) throws JDOMException {
        try {
            return build(url.openStream());
        }
        catch (IOException e) {
            throw new JDOMException("Error in building from " + url, e);
        }
    }

    /**
     * <p>
     * This will build a JDOM tree from an existing DOM tree.
     * </p>
     *
     * @param domDocument <code>org.w3c.dom.Document</code> object
     * @return <code>Document</code> - JDOM document object.
     */
    public Document build(org.w3c.dom.Document domDocument) {
        Document doc = new Document(null);
        buildTree(domDocument, doc, null, true);
        return doc;
    }

    /**
     * <p>
     * This will build a JDOM Element from an existing DOM Element
     * </p>
     *
     * @param domElement <code> org.w3c.dom.Element</code> object
     * @return <code>Element</code> - JDOM Element object
     */
    public org.jdom.Element build(org.w3c.dom.Element domElement) {
        Document doc = new Document(null);
        buildTree(domElement, doc, null, true);
        return doc.getRootElement();               
    }

    /**
     * <p>
     * This takes a DOM <code>Node</code> and builds up
     *   a JDOM tree, recursing until the DOM tree is exhausted
     *   and the JDOM tree results.
     * </p>
     *
     * @param node <code>Code</node> to examine.
     * @param doc JDOM <code>Document</code> being built.
     * @param current <code>Element</code> that is current parent.
     * @param atRoot <code>boolean</code> indicating whether at root level.
     */
    private void buildTree(Node node,
                           Document doc,
                           Element current,
                           boolean atRoot) {
        LinkedList additionalNamespaces = new LinkedList();

        // Recurse through the tree
        switch (node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                NodeList nodes = node.getChildNodes();
                for (int i=0, size=nodes.getLength(); i<size; i++) {
                    buildTree(nodes.item(i), doc, current, true);
                }
                break;

            case Node.ELEMENT_NODE:
                String localName = node.getLocalName();
                String prefix = node.getPrefix();
                String uri = node.getNamespaceURI();
                Element element = null;
                Namespace ns = null;
                if (uri == null) {
                    element = new Element(localName);
                }
                else {
                    ns = Namespace.getNamespace(prefix, uri);
                    element = new Element(localName, ns);
                }

                // Add attributes
                NamedNodeMap attributeList = node.getAttributes();
                for (int i=0, size=attributeList.getLength(); i<size; i++) {
                    Attr att = (Attr) attributeList.item(i);

                    // Distinguish between namespace and attribute
                    String attname = att.getName();
                    String attvalue = att.getValue();

                    // Don't add xmlns attributes, but do add them as
                    // additional namespaces if they're different than this
                    // element's namespace (perhaps we should also have logic
                    // not to mark them as additional if it's been done
                    // already, but it probably doesn't matter)
                    if (attname.equals("xmlns")) {
                        Namespace declaredNS =
                            Namespace.getNamespace("", attvalue);
                        if (!declaredNS.equals(ns)) {
                            element.addNamespaceDeclaration(declaredNS);
                        }
                    }
                    else if (attname.startsWith("xmlns:")) {
                        String attsubname = attname.substring(6);
                        Namespace declaredNS =
                            Namespace.getNamespace(attsubname, attvalue);
                        if (!declaredNS.equals(ns)) {
                            element.addNamespaceDeclaration(declaredNS);
                        }
                    }
                    else {
                        prefix = att.getPrefix();
                        uri = att.getNamespaceURI();
                        Namespace attns = Namespace.getNamespace(prefix, uri);
                        Attribute attribute =
                            new Attribute(attname, attvalue, attns);
                        element.addAttribute(attribute);
                    }
                }

                if (atRoot) {
                    // If at root, set as document root
                    doc.setRootElement(element);
                } else {
                    // else add to parent element
                    current.addContent(element);
                }

                // Recurse on child nodes
                NodeList children = node.getChildNodes();
                for (int i=0, size=children.getLength(); i<size; i++) {
                    buildTree(children.item(i), doc, element, false);
                }
                break;

            case Node.TEXT_NODE:
                String text = node.getNodeValue();
                current.addContent(text);
                break;

            case Node.CDATA_SECTION_NODE:
                String cdata = node.getNodeValue();
                current.addContent(new CDATA(cdata));
                break;


            case Node.PROCESSING_INSTRUCTION_NODE:
                if (atRoot) {
                    doc.addContent(
                        new ProcessingInstruction(node.getNodeName(),
                                                  node.getNodeValue()));
                } else {
                    current.addContent(
                        new ProcessingInstruction(node.getNodeName(),
                                                  node.getNodeValue()));
                }
                break;

            case Node.COMMENT_NODE:
                if (atRoot) {
                    doc.addContent(new Comment(node.getNodeValue()));
                } else {
                    current.addContent(new Comment(node.getNodeValue()));
                }
                break;

            case Node.ENTITY_REFERENCE_NODE:
                Entity entity = new Entity(node.getNodeName());

                // XXX: Temp Hack (brett)
                entity.setContent(node.getFirstChild().getNodeValue());

                current.addContent(entity);
                break;

            case Node.ENTITY_NODE:
                // ??
                break;

            case Node.DOCUMENT_TYPE_NODE:
                DocumentType domDocType = (DocumentType)node;
                String publicID = domDocType.getPublicId();
                String systemID = domDocType.getSystemId();

                DocType docType = new DocType(domDocType.getName());
                if ((publicID != null) && (!publicID.equals(""))) {
                    docType.setPublicID(publicID);
                }
                if ((systemID != null) && (!systemID.equals(""))) {
                    docType.setSystemID(systemID);
                }

                doc.setDocType(docType);
                break;
        }
    }
}
