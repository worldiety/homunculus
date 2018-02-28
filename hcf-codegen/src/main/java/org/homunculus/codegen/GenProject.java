package org.homunculus.codegen;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;

import org.homunculus.codegen.generator.GenerateAsyncControllers;
import org.homunculus.codegen.generator.GenerateAutoDiscovery;
import org.homunculus.codegen.generator.GenerateAutoDiscovery.DiscoveryKind;
import org.homunculus.codegen.generator.GenerateRequestFactories;
import org.homunculus.codegen.generator.GenerateViewsFromXML;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Torben Schinke on 20.02.18.
 */

public class GenProject {

    private List<SrcFile> units = new ArrayList<>();
    private JCodeModel codeModel = new JCodeModel();
    private Map<DiscoveryKind, List<SrcFile>> discoveredKinds = new HashMap<>();
    private List<XMLFile> xmlFiles = new ArrayList<>();
    private File projectRoot;
    private String manifestPackage;

    public void addFile(File file) throws IOException {
        if (file.getName().toLowerCase().endsWith(".java")) {
            addParseJava(file);
        } else if (file.getName().toLowerCase().endsWith(".xml")) {
            addParseXml(file);
        }
    }

    public void setManifestPackage(String manifestPackage) {
        this.manifestPackage = manifestPackage;
    }

    public String getManifestPackage() {
        return manifestPackage;
    }

    public File getProjectRoot() {
        return projectRoot;
    }

    public void setProjectRoot(File file) {
        projectRoot = file;
    }

    private void addParseXml(File file) {
        System.out.println("found " + file);
        try {
            try (FileInputStream in = new FileInputStream(file)) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputSource is = new InputSource(in);
                Document doc = builder.parse(is);
                xmlFiles.add(new XMLFile(file, doc));
                NodeList nl = doc.getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    System.out.println(nl.item(i));
                }

            }
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("failed to parse {}", file, e);
        }
    }

    private void addParseJava(File file) {
        try {
            try (FileInputStream in = new FileInputStream(file)) {

                CompilationUnit cu = JavaParser.parse(in);
                units.add(new SrcFile(file, cu));

            }
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("failed to parse {}", file, e);
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

    public void clearDir(File dir) throws IOException {
        delete(dir);
    }

    static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files == null) {
                return;
            }
            for (File c : files)
                delete(c);
        }
        if (!f.delete() && f.exists())
            throw new FileNotFoundException("Failed to delete file: " + f);
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
                if (file.isFile()) {
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

    public List<SrcFile> getSrcFiles() {
        return units;
    }

    public List<XMLFile> getXmlFiles() {
        return xmlFiles;
    }

    public Map<DiscoveryKind, List<SrcFile>> getDiscoveredKinds() {
        return discoveredKinds;
    }

    public void generate() throws Exception {
        new GenerateAutoDiscovery().generate(this);
        new GenerateAsyncControllers().generate(this);
        new GenerateRequestFactories().generate(this);
        new GenerateViewsFromXML().generate(this);
    }

    public void emitGeneratedClass(File targetDir) throws IOException {
        codeModel.build(targetDir, System.out);
    }

    public String getDisclaimer(Class<?> clazz) {
        return "DO NOT MODIFY - GENERATED BY " + clazz.getName();
    }
}
