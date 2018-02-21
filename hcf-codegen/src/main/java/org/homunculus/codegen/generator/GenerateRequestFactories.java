package org.homunculus.codegen.generator;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import org.homunculus.codegen.Generator;
import org.homunculus.codegen.Project;
import org.homunculus.codegen.SrcFile;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.factory.flavor.hcf.FactoryParam;
import org.homunculusframework.navigation.ModelAndView;
import org.homunculusframework.navigation.Navigation;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Loops over all classes and creates factories for each pojo which meet the following conditions:
 * <ul>
 * <li>Is not abstract</li>
 * <li>Is a class</li>
 * <li>Contains {@link javax.inject.Inject} fields with annotated {@link org.homunculusframework.factory.flavor.hcf.FactoryParam}</li>
 * </ul>
 * <p>
 * Created by Torben Schinke on 20.02.18.
 */
public class GenerateRequestFactories implements Generator {
    @Override
    public void generate(Project project) throws Exception {
        for (SrcFile src : project.getUnits()) {
            Optional<ClassOrInterfaceDeclaration> optDec = src.getUnit().getClassByName(src.getPrimaryClassName());
            if (!optDec.isPresent()) {
                LoggerFactory.getLogger(getClass()).warn("ignored file {}", src.getFile());
                continue;
            }
            ClassOrInterfaceDeclaration dec = optDec.get();
            if (!dec.isPublic() || dec.isNestedType() || dec.isAbstract() || dec.isInterface()) {
                continue;
            }

            String beanName = null;
            for (AnnotationExpr annotation : dec.getAnnotations()) {
                String aFqn = src.getFullQualifiedName(annotation.getNameAsString());
                if (aFqn.equals(Named.class.getName())) {
                    if (annotation instanceof SingleMemberAnnotationExpr) {
                        SingleMemberAnnotationExpr expr = ((SingleMemberAnnotationExpr) annotation);
                        if (expr.getMemberValue() instanceof StringLiteralExpr) {
                            beanName = ((StringLiteralExpr) expr.getMemberValue()).getValue();
                        } else if (expr.getMemberValue() instanceof NameExpr) {
                            NameExpr nameExpr = ((NameExpr) expr.getMemberValue());
                            String value = resolveStaticFieldValue(src, nameExpr.getNameAsString());
                            if (value == null) {
                                LoggerFactory.getLogger(getClass()).warn("constant evaluation not supported: {}", nameExpr);
                            } else {
                                beanName = value;
                            }
                        }
                    }
                }
            }

            List<Field> fields = new ArrayList<>();
            collectFields(project, src, dec, fields);

            if (!fields.isEmpty()) {
                Class[] factoryKinds = {Request.class, ModelAndView.class};
                JDefinedClass jc = project.getCodeModel()._package(src.getPackageName())._class(src.getPrimaryClassName() + "Factory");
                jc.javadoc().add("Contains factory methods which are generated automatically by using all members which are annotated with {@link " + FactoryParam.class.getName() + "}. Consider using {@link " + Named.class + "} to uniquely identify parameters.");
                jc.headerComment().add(project.getDisclaimer(getClass()));
                jc.constructor(JMod.PRIVATE);
                for (Class clazz : factoryKinds) {
                    JMethod meth = jc.method(JMod.PUBLIC | JMod.STATIC, clazz, "create" + clazz.getSimpleName());
                    meth.javadoc().add("Creates a " + clazz.getSimpleName() + " to create a {@link " + src.getPrimaryClassName() + "}. ");
                    if (clazz == Request.class) {
                        meth.javadoc().add("Use this directly for navigation e.g. by calling {@link " + Navigation.class.getName() + "#forward(Request)}.");
                    } else {
                        meth.javadoc().add("Use this as a result from a controller method. The direction of navigation is usually determined by the appropriate Request to the controller method.");
                    }
                    for (Field f : fields) {
                        String fqnType = f.file.getFullQualifiedName(f.dec.getElementType().asString());
                        JVar var = meth.param(project.getCodeModel().ref(fqnType), f.getFieldName());
                        if (f.isNullable) {
                            var.annotate(Nullable.class);
                        }
                    }

                    StringBuilder tmp = new StringBuilder();
                    tmp.append("new " + clazz.getSimpleName() + "(");
                    if (beanName == null) {
                        tmp.append(src.getFullQualifiedNamePrimaryClassName()).append(".class");
                    } else {
                        tmp.append("\"").append(beanName).append("\"");
                    }

                    tmp.append(").");

                    for (Field f : fields) {
                        tmp.append("put(\"").append(f.getFieldName()).append("\", ").append(f.getFieldName()).append(").");
                    }
                    tmp.setLength(tmp.length() - 1);
                    meth.body()._return(JExpr.direct(tmp.toString()));

                }
            }
        }
    }


