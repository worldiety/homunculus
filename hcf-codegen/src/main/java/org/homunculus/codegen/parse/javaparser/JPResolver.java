package org.homunculus.codegen.parse.javaparser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

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
                    res.add(new JPMethod(td, m, isDeclared));
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

}
