package org.homunculus.codegen.parse.javaparser;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.Constructor;
import org.homunculus.codegen.parse.Field;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Method;
import org.homunculus.codegen.parse.Resolver;
import org.homunculus.codegen.parse.reflection.ReflectionResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by Torben Schinke on 09.03.18.
 */

public class JPResolver implements Resolver {
    private List<SrcFile> srcFiles;
    private Map<FullQualifiedName, TypeContext> typeTree = new HashMap<>();
    private ReflectionResolver reflection = new ReflectionResolver();

    public JPResolver(List<SrcFile> srcFiles) {
        this.srcFiles = srcFiles;
        for (SrcFile file : srcFiles) {
            for (TypeDeclaration td : file.getUnit().getTypes()) {
                FullQualifiedName fqn = new FullQualifiedName(file.getFullQualifiedName(td.getNameAsString()));
                typeTree.put(fqn, new TypeContext(file, td));
//                System.out.println(fqn);
            }
        }
    }

    @Override
    public List<Constructor> getConstructors(FullQualifiedName name) throws ClassNotFoundException {
        TypeContext tc = typeTree.get(name);
        if (tc == null) {
            throw new ClassNotFoundException(name.toString());
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
        return typeTree.get(name).type.getModifiers().contains(Modifier.ABSTRACT);
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
                    superType = new FullQualifiedName(td.src.getPackageName() + "." + superTypeName);
                    td = typeTree.get(superType);
                    if (td == null) {
                        try {
                            res.addAll(reflection.getMethods(superType));
                        } catch (ClassNotFoundException e) {
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
    public boolean isInstanceOf(FullQualifiedName which, FullQualifiedName what) {
        TypeContext root = typeTree.get(which);
        String startingPoint = which.toString();
        while (root != null) {
            String superTypeName = root.type.asClassOrInterfaceDeclaration().getExtendedTypes().get(0).getNameAsString();
            FullQualifiedName superType = new FullQualifiedName(root.src.getPackageName() + "." + superTypeName);
            if (superType.equals(what)) {
                return true;
            }
            root = typeTree.get(superType);
            startingPoint = superType.toString();
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
            System.out.println("IsInstanceOf cannot resolve class " + what);
            return false;
        }

        return false;
    }
}