    private void collectFields(Project project, SrcFile src, ClassOrInterfaceDeclaration dec, List<Field> dst) {
        if (dec.getExtendedTypes().size() > 0) {
            LoggerFactory.getLogger(getClass()).warn("not implemented: {}", dec.getExtendedTypes());
        }
        for (FieldDeclaration field : dec.getFields()) {
            Field tmp = new Field(src, field);
            for (AnnotationExpr annotation : field.getAnnotations()) {
                String aFqn = src.getFullQualifiedName(annotation.getNameAsString());
                if (aFqn.equals(Inject.class.getName()) || aFqn.equals(Autowired.class.getName())) {
                    tmp.isInjectable = true;
                    continue;
                }
                if (aFqn.equals(FactoryParam.class.getName())) {
                    tmp.isFactoryParam = true;
                }
                if (aFqn.equals(Nullable.class.getName())) {
                    tmp.isNullable = true;
                }

                if (aFqn.equals(Named.class.getName())) {
                    if (annotation instanceof SingleMemberAnnotationExpr) {
                        SingleMemberAnnotationExpr expr = ((SingleMemberAnnotationExpr) annotation);
                        if (expr.getMemberValue() instanceof StringLiteralExpr) {
                            tmp.alternateName = ((StringLiteralExpr) expr.getMemberValue()).getValue();
                        } else if (expr.getMemberValue() instanceof NameExpr) {
                            NameExpr nameExpr = ((NameExpr) expr.getMemberValue());
                            String value = resolveStaticFieldValue(src, nameExpr.getNameAsString());
                            if (value == null) {
                                LoggerFactory.getLogger(getClass()).warn("constant evaluation not supported: {}", nameExpr);
                            } else {
                                tmp.alternateName = value;
                            }
                        }
                    }
                }
            }
            if (tmp.isInjectable && tmp.isFactoryParam) {
                dst.add(tmp);
            }
        }
    }

    @Nullable
    private static String resolveStaticFieldValue(SrcFile src, String fieldName) {
        for (FieldDeclaration f : src.getUnit().getClassByName(src.getPrimaryClassName()).get().getFields()) {
            if (f.getVariables().size() > 0) {
                VariableDeclarator var = f.getVariables().get(0);
                if (var.getNameAsString().equals(fieldName)) {
                    if (var.getInitializer().isPresent()) {
                        if (var.getInitializer().get() instanceof StringLiteralExpr) {
                            return var.getInitializer().get().asStringLiteralExpr().asString();
                        }

                    }
                }
            }
        }
        return null;
    }


    private static class Field {
        private final SrcFile file;
        private final FieldDeclaration dec;
        boolean isInjectable = false;
        boolean isFactoryParam = false;
        boolean isNullable = false;
        String alternateName = null;

        public Field(SrcFile file, FieldDeclaration dec) {
            this.file = file;
            this.dec = dec;
        }

        public String getFieldName() {
            if (alternateName == null) {
                return dec.getVariables().get(0).getNameAsString();
            } else {
                return alternateName;
            }
        }
    }
}
