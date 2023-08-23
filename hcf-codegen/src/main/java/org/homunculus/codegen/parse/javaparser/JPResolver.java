package org.homunculus.codegen.parse.javaparser;

import android.app.Activity;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.Constructor;
import org.homunculus.codegen.parse.Field;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Method;
import org.homunculus.codegen.parse.Resolver;
import org.homunculus.codegen.parse.jcodemodel.JCodeModelResolver;
import org.homunculus.codegen.parse.reflection.ReflectionResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;


/**
 * Created by Torben Schinke on 09.03.18.
 */

public class JPResolver implements Resolver {
    private List<SrcFile> srcFiles;
    private Map<FullQualifiedName, TypeContext> typeTree = new HashMap<>();
    private ReflectionResolver reflection = new ReflectionResolver();
    private JCodeModelResolver codeResolver;

    final static Map<FullQualifiedName, FullQualifiedName> instanceOfTable;

    static {
        instanceOfTable = new HashMap<>();


        instanceOfTable.put(new FullQualifiedName("org.homunculus.android.component.HomunculusActivity"), new FullQualifiedName(Activity.class));
        instanceOfTable.put(new FullQualifiedName("org.homunculus.android.compat.EventAppCompatActivity"), new FullQualifiedName(Activity.class));
    }

    public JPResolver(JCodeModelResolver codeResolver, List<SrcFile> srcFiles) {
        this.srcFiles = srcFiles;
        this.codeResolver = codeResolver;
        for (SrcFile file : srcFiles) {
            file.setResolver(this);
            for (TypeDeclaration td : file.getUnit().getTypes()) {
                FullQualifiedName fqn = new FullQualifiedName(file.getFullQualifiedName(td.getNameAsString()));
                typeTree.put(fqn, new TypeContext(file, td, fqn));
//                System.out.println(fqn);
            }
        }
    }


    @Override
    public void getSuperTypes(FullQualifiedName name, List<FullQualifiedName> dst) throws ClassNotFoundException {
        List<FullQualifiedName> found = new ArrayList<>();
        List<FullQualifiedName> notFound = new ArrayList<>();
        List<FullQualifiedName> seeds = new ArrayList<>();

        seeds.add(name);
        //chaos resolving
        int retry = 0;
        while (retry < 20) {
            List<FullQualifiedName> tmp = new ArrayList<>(seeds);

            for (FullQualifiedName seed : tmp) {
                listTypes(seed, found, notFound);

                reflection.listTypes(seed, found, notFound);

                codeResolver.listTypes(seed, found, notFound);


            }

            unique(seeds);
            unique(found);
            unique(notFound);

            notFound.removeAll(found);

            seeds.clear();
            seeds.addAll(notFound);

            retry++;
        }

        dst.addAll(found);
        dst.addAll(notFound);

        unique(dst);

        dst.remove(new FullQualifiedName(Object.class));
        dst.add(new FullQualifiedName(Object.class));
    }

    private void unique(List<FullQualifiedName> list) {
        Set<FullQualifiedName> used = new HashSet<>();
        List<FullQualifiedName> tmp = new ArrayList<>(list.size());
        for (FullQualifiedName fqn : list) {
            if (!used.contains(fqn)) {
                tmp.add(fqn);
                used.add(fqn);
            }
        }
        list.clear();
        list.addAll(tmp);
    }

    public void listTypes(FullQualifiedName src, List<FullQualifiedName> found, List<FullQualifiedName> notFound) {
        TypeContext tc = typeTree.get(src);
        if (tc == null) {
            notFound.add(src);
        } else {
            if (tc.type instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration dec = (ClassOrInterfaceDeclaration) tc.type;

                for (ClassOrInterfaceType s : dec.getExtendedTypes()) {
                    FullQualifiedName fqn = new FullQualifiedName(tc.src.getFullQualifiedName(s.getNameAsString()));
                    listTypes(fqn, found, notFound);
                }

                for (ClassOrInterfaceType s : dec.getImplementedTypes()) {
                    FullQualifiedName fqn = new FullQualifiedName(tc.src.getFullQualifiedName(s.getNameAsString()));
                    listTypes(fqn, found, notFound);
                }
            }
        }
    }

    @Override
    public boolean has(FullQualifiedName name) {
        if (typeTree.containsKey(name)) {
            return true;
        }
        if (codeResolver.has(name)) {
            return true;
        }
        if (reflection.has(name)) {
            return true;
        }
        return false;
    }

    @Override
    public List<Constructor> getConstructors(FullQualifiedName name) throws ClassNotFoundException {
        TypeContext tc = typeTree.get(name);
        if (tc == null) {
            try {
                return reflection.getConstructors(name);
            } catch (ClassNotFoundException nfe) {
                return codeResolver.getConstructors(name);
            }
        }
        ClassOrInterfaceDeclaration dec = tc.src.getUnit().getClassByName(name.getSimpleName()).get();
        List<Constructor> res = new ArrayList<>();
        for (ConstructorDeclaration c : dec.getConstructors()) {
            res.add(new JPConstructor(tc, name, c));
        }
        return res;
    }

    @Override
    public List<FullQualifiedName> getTypes() {
        return new ArrayList<>(typeTree.keySet());
    }

    @Override
    public boolean isAbstract(FullQualifiedName name) {
        return typeTree.get(name).type.getModifiers().contains(Modifier.abstractModifier());
    }

    @Override
    public boolean isPublic(FullQualifiedName name) {
        return typeTree.get(name).type.isPublic();
    }

    @Override
    public boolean isStatic(FullQualifiedName name) {
        return typeTree.get(name).type.isStatic();
    }

    @Override
    public boolean isNested(FullQualifiedName name) {
        return typeTree.get(name).type.isNestedType();
    }

