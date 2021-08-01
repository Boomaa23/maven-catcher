package com.boomaa.mvnc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Main {
    private static final String[] UPSTREAM = new String[] {
            "https://jcenter.bintray.com",
            "https://repo1.maven.org/maven2",
            "https://devsite-ctr-electronics.com/maven/release",
            "http://www.revrobotics.com/content/sw/color-sensor-v3/sdk/maven",
            "https://frcmaven.wpi.edu/artifactory/release",
            "https://maven.octyl.net/repository/team5818-releases"
    };

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(80);
        while (true) {
            Socket socket = server.accept();

            InputStream in = socket.getInputStream();
            byte[] b = readInStream(in);
            HTTPParser initReq = new HTTPParser(b);

            System.out.println(initReq);

            boolean foundRemote = false;
            for (String upstr : UPSTREAM) {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(upstr + initReq.getRoute()).openConnection();
                    conn.setRequestMethod(initReq.getMethod());
                    for (Map.Entry<String, Object> header : initReq.getHeaders().entrySet()) {
                        String key = header.getKey();
                        String value = header.getValue().toString();
                        if (key.equals("Host")) {
                            value = upstr.substring(upstr.indexOf("//") + 2);
                        }
                        conn.setRequestProperty(key, value);
                    }
                    conn.setDoOutput(true);
                    HTTPBuilder bldr = httpUrlResponse(conn);
                    OutputStream out = socket.getOutputStream();
                    out.write(bldr.build());
                    System.out.println(bldr);
                    foundRemote = true;
                    break;
                } catch (IOException ignored) {
                }
            }

//            HTTPBuilder resp = new HTTPBuilder();
//            if (msg != null) {
//                resp.appendText("HTTP/1.1 200 OK")
//                        .appendHeader("Content-Length", msg.length())
//                        .appendHeader("Content-Type", "text/html")
//                        .makeLine().appendText(msg);
//            } else {
//                resp.appendText("HTTP/1.1 404 Not Found")
//                        .appendHeader("Content-Length", 0)
//                        .appendHeader("Content-Type", "text/html")
//                        .makeLine();
//            }
//            System.out.println(resp);
//            OutputStream out = socket.getOutputStream();
//            out.write(resp.build());
        }
    }

    public static HTTPBuilder httpUrlResponse(HttpURLConnection conn) throws IOException {
        HTTPBuilder builder = new HTTPBuilder();
        builder.appendText(conn.getResponseCode() + " " + conn.getResponseMessage());

        Map<String, List<String>> map = conn.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getKey() == null || entry.getKey().equals("null")) {
                continue;
            }
            builder.appendHeader(entry.getKey(), entry.getValue().toString().replaceAll("]", "").replaceAll("\\[", ""));
        }
        return builder;
    }

    public static byte[] readInStream(InputStream in) throws IOException {
        byte[] b = new byte[65535];
        int numRead = in.read(b);
        if (numRead == -1) {
            numRead = b.length;
        }
        return sliceArr(b, 0, numRead);
    }

    public static byte[] sliceArr(byte[] array, int start, int end) {
        try {
            byte[] out = new byte[end - start];
            System.arraycopy(array, start, out, 0, out.length);
            return out;
        } catch (ArrayIndexOutOfBoundsException | NegativeArraySizeException ignored) {
        }
        return new byte[0];
    }
}
