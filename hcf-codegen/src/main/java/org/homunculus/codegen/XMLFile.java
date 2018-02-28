package org.homunculus.codegen;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by Torben Schinke on 22.02.18.
 */

public class XMLFile {
    private final File file;
    private final Document doc;

    public XMLFile(File file, Document doc) {
        this.file = file;
        this.doc = doc;
    }

    public File getFile() {
        return file;
    }

    public Document getDoc() {
        return doc;
    }


    public List<Element> collect(Function<Element, Boolean> acceptor) {
        ArrayList<Element> dst = new ArrayList<>();
        collect(dst, doc.getDocumentElement(), acceptor);
        return dst;
    }

    private void collect(List<Element> list, Element root, Function<Element, Boolean> acceptor) {
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                if (acceptor.apply((Element) node)) {
                    list.add((Element) node);
                }
                collect(list, (Element) node, acceptor);
            }
        }
    }
}
