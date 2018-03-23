package org.homunculusframework.factory.container;

import java.util.Arrays;

/**
 * Created by Torben Schinke on 20.03.18.
 */

public class UtilStack {
    /**
     * Returns a normalized stack trace, starting just with the first line which called this routine. Side note:
     * different VMs have different call stack  origins (e.g. Android Dalvik vs Android Art vs Java SE).
     *
     * @param offset the offset started from the calling method
     * @return
     */
    public static StackTraceElement[] getCallStack(int offset) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        int callOffset = 0;
        for (int i = 0; i < trace.length; i++) {
            StackTraceElement elem = trace[i];
            if (elem.getFileName() != null && elem.getFileName().equals("DefaultFactory.java") && elem.getMethodName().equals("getCallStack")) {
                callOffset = i + 1;
                break;
            }
        }
        return Arrays.copyOfRange(trace, offset + callOffset, trace.length);
    }

}