    @Override
    public boolean isPrivate(FullQualifiedName name) {
        return typeTree.get(name).type.isPrivate();
    }

    @Override
    public boolean isTopLevelType(FullQualifiedName name) {
        return typeTree.get(name).type.isTopLevelType();
    }

    @Override
    public List<Annotation> getAnnotations(FullQualifiedName name) {
        List<Annotation> res = new ArrayList<>();
        TypeContext ctx = typeTree.get(name);
        NodeList<AnnotationExpr> tmp = ctx.type.getAnnotations();
        for (AnnotationExpr a : tmp) {
            res.add(new JPAnnotation(ctx, new FullQualifiedName(ctx.src.getFullQualifiedName(a.getNameAsString())), a));
        }
        return res;
    }

    @Override
    @Nullable
    public List<Method> getMethods(FullQualifiedName name) throws ClassNotFoundException {
        TypeContext td = typeTree.get(name);
        if (td == null) {
            return reflection.getMethods(name);
        }
        List<Method> res = new ArrayList<>();
        boolean isDeclared = true;
        while (td != null) {
            if (td.type instanceof ClassOrInterfaceDeclaration) {
                for (MethodDeclaration m : td.type.asClassOrInterfaceDeclaration().getMethods()) {
                    res.add(new JPMethod(td, name, m, isDeclared));
                }
                if (td.type.asClassOrInterfaceDeclaration().getExtendedTypes().size() > 0) {
                    //the supertype is defined in the unit itself
                    String superTypeName = td.type.asClassOrInterfaceDeclaration().getExtendedTypes().get(0).getNameAsString();
                    FullQualifiedName superType;
                    superType = new FullQualifiedName(td.src.getFullQualifiedName(superTypeName));
                    td = typeTree.get(superType);
                    if (td == null) {
                        try {
                            res.addAll(reflection.getMethods(superType));
                        } catch (ClassNotFoundException e) {
                            //TODO
                            e.printStackTrace();
                            System.out.println("unable to resolve methods for: " + superType);
                        }
                        break;
                    }
                } else {
                    td = null;
                }
            }
            isDeclared = false;
        }
        return res;
    }


    @Override
    public List<Field> getFields(FullQualifiedName name) throws ClassNotFoundException {
        TypeContext td = typeTree.get(name);
        if (td == null) {
            return reflection.getFields(name);
        }
        List<Field> res = new ArrayList<>();
        boolean isDeclared = true;
        while (td != null) {
            if (td.type instanceof ClassOrInterfaceDeclaration) {
                for (FieldDeclaration m : td.type.asClassOrInterfaceDeclaration().getFields()) {
                    res.add(new JPField(td, m, isDeclared));
                }
                if (td.type.asClassOrInterfaceDeclaration().getExtendedTypes().size() > 0) {
                    //the supertype is defined in the unit itself
                    String superTypeName = td.type.asClassOrInterfaceDeclaration().getExtendedTypes().get(0).getNameAsString();
                    FullQualifiedName superType;
                    superType = new FullQualifiedName(td.src.getPackageName() + "." + superTypeName);
                    td = typeTree.get(superType);
                    if (td == null) {
                        try {
                            res.addAll(reflection.getFields(superType));
                        } catch (ClassNotFoundException e) {
                            //TODO
//                            System.out.println("unable to resolve methods for: " + superType);
                        }
                        break;
                    }
                } else {
                    td = null;
                }
            } else {
                td = null;
            }
            isDeclared = false;
        }
        return res;
    }

    @Override
    public boolean isInstanceOf(FullQualifiedName which, FullQualifiedName what) {
        if (which.equals(what)) {
            return true;
        }
        if (what.equals(instanceOfTable.get(which))) {
            return true;
        }

        TypeContext root = typeTree.get(which);
        String startingPoint = which.toString();
        while (root != null) {
            if (!(root.type instanceof ClassOrInterfaceDeclaration)) {
                //TODO not correct for enum super type
                root = null;
                continue;
            }
            if (root.type.asClassOrInterfaceDeclaration().getExtendedTypes().isEmpty()) {
                root = null;
                continue;
            }
            String superTypeName = root.type.asClassOrInterfaceDeclaration().getExtendedTypes().get(0).getNameAsString();
            FullQualifiedName superType = new FullQualifiedName(root.src.getFullQualifiedName(superTypeName));
            if (superType.equals(what)) {
                return true;
            }
            root = typeTree.get(superType);
            startingPoint = superType.toString();
//            System.out.println("??" + superType + " is a " + what);

            if (what.equals(instanceOfTable.get(superType))) {
                return true;
            }
        }
        if (what.equals(new FullQualifiedName(startingPoint))) {
            return true;
        }
        //if we got here either "which" inherits a classpath class or is undefined
        //what contains now the class to resolve
        try {
            Class reflectionRoot = Class.forName(startingPoint);
            while (reflectionRoot != null) {
                if (new FullQualifiedName(reflectionRoot).equals(what)) {
                    return true;
                }
                reflectionRoot = reflectionRoot.getSuperclass();
            }
        } catch (ClassNotFoundException e) {
//            System.out.println("reflection: not found " + startingPoint);
            List<FullQualifiedName> superTypes = new ArrayList<>();

            codeResolver.listTypes(new FullQualifiedName(startingPoint), superTypes, superTypes);
            for (FullQualifiedName fqnSuperType : superTypes) {
                if (fqnSuperType.equals(what)) {
                    return true;
                }
            }
//            System.out.println("IsInstanceOf cannot resolve class " + what + " (" + which + ")");
            return false;
        } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
//            System.out.println("NOCLASSDEF: IsInstanceOf cannot resolve class " + what + " (" + which + ")");
            return false;
        }

        return false;
    }
}
