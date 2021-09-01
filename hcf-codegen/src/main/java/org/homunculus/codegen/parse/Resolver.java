package org.homunculus.codegen.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;


/**
 * Created by Torben Schinke on 09.03.18.
 */

public interface Resolver {

    /**
     * Tries to determine the common super types. The most valuable super type comes first, interface order is undefined.
     */
    default List<FullQualifiedName> resolveCommonSuperTypes(List<FullQualifiedName> names) throws ClassNotFoundException {
        List<FullQualifiedName>[] hierarchies = new List[names.size()];
        for (int i = 0; i < hierarchies.length; i++) {
            hierarchies[i] = new ArrayList<>();
            getSuperTypes(names.get(i), hierarchies[i]);
        }
        //n^4 yikes? naiv loop to remove all those entries which are not also in all other hierarchies
        for (int h = 0; h < hierarchies.length; h++) {
            List<FullQualifiedName> list = hierarchies[h];
            Iterator<FullQualifiedName> it = list.iterator();
            while (it.hasNext()) {
                FullQualifiedName fqn = it.next();
                for (List<FullQualifiedName> other : hierarchies) {
                    if (other == list) {
                        continue;
                    }
                    if (!other.contains(fqn)) {
                        it.remove();
                    }
                }
            }
        }
//        for (int i = 0; i < names.size(); i++) {
//            System.out.println(names.get(i) + " is a ");
//            for (FullQualifiedName fqn : hierarchies[i]) {
//                System.out.println("   -" + fqn);
//            }
//        }

        if (hierarchies[0].size() > 0) {
            return hierarchies[0];
        }

        return Collections.singletonList(new FullQualifiedName(Object.class));
    }

    /**
     * Returns all super types and implemented interfaces.
     */
    void getSuperTypes(FullQualifiedName name, List<FullQualifiedName> dst) throws ClassNotFoundException;

    boolean has(FullQualifiedName name);

    List<Constructor> getConstructors(FullQualifiedName name) throws ClassNotFoundException;

    List<Method> getMethods(FullQualifiedName name) throws ClassNotFoundException;

    List<Field> getFields(FullQualifiedName name) throws ClassNotFoundException;

    /**
     * Returns all available types.
     */
    List<FullQualifiedName> getTypes();

    boolean isAbstract(FullQualifiedName name);

    boolean isPublic(FullQualifiedName name);

    boolean isPrivate(FullQualifiedName name);

    boolean isTopLevelType(FullQualifiedName name);

    default boolean isDefault(FullQualifiedName name) {
        return !isPublic(name) && !isPrivate(name);
    }

    boolean isStatic(FullQualifiedName name);

    boolean isNested(FullQualifiedName name);

    List<Annotation> getAnnotations(FullQualifiedName name);

    @Nullable
    default Annotation getAnnotation(FullQualifiedName type, FullQualifiedName annotation) {
        for (Annotation a : getAnnotations(type)) {
            if (a.getFullQualifiedName().equals(annotation)) {
                return a;
            }
        }
        return null;
    }

    @Nullable
    default Annotation getAnnotation(FullQualifiedName type, Class annotation) {
        return getAnnotation(type, new FullQualifiedName(annotation));
    }

    boolean isInstanceOf(FullQualifiedName which, FullQualifiedName what);
}
