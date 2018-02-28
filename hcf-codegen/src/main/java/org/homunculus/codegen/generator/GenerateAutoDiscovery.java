package org.homunculus.codegen.generator;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;
import com.helger.jcodemodel.JVar;

import org.homunculus.codegen.Generator;
import org.homunculus.codegen.GenProject;
import org.homunculus.codegen.SrcFile;
import org.homunculusframework.factory.container.Configuration;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;


/**
 * Created by Torben Schinke on 20.02.18.
 */

public class GenerateAutoDiscovery implements Generator {
    @Override
    public void generate(GenProject project) throws JClassAlreadyExistsException {
        Map<DiscoveryKind, List<SrcFile>> discoveryKinds = discover(project);
        for (Entry<DiscoveryKind, List<SrcFile>> entry : discoveryKinds.entrySet()) {
//            StringBuilder sb = new StringBuilder();
//            entry.getValue().stream().forEach(src -> sb.append("\n  *" + src.getFile()));
//            LoggerFactory.getLogger(getClass()).info("{}->{}", entry.getKey(), sb);
            LoggerFactory.getLogger(getClass()).info("detected {} classes of type {} for auto discovery", entry.getValue().size(), entry.getKey());
        }

        create(discoveryKinds, project);
    }

    private void create(Map<DiscoveryKind, List<SrcFile>> discoveryKinds, GenProject project) throws JClassAlreadyExistsException {
        JPackage jp = project.getCodeModel()._package("org.homunculus.generated");
        JDefinedClass jc = jp._class("AutoDiscovery");
        jc.headerComment().add(project.getDisclaimer(getClass()));
        jc.javadoc().add("Automatically generated to provide an automatic discovery of classes within this project.");
        JMethod con = jc.constructor(JMod.PUBLIC);
        JVar varCfg = con.param(Configuration.class, "cfg");
        JBlock body = con.body();
        int total = 0;
        for (Entry<DiscoveryKind, List<SrcFile>> entry : discoveryKinds.entrySet()) {
            body.addSingleLineComment();
            body.addSingleLineComment("register all kinds of " + entry.getKey());
            for (SrcFile file : entry.getValue()) {
                body.invoke(JExpr.ref(varCfg.name()), "add").arg(JExpr.direct(file.getFullQualifiedNamePrimaryClassName() + ".class"));
                total++;
            }

        }

        LoggerFactory.getLogger(getClass()).info("created {} with {} registrations", jc.fullName(), total);

    }

    private Map<DiscoveryKind, List<SrcFile>> discover(GenProject project) {
        Map<DiscoveryKind, List<SrcFile>> discoveryKinds = project.getDiscoveredKinds();
        for (DiscoveryKind kind : DiscoveryKind.values()) {
            discoveryKinds.put(kind, new ArrayList<>());
        }
        for (SrcFile src : project.getSrcFiles()) {
            Optional<ClassOrInterfaceDeclaration> optDec = src.getUnit().getClassByName(src.getPrimaryClassName());
            if (!optDec.isPresent()) {
                LoggerFactory.getLogger(getClass()).warn("ignored file {}", src.getFile());
                continue;
            }
            ClassOrInterfaceDeclaration dec = optDec.get();


            NodeList<AnnotationExpr> annotations = dec.getAnnotations();
            for (DiscoveryKind kind : DiscoveryKind.values()) {
                for (AnnotationExpr annotation : annotations) {
                    String symbol = src.getFullQualifiedName(annotation.getNameAsString());
                    if (kind.match(symbol)) {
                        discoveryKinds.get(kind).add(src);
                    }
                }
            }

        }
        return discoveryKinds;
    }


    public enum DiscoveryKind {
        /**
         * These things are singelton controllers
         */
        CONTROLLER("org.springframework.stereotype.Controller", "org.springframework.stereotype.Service", "javax.inject.Singleton"),
        /**
         * These things are just named, may be controllers AND/OR other beans (usually UIS)
         */
        NAMED("javax.inject.Named");

        private String[] fullQualifiedNames;

        DiscoveryKind(String... args) {
            fullQualifiedNames = args;
        }

        public boolean match(String name) {
            for (String n : fullQualifiedNames) {
                if (n.equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }
}
