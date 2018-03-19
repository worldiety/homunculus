package org.homunculus.codegen;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.util.JCSecureLoader;

import org.homunculus.android.component.module.toolbarbuilder.SuperToolbar2;
import org.homunculus.codegen.generator.GenerateBindables;
import org.homunculus.codegen.generator.GenerateScopes;
import org.homunculus.codegen.generator.PreprocessDiscoverBeans;
import org.homunculus.codegen.generator.PreprocessDiscoverBeans.DiscoveryKind;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Resolver;
import org.homunculus.codegen.parse.javaparser.JPResolver;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import sun.reflect.CallerSensitive;

/**
 * Created by Torben Schinke on 20.02.18.
 */

public class GenProject {

    private List<org.homunculus.codegen.parse.javaparser.SrcFile> units = new ArrayList<>();
    private JCodeModel codeModel = new JCodeModel();
    private Map<DiscoveryKind, Set<FullQualifiedName>> discoveredKinds = new HashMap<>();
    private List<XMLFile> xmlFiles = new ArrayList<>();
    private File projectRoot;
    private String manifestPackage;
    private Resolver resolver;


    public GenProject(){
       // System.out.println(SuperToolbar2.class);
       // JCSecureLoader.setContextClassLoader(new MyFixedContextClassLoader(Thread.currentThread().getContextClassLoader()));
    }
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
//        System.out.println("found " + file);
        try {
            try (FileInputStream in = new FileInputStream(file)) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputSource is = new InputSource(in);
                Document doc = builder.parse(is);
                xmlFiles.add(new XMLFile(file, doc));
//                NodeList nl = doc.getChildNodes();
//                for (int i = 0; i < nl.getLength(); i++) {
//                    System.out.println(nl.item(i));
//                }

            }
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("failed to parse {}", file, e);
        }
    }

    private void addParseJava(File file) {
        try {
            try (FileInputStream in = new FileInputStream(file)) {

                CompilationUnit cu = JavaParser.parse(in);
                units.add(new org.homunculus.codegen.parse.javaparser.SrcFile(file, cu));

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
    public org.homunculus.codegen.parse.javaparser.SrcFile findSourceFileForType(String fqn) {
        for (org.homunculus.codegen.parse.javaparser.SrcFile file : units) {
            if (file.getFullQualifiedNamePrimaryClassName().equals(fqn)) {
                return file;
            }
        }
        return null;
    }

    public JCodeModel getCodeModel() {
        return codeModel;
    }

    public List<org.homunculus.codegen.parse.javaparser.SrcFile> getSrcFiles() {
        return units;
    }

    public List<XMLFile> getXmlFiles() {
        return xmlFiles;
    }

    public Map<DiscoveryKind, Set<FullQualifiedName>> getDiscoveredKinds() {
        return discoveredKinds;
    }

    public void generate() throws Exception {
        resolver = new JPResolver(units);
        new PreprocessDiscoverBeans().generate(this);
        new GenerateScopes().generate(this);
        new GenerateBindables().generate(this);
//        new GenerateAsyncControllers().generate(this);
//        new GenerateMethodBindings().generate(this);
//        new GenerateTaskMethods().generate(this);
//        new GenerateObjectBindings().generate(this);
//        new GenerateControllerFactory().generate(this);
//        new GenerateViewsFromXML().generate(this);
    }

    public Resolver getResolver() {
        return resolver;
    }

    public void emitGeneratedClass(File targetDir) throws IOException {
        codeModel.build(targetDir, System.out);
    }

    public String getDisclaimer(Class<?> clazz) {
        return "DO NOT MODIFY - GENERATED BY " + clazz.getName();
    }

//    public String getJavadocReference(org.homunculus.codegen.parse.javaparser.SrcFile file, MethodDeclaration decl) {
//        ClassOrInterfaceDeclaration dec = (ClassOrInterfaceDeclaration) decl.getParentNode().get();
//
//        StringBuilder sb = new StringBuilder();
//        sb.append(file.getPackageName());
//        sb.append(".").append(dec.getNameAsString()).append("#");
//        sb.append(decl.getName());
//        sb.append("(");
//        for (Parameter p : decl.getParameters()) {
//            sb.append(file.getFullQualifiedName(p.getType().asString()));
//            sb.append(",");
//        }
//        if (sb.charAt(sb.length() - 1) == ',') {
//            sb.setLength(sb.length() - 1);
//        }
//        sb.append(")");
//        return sb.toString();
//    }


    private static class MyFixedContextClassLoader extends ClassLoader{
        private final ClassLoader delegate;

        public MyFixedContextClassLoader(ClassLoader delegate) {
            this.delegate = delegate;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            try {
                return delegate.loadClass(name);
            }catch (NoClassDefFoundError e){
                throw new ClassNotFoundException(name+" due to "+e.getClass().getName()+": "+e.getMessage());
            }
        }




        @org.jetbrains.annotations.Nullable
        @Override
        public URL getResource(String name) {
            return delegate.getResource(name);
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            return delegate.getResources(name);
        }



        @CallerSensitive
        public static boolean registerAsParallelCapable() {
            return ClassLoader.registerAsParallelCapable();
        }

        public static URL getSystemResource(String name) {
            return ClassLoader.getSystemResource(name);
        }

        public static Enumeration<URL> getSystemResources(String name) throws IOException {
            return ClassLoader.getSystemResources(name);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            return delegate.getResourceAsStream(name);
        }

        public static InputStream getSystemResourceAsStream(String name) {
            return ClassLoader.getSystemResourceAsStream(name);
        }

        @CallerSensitive
        public static ClassLoader getSystemClassLoader() {
            return ClassLoader.getSystemClassLoader();
        }



        @Override
        public void setDefaultAssertionStatus(boolean enabled) {
            delegate.setDefaultAssertionStatus(enabled);
        }

        @Override
        public void setPackageAssertionStatus(String packageName, boolean enabled) {
            delegate.setPackageAssertionStatus(packageName, enabled);
        }

        @Override
        public void setClassAssertionStatus(String className, boolean enabled) {
            delegate.setClassAssertionStatus(className, enabled);
        }

        @Override
        public void clearAssertionStatus() {
            delegate.clearAssertionStatus();
        }
    }
}
