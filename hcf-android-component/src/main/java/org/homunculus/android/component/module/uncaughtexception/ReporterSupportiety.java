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
package org.homunculus.android.component.module.uncaughtexception;

import org.homunculus.android.component.module.uncaughtexception.Supportiety.ApplicationDetails;
import org.homunculus.android.component.module.uncaughtexception.Supportiety.Ticket;
import org.homunculusframework.concurrent.Async;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.lang.Result;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A default implementation for the worldiety supportiety crash logging system.
 * <p>
 * Features:
 * <ul>
 * <li>Appends each {@link java.io.File} or {@link java.io.InputStream} as a blob</li>
 * <li>Appends each {@link Throwable} as trace</li>
 * <li>Everything else is converted to a string and appended also as a blob</li>
 * <li>Null values are ignored entirely</li>
 * </ul>
 *
 * @author Torben Schinke
 * @since 1.0
 */

public class ReporterSupportiety implements Reporter {


    private final String endpoint;
    private final String clientId;
    private final String clientSecret;
    private final ApplicationDetails details;

    public ReporterSupportiety(String endpoint, String clientId, String clientSecret, ApplicationDetails details) {
        this.endpoint = endpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.details = details;
    }

    @Override
    public Task<Result<String>> report(Scope scope, Map<String, Object> crashData) {
//        return Async.inThread(scope, ctx -> {
//            try {
//                Supportiety client = Supportiety.createCustom(endpoint, clientId, clientSecret);
//                Ticket ticket = client.createTicket(details);
//                for (Entry<String, Object> entry : crashData.entrySet()) {
//                    if (entry.getKey() != null && entry.getValue() != null) {
//                        try {
//                            if (entry.getValue() instanceof Throwable) {
//                                client.appendTrace(ticket, (Throwable) entry.getValue());
//                            }
//                        } catch (Exception e) {
//                            LoggerFactory.getLogger(getClass()).error("failed to write throwable", e);
//                        }
//
//                        try {
//                            if (entry.getValue() instanceof File) {
//                                try (FileInputStream fin = new FileInputStream((File) entry.getValue())) {
//                                    client.appendBlob(ticket, entry.getKey(), fin);
//                                }
//                            }
//                        } catch (Throwable t) {
//                            LoggerFactory.getLogger(getClass()).error("failed to write file", t);
//                        }
//
//                        try {
//                            if (entry.getValue() instanceof InputStream) {
//                                try (InputStream in = (InputStream) entry.getValue()) {
//                                    client.appendBlob(ticket, entry.getKey(), in);
//                                }
//                            }
//                        } catch (Exception t) {
//                            LoggerFactory.getLogger(getClass()).error("failed to write file", t);
//                        }
//
//                    }
//                }
//                insertCustomData(client, ticket, crashData);
//                return Result.create(ticket.getId());
//            } catch (Throwable t) {
//                return Result.auto(t);
//            }
//        });
        throw new Panic("Not yet implemented");
    }

    protected void insertCustomData(Supportiety server, Ticket ticket, Map<String, Object> crashData) {
        try {
            Process logcat = Runtime.getRuntime().exec(new String[]{
                    "/system/bin/logcat", "-d"     // never have seen logcat not to reside in /system/bin
            });
            InputStream in = logcat.getInputStream();
            try {
                server.appendBlob(ticket, "logcat.log", in);
            } finally {
                in.close();
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("failed to grab logcat", e);
        }
    }
}
