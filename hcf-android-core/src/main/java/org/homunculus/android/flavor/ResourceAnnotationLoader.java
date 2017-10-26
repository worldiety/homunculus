/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.homunculus.android.flavor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.homunculusframework.factory.container.AnnotatedFieldProcessor;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;


/**
 * The processor to support the {@link Resource} annotation.
 * Actually does some weired things and picks the last unique child and resolves the context from there.
 * By doing this we can ensure that we grab the correct context (e.g. correctly themed) so that inflated
 * layouts and views also have the correct theme applied.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class ResourceAnnotationLoader implements AnnotatedFieldProcessor {


    @Override
    public void process(Scope scope, Object instance, Field field) {
        Resource resource = field.getAnnotation(Resource.class);
        if (resource == null) {
            return;
        }

        Context context = scope.resolve("$context", Context.class);
        if (context == null) {
            context = scope.resolve(Context.class);
        }
        if (context == null) {
            LoggerFactory.getLogger(instance.getClass()).error("missing Android context, cannot populate {}.{}", instance.getClass().getSimpleName(), field.getName());
            return;
        }

        try {
            field.setAccessible(true);
            if (field.getType() == String.class) {
                field.set(instance, context.getResources().getString(resource.value()));
            } else if (field.getType() == Bitmap.class) {
                field.set(instance, BitmapFactory.decodeResource(context.getResources(), resource.value()));
            } else if (field.getType() == Drawable.class) {
                field.set(instance, context.getResources().getDrawable(resource.value()));
            } else if (field.getType() == View.class) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                View view = layoutInflater.inflate(resource.value(), (ViewGroup) null, false);
                field.set(instance, view);
            } else {
                LoggerFactory.getLogger(instance.getClass()).error("unsupported resource {}.{}", instance.getClass().getSimpleName(), field.getName());
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(instance.getClass()).error("failed to inject resource {} into {}.{} -> {}", resource.value(), instance.getClass().getSimpleName(), field.getName(), e);
        }

    }
}
