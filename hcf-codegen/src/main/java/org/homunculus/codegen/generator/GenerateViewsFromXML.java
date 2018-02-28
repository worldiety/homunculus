package org.homunculus.codegen.generator;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import org.homunculus.codegen.Generator;
import org.homunculus.codegen.GenProject;
import org.homunculus.codegen.XMLFile;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Torben Schinke on 22.02.18.
 */

public class GenerateViewsFromXML implements Generator {
    private final static String XML_NAMESPACE = "http://schemas.homunculus.io/apk/res/android";
    private final static String XML_CREATE_CLASS = "class";
    private final static String XML_CREATE_CLASS_SIMPLE = "hcf:class";
    private final static String XML_ID_SIMPLE = "android:id";

    private final static Map<String,String> ALIASE = new HashMap<>();

    static{
        ALIASE.put("android.widget.ImageView","ImageView");

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
            NodeList nl = file.getDoc().getChildNodes();
            String fqn = file.getDoc().getDocumentElement().getAttribute(XML_CREATE_CLASS_SIMPLE);
            if (fqn == null || fqn.trim().isEmpty()) {
                continue;
            }
            genClass(project, file, fqn);
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

        List<Element> exports = file.collect(element -> {
            String id = element.getAttribute(XML_ID_SIMPLE);
            if (id == null || id.trim().length() == 0) {
                return false;
            }
            return true;
        });

        for (Element element : exports) {
            String id = element.getAttribute(XML_ID_SIMPLE);
            AbstractJClass returnType = project.getCodeModel().ref(getAndroidViewName(element.getNodeName()));
            JMethod meth = cl.method(JMod.PUBLIC, returnType, "get" + makeNiceName(id));
            String fieldName = id.substring(id.lastIndexOf("/") + 1);
            meth.body()._return(JExpr.direct("findViewById(" + project.getManifestPackage() + ".R.id." + fieldName + ")"));
        }


    }

    private String getAndroidViewName(String id) {
        if (id.contains(".")) {
            return id;
        } else {
            return "android.widget." + id;
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
        return sb.toString();
    }
}
