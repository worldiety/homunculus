package org.homunculus.codegen.generator;

import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JPackage;
import com.helger.jcodemodel.JVar;

import org.homunculus.codegen.GenProject;
import org.homunculus.codegen.Generator;
import org.homunculus.codegen.SrcFile;
import org.homunculus.codegen.generator.PreprocessDiscoverBeans.DiscoveryKind;
import org.homunculusframework.factory.container.Configuration;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * Created by Torben Schinke on 20.02.18.
 */
@Deprecated
public class GenerateAutoDiscovery implements Generator {
    @Override
    public void generate(GenProject project) throws JClassAlreadyExistsException {
        Map<DiscoveryKind, Set<SrcFile>> discoveryKinds = project.getDiscoveredKinds();
        for (Entry<DiscoveryKind, Set<SrcFile>> entry : discoveryKinds.entrySet()) {
//            StringBuilder sb = new StringBuilder();
//            entry.getValue().stream().forEach(src -> sb.append("\n  *" + src.getFile()));
//            LoggerFactory.getLogger(getClass()).info("{}->{}", entry.getKey(), sb);
            LoggerFactory.getLogger(getClass()).info("detected {} classes of type {} for auto discovery", entry.getValue().size(), entry.getKey());
        }

        create(discoveryKinds, project);
    }

    private void create(Map<DiscoveryKind, Set<SrcFile>> discoveryKinds, GenProject project) throws JClassAlreadyExistsException {
        JPackage jp = project.getCodeModel()._package("org.homunculus.generated");
        JDefinedClass jc = jp._class("AutoDiscovery");
        jc.headerComment().add(project.getDisclaimer(getClass()));
        jc.javadoc().add("Automatically generated to provide an automatic discovery of classes within this project.");
        JMethod con = jc.constructor(JMod.PUBLIC);
        JVar varCfg = con.param(Configuration.class, "cfg");
        JBlock body = con.body();
        int total = 0;
        for (Entry<DiscoveryKind, Set<SrcFile>> entry : discoveryKinds.entrySet()) {
            body.addSingleLineComment();
            body.addSingleLineComment("register all kinds of " + entry.getKey());
            for (SrcFile file : entry.getValue()) {
                body.invoke(JExpr.ref(varCfg.name()), "add").arg(JExpr.direct(file.getFullQualifiedNamePrimaryClassName() + ".class"));
                total++;
            }

        }

        LoggerFactory.getLogger(getClass()).info("created {} with {} registrations", jc.fullName(), total);

    }


}
