package org.homunculus.codegen;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;

import org.homunculus.codegen.generator.GenerateAsyncControllers;
import org.homunculus.codegen.generator.GenerateAutoDiscovery;
import org.homunculus.codegen.generator.GenerateAutoDiscovery.DiscoveryKind;
import org.homunculus.codegen.generator.GenerateRequestFactories;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by Torben Schinke on 20.02.18.
 */

public class Project {

    private List<SrcFile> units = new ArrayList<>();
    private JCodeModel codeModel = new JCodeModel();
    private Map<DiscoveryKind, List<SrcFile>> discoveredKinds = new HashMap<>();

    public void addFile(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            CompilationUnit cu = JavaParser.parse(in);
            units.add(new SrcFile(file, cu));
//            LoggerFactory.getLogger(getClass()).info("added {}", file);
        }


    }

    public void addDirectory(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".java")) {
                addFile(file);
            }
        }
    }

    public void addRecursive(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                addRecursive(file);
            } else {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".java")) {
                    addFile(file);
                }
            }
        }
    }

    @Nullable
    public SrcFile findSourceFileForType(String fqn) {
        for (SrcFile file : units) {
            if (file.getFullQualifiedNamePrimaryClassName().equals(fqn)) {
                return file;
            }
        }
        return null;
    }

    public JCodeModel getCodeModel() {
        return codeModel;
    }

    public List<SrcFile> getUnits() {
        return units;
    }

    public Map<DiscoveryKind, List<SrcFile>> getDiscoveredKinds() {
        return discoveredKinds;
    }

    public void generate() throws Exception {
        new GenerateAutoDiscovery().generate(this);
        new GenerateAsyncControllers().generate(this);
        new GenerateRequestFactories().generate(this);
    }

    public void emitGeneratedClass(File targetDir) throws IOException {
        codeModel.build(targetDir, System.out);
    }

    public String getDisclaimer(Class<?> clazz) {
        return "DO NOT MODIFY - GENERATED BY " + clazz.getName();
    }
}
