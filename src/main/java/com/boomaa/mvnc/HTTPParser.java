package com.boomaa.mvnc;

import java.util.LinkedHashMap;
import java.util.Map;

public class HTTPParser {
    private final String method;
    private final String route;
    private final Map<String, Object> headers;
    private final String message;

    public HTTPParser(String message) {
        int ioFirstSpc = message.indexOf(' ');
        this.method = message.substring(0, ioFirstSpc);
        int ioSecSpc = message.indexOf(' ', ioFirstSpc + 1);
        this.route = message.substring(ioFirstSpc + 1, ioSecSpc);
        this.headers = new LinkedHashMap<>();

        String headerMsg = message;
        int ioLineSep = headerMsg.indexOf("\r\n");
        do {
            headerMsg = headerMsg.substring(ioLineSep + 2);
            if (headerMsg.substring(0, 2).equals("\r\n")) {
                break;
            }
            int ioColon = headerMsg.indexOf(':');
            headers.put(headerMsg.substring(0, ioColon), headerMsg.substring(ioColon + 1, headerMsg.indexOf("\r\n")));
        } while ((ioLineSep = headerMsg.indexOf("\r\n")) != -1);

        this.message = headerMsg.substring(2);
    }

    public HTTPParser(byte[] bytes) {
        this(new String(bytes));
    }

    public String getMethod() {
        return method;
    }

    public String getRoute() {
        return route;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "HTTPParser{" +
                "method='" + method + '\'' +
                ", route='" + route + '\'' +
                ", headers=" + headers +
                ", message='" + message + '\'' +
                '}';
    }
}
