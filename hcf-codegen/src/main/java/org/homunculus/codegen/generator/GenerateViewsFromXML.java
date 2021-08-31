/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.homunculus.codegen.generator;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import org.homunculus.codegen.GenProject;
import org.homunculus.codegen.Generator;
import org.homunculus.codegen.XMLFile;
import org.homunculus.codegen.parse.Strings;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Picks all xml's from the layout folder and creates them in packages relative to
 * the applications package name.
 * E.g. com.my.app is the app's package and your view is in res/layout/states_myuis_myview.xml
 * the view is generated as com.my.app.states.myuis.Myview
 * <p>
 * Comments are respected and added as javadoc.
 *
 * @author Torben Schinke
 * @since 1.0
 */

public class GenerateViewsFromXML implements Generator {
    private final static String XML_ID_SIMPLE = "android:id";
    private final static String INCLUDE = "include";
    private final static String AT_LAYOUT = "@layout/";

    private final static Map<String, String> ALIASE = new HashMap<>();

    static {
        ALIASE.put("ImageView", "android.widget.ImageView");
        ALIASE.put("ViewGroup", "android.view.ViewGroup");
        ALIASE.put("View", "android.view.View");

    }

    @Override
    public void generate(GenProject project) throws Exception {
        Map<String, XMLFile> toGenerate = collectViews(project);


    }


    /**
     * @param project
     * @return a map with full qualified classnames and the according files
     */
    private Map<String, XMLFile> collectViews(GenProject project) throws JClassAlreadyExistsException {
        Map<String, XMLFile> toGenerate = new HashMap<>();
        for (XMLFile file : project.getXmlFiles()) {
            //only pick those xml from the layout dir
            if (!file.getFile().getParentFile().getName().equals("layout")) {
                continue;
            }
            String mainPackage = project.getManifestPackage();
            String fname = file.getFile().getName().substring(0, file.getFile().getName().length() - 4);
            String[] relPackageName = fname.split(Pattern.quote("_"));
            String className;
            if (relPackageName.length == 0) {
                className = mainPackage + "." + Strings.startUpperCase(Strings.nicefy(fname));
            } else {
                className = mainPackage;
                for (int i = 0; i < relPackageName.length - 1; i++) {
                    className = className + "." + relPackageName[i];
                }
                className = className + "." + Strings.startUpperCase(Strings.nicefy(relPackageName[relPackageName.length - 1]));
            }
            genClass(project, file, className);
        }
        return toGenerate;
    }


    private void genClass(GenProject project, XMLFile file, String className) throws JClassAlreadyExistsException {

        String packagename;
        String simpleClassName;
        if (className.lastIndexOf('.') > 0) {
            packagename = className.substring(0, className.lastIndexOf('.'));
            simpleClassName = className.substring(className.lastIndexOf('.') + 1, className.length());
        } else {
            packagename = "";
            simpleClassName = className;
        }

        JDefinedClass cl = project.getCodeModel()._package(packagename)._class(simpleClassName);
        cl._extends(project.getCodeModel().directClass("org.homunculus.android.component.InflaterView"));
        cl.javadoc().add(project.getDisclaimer(getClass()));
        cl.javadoc().add("\nThis class has been generated from " + file.getFile().getAbsolutePath().substring(project.getProjectRoot().getAbsolutePath().length()));
        JMethod con = cl.constructor(JMod.PUBLIC);
        JVar pCtx = con.param(project.getCodeModel().ref("android.content.Context"), "context");

        String attribName = file.getFile().getName().substring(0, file.getFile().getName().length() - 4);
        String impName = project.getManifestPackage() + ".R.layout";
        con.javadoc().add("Inflates the layout from {@link " + impName + "." + attribName + "}");
        con.body().invokeSuper().arg(pCtx).arg(project.getCodeModel().ref(impName).staticRef(attribName));

        inflate(project, file);

        List<Element> exports = file.collect(element -> {
            String id = element.getAttribute(XML_ID_SIMPLE);
            if (id == null || id.trim().length() == 0) {
                return false;
            }
            return true;
        });

        for (Element element : exports) {
            if (element.getNodeName().equals("include")) {

            }
            String id = element.getAttribute(XML_ID_SIMPLE);
            AbstractJClass returnType = project.getCodeModel().ref(getAndroidViewName(element.getNodeName()));
            JMethod meth = cl.method(JMod.PUBLIC, returnType, "get" + makeNiceName(id));
            String fieldName = id.substring(id.lastIndexOf("/") + 1);
            meth.body()._return(JExpr.direct("findViewById(" + project.getManifestPackage() + ".R.id." + fieldName + ")"));

            Comment comment = grabLatestNode(element);
            if (comment != null) {
                meth.javadoc().add(comment.getNodeValue());
            }

        }
    }

