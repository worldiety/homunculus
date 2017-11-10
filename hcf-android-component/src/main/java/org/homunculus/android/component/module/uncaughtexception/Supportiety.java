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

import org.homunculusframework.lang.Panic;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * All functionality is intentionally kept inside this single class and also it has only little dependencies (json.org). Moreover it is compatible to Java 1.6 and Android.
 * <p>
 * <p>
 * Changelog
 * <p>
 * Version 1_2016-06-06: initial implementation
 * <p>
 * Created by tschinke on 06.07.16.
 */
public final class Supportiety {

    private final static String API_CREATE = "1.0/receiver/ticket";
    private final static String API_UPLOAD = "1.0/receiver/ticket/{secret}";

    private final String endpointURL;
    private final String clientId;
    private final String clientSecret;


    private Supportiety(String endpointURL, String clientId, String clientSecret) {
        if (!endpointURL.endsWith("/")) {
            endpointURL = endpointURL + "/";
        }
        this.endpointURL = endpointURL;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }


    public static Supportiety createCustom(String endpointURL, String clientId, String clientSecret) {
        return new Supportiety(endpointURL, clientId, clientSecret);
    }


    /**
     * Creates a new ticket in supportiety.
     *
     * @param details
     * @return
     * @throws IOException
     */
    public Ticket createTicket(ApplicationDetails details) throws IOException, ProtocolException {
        JSONObject json = new JSONObject();
        try {
            json.put("applicationId", details.getApplicationId());
            json.put("versionName", details.getVersionName());
            json.put("versionNum", details.getVersionNum());
        } catch (JSONException e) {
            throw new Panic(e);
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(endpointURL + API_CREATE).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("clientId", clientId);
        conn.setRequestProperty("clientSecret", clientSecret);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.getOutputStream().write(json.toString().getBytes("UTF-8"));

        InputStream in;
        try {
            in = conn.getInputStream();
        } catch (IOException e) {
            in = conn.getErrorStream();
        }
        Scanner s = new Scanner(in).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        try {
            JSONObject resp = new JSONObject(result);
            return new Ticket(resp.getString("ticketId"), resp.getString("ticketSecretId"));
        } catch (JSONException e) {
            throw new ProtocolException(result, e);
        }
    }

    /**
     * Appends a throwable to the given ticket, so that it get's categorized as a crash.
     */
    public TicketFile appendTrace(Ticket ticket, Throwable t) throws IOException, ProtocolException {
        return appendBlob(ticket, "trace.json", new ByteArrayInputStream(asTrace(t).toString().getBytes("UTF-8")));
    }

    /**
     * Appends any file
     */
    public TicketFile appendBlob(Ticket ticket, String name, InputStream src) throws IOException, ProtocolException {
        HttpURLConnection conn = (HttpURLConnection) new URL(endpointURL + API_UPLOAD.replace("{secret}", ticket.getSecret())).openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("clientId", clientId);
        conn.setRequestProperty("clientSecret", clientSecret);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("ticketSecretId", ticket.getSecret());
        conn.setRequestProperty("blobName", name);
        conn.setDoOutput(true);
        OutputStream out = conn.getOutputStream();
        byte[] buf = new byte[8192];
        int read;
        while ((read = src.read(buf)) != -1) {
            out.write(buf, 0, read);
        }

        InputStream in;
        try {
            in = conn.getInputStream();
        } catch (IOException e) {
            in = conn.getErrorStream();
        }
        Scanner s = new Scanner(in).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        try {
            JSONObject resp = new JSONObject(result);
            return new TicketFile(resp.getString("ticketId"), resp.getString("sha2"), resp.getLong("fileId"), resp.getString("sha2"), resp.getLong("size"));
        } catch (JSONException e) {
            throw new ProtocolException(result, e);
        }
    }

    private JSONObject asTrace(Throwable t) {
        JSONObject res = new JSONObject();
        try {
            insert(res, t);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    private void insert(JSONObject obj, Throwable throwable) throws JSONException {
        obj.put("msg", throwable.getMessage());
        obj.put("type", throwable.getClass().getName());
        insertStacktrace(obj, throwable.getStackTrace());
        try {
            Throwable[] suppressed = throwable.getSuppressed();
            if (suppressed != null && suppressed.length > 0) {
                JSONArray supp = new JSONArray();
                for (Throwable t : suppressed) {
                    JSONObject dst = new JSONObject();
                    insert(dst, t);
                    supp.put(dst);
                }
                obj.put("suppressed", supp);
            }
        } catch (Error e) {
            //intentionally ignored
            System.err.println("no suppressed support");
        }

        if (throwable.getCause() != null) {
            JSONObject causedBy = new JSONObject();
            insert(causedBy, throwable.getCause());
            obj.put("causedBy", causedBy);
        }
    }

    private void insertStacktrace(JSONObject obj, StackTraceElement[] stackTraceElements) throws JSONException {
        JSONArray trace = new JSONArray();
        for (StackTraceElement elem : stackTraceElements) {
            JSONObject stack = new JSONObject();
            stack.put("c", elem.getClassName());
            stack.put("m", elem.getMethodName());
            stack.put("f", elem.getFileName());
            stack.put("l", elem.getLineNumber());
            stack.put("n", elem.isNativeMethod());
            trace.put(stack);
        }
        obj.put("trace", trace);
    }

    public final static class TicketFile {
        private final String ticketId;
        private final String ticketSecret;
        private final long id;
        private final String sha2;
        private final long size;

        public TicketFile(String ticketId, String ticketSecret, long id, String sha2, long size) {
            this.ticketId = ticketId;
            this.ticketSecret = ticketSecret;
            this.id = id;
            this.sha2 = sha2;
            this.size = size;
        }

        public String getTicketId() {
            return ticketId;
        }

        public String getTicketSecret() {
            return ticketSecret;
        }

        public long getId() {
            return id;
        }

        public String getSha2() {
            return sha2;
        }

        public long getSize() {
            return size;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TicketFile that = (TicketFile) o;

            if (id != that.id) return false;
            if (size != that.size) return false;
            if (!ticketId.equals(that.ticketId)) return false;
            if (!ticketSecret.equals(that.ticketSecret)) return false;
            return sha2.equals(that.sha2);

        }

        @Override
        public int hashCode() {
            int result = ticketId.hashCode();
            result = 31 * result + ticketSecret.hashCode();
            result = 31 * result + (int) (id ^ (id >>> 32));
            result = 31 * result + sha2.hashCode();
            result = 31 * result + (int) (size ^ (size >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "TicketFile{" +
                    "ticketId='" + ticketId + '\'' +
                    ", ticketSecret='" + ticketSecret + '\'' +
                    ", id=" + id +
                    ", sha2='" + sha2 + '\'' +
                    ", size=" + size +
                    '}';
        }
    }

    public final static class ApplicationDetails {
        private final String applicationId;
        private final String versionName;
        private final long versionNum;

        public ApplicationDetails(String applicationId, String versionName, long versionNum) {
            this.applicationId = applicationId;
            this.versionName = versionName;
            this.versionNum = versionNum;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public String getVersionName() {
            return versionName;
        }

        public long getVersionNum() {
            return versionNum;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ApplicationDetails that = (ApplicationDetails) o;

            if (versionNum != that.versionNum) return false;
            if (!applicationId.equals(that.applicationId)) return false;
            return versionName.equals(that.versionName);

        }

        @Override
        public int hashCode() {
            int result = applicationId.hashCode();
            result = 31 * result + versionName.hashCode();
            result = 31 * result + (int) (versionNum ^ (versionNum >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "ApplicationDetails{" +
                    "applicationId='" + applicationId + '\'' +
                    ", versionName='" + versionName + '\'' +
                    ", versionNum=" + versionNum +
                    '}';
        }
    }

    public final static class Ticket {
        private final String id;
        private final String secret;

        public Ticket(String id, String secret) {
            this.id = id;
            this.secret = secret;
        }

        public String getId() {
            return id;
        }

        public String getSecret() {
            return secret;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Ticket ticket = (Ticket) o;

            if (!id.equals(ticket.id)) return false;
            return secret.equals(ticket.secret);

        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + secret.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Ticket{" +
                    "id='" + id + '\'' +
                    ", secret='" + secret + '\'' +
                    '}';
        }
    }

    public final static class ProtocolException extends Exception {
        public ProtocolException() {
        }

        public ProtocolException(String message) {
            super(message);
        }

        public ProtocolException(String message, Throwable cause) {
            super(message, cause);
        }


    }
}
