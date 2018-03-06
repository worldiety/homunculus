package org.homunculus.codegen;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.io.File;

import javax.annotation.Nullable;

/**
 * Created by Torben Schinke on 20.02.18.
 */

public class SrcFile {
    private final File file;
    private final CompilationUnit unit;

    public SrcFile(File file, CompilationUnit unit) {
        this.file = file;
        this.unit = unit;
    }

    public CompilationUnit getUnit() {
        return unit;
    }

    public File getFile() {
        return file;
    }

    public String getPrimaryClassName() {
        return getFile().getName().substring(0, getFile().getName().length() - 5);
    }

    /**
     * Tries to simply resolve a name to a full qualified name based on the current imports
     *
     * @param name a name like Singleton or javax.inject.Singleton
     * @return e.g. javax.inject.Singleton
     */
    public String getFullQualifiedName(String name) {
        if (name.contains(".")) {
            return name;//not correct for partial imports e.g. of inner classes
        } else {
            //simply match it against the import
            for (ImportDeclaration imp : unit.getImports()) {
                if (imp.getNameAsString().endsWith(name)) {
                    return imp.getNameAsString();
                }
            }
        }
        if (unit.getPackageDeclaration().isPresent()) {
            //local class with package
            if (unit.getClassByName(name).isPresent()) {
                return unit.getPackageDeclaration().get().getNameAsString() + "." + name;
            } else {
                //native type
                return name;
            }
        } else {
            //local class without package
            return name;
        }
    }

    public String getFullQualifiedName(Type type) {
        if (type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType itype = (ClassOrInterfaceType) type;
            StringBuilder str = new StringBuilder();
            itype.getScope().ifPresent(s -> str.append(s.asString()).append("."));
            str.append(itype.getName().asString());
            return getFullQualifiedName(str.toString());
        } else {
            return type.toString();
        }
    }

    public String getFullQualifiedNamePrimaryClassName() {
        if (unit.getPackageDeclaration().isPresent()) {
            return unit.getPackageDeclaration().get().getNameAsString() + "." + getPrimaryClassName();
        } else {
            return getPrimaryClassName();
        }
    }

    public String getPackageName() {
        if (unit.getPackageDeclaration().isPresent()) {
            return unit.getPackageDeclaration().get().getNameAsString();
        } else {
            return "";
        }
    }

    @Nullable
    public AnnotationExpr getAnnotation(ClassOrInterfaceDeclaration type, String fqn) {
        for (AnnotationExpr a : type.getAnnotations()) {
            String aFqn = getFullQualifiedName(a.getNameAsString());
            if (aFqn.equals(fqn)) {
                return a;
            }
        }
        return null;
    }

    @Nullable
    public AnnotationExpr getAnnotation(ClassOrInterfaceDeclaration type, Class annotation) {
        return getAnnotation(type, annotation.getName());
    }
}