    /**
     * Inflates all available includes from the given XML file. Each include may include other views, so
     * this process is repeated until no includes are available anymore.
     *
     * @param project to grab the includes from
     * @param file
     */
    private void inflate(GenProject project, XMLFile file) {
        while (true) {
            List<Element> includes = file.collect(element -> {
                return element.getNodeName().equals(INCLUDE);
            });

            if (includes.isEmpty()) {
                return;
            }

            for (Element include : includes) {
                //e.g. layout="@layout/activity_cart"
                String layoutId = include.getAttribute("layout");
                if (layoutId.startsWith(AT_LAYOUT)) {
                    layoutId = layoutId.substring(AT_LAYOUT.length());
                }

                //this overrides the included layout id
                String androidId = include.getAttribute(XML_ID_SIMPLE);

                XMLFile includeFile = null;
                String targetFileName = layoutId + ".xml";
                for (XMLFile i : project.getXmlFiles()) {
                    if (i.getFile().getName().equals(targetFileName)) {
                        includeFile = i;
                        break;
                    }
                }

                //we always replace include-nodes, either with android.view.View or the concrete types
                Element templateView;
                if (includeFile == null) {
                    //ups so such file or from sdk or whatever, replace with view
                    templateView = file.getDoc().createElement("android.view.View");
                } else {
                    //we found the file to include
                    templateView = (Element) file.getDoc().importNode(includeFile.getDoc().getDocumentElement(), true);
                }

                Node templateParent = include.getParentNode();
                templateParent.removeChild(include);
                //now either insert or add, whatever fits
                if (include.getNextSibling() != null) {
                    Node nextSibling = include.getNextSibling();
                    templateParent.insertBefore(templateView, nextSibling);
                } else {
                    templateParent.appendChild(templateView);
                }

                //do not forget to override the id
                if (androidId != null) {
                    templateView.setAttribute(XML_ID_SIMPLE, androidId);
                }

            }
        }
    }

    /**
     * Walks it's sibling up until and discards Text nodes until no more Text nodes are found and then returns
     * the Comment (if any) or null.
     *
     * @param node the node to grab the comment for
     * @return null or the comment
     */
    @Nullable
    private Comment grabLatestNode(Node node) {
        Node priorSib = node.getPreviousSibling();
        if (priorSib == null) {
            return null;
        }
        while (priorSib instanceof Text) {
            priorSib = priorSib.getPreviousSibling();
        }

        if (priorSib instanceof Comment) {
            return (Comment) priorSib;
        }
        return null;
    }

    private String getAndroidViewName(String id) {
        if (id.contains(".")) {
            return id;
        } else {
            String overrideImport = ALIASE.get(id);
            if (overrideImport != null) {
                return overrideImport;
            } else {
                return "android.widget." + id;
            }
        }
    }


    private String makeNiceName(String androidIdDecl) {
        String name = androidIdDecl.substring(androidIdDecl.lastIndexOf("/") + 1);
        StringBuilder sb = new StringBuilder();
        for (String camel : name.split(Pattern.quote("_"))) {
            if (camel.length() == 0) {
                continue;
            }
            sb.append(camel.substring(0, 1).toUpperCase());
            sb.append(camel.substring(1));
        }
        return Strings.nicefy(sb.toString());
    }
}
