package com.boomaa.mvnc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(80);
        System.out.println("Starting server on port 80");
        List<String> upstream = Files.readAllLines(Paths.get("proxies.conf"));

        while (true) {
            Socket socket = server.accept();

            InputStream in = socket.getInputStream();
            byte[] b = readInStream(in);
            HTTPParser initReq = new HTTPParser(b);
            Path reqRoute = Paths.get(System.getProperty("user.dir") + "/cached/" + initReq.getRoute());

            System.out.println(initReq);

            if (Files.exists(reqRoute)) {
                System.out.println("Using cached file\n");
                OutputStream out = socket.getOutputStream();
                byte[] fileBytes = Files.readAllBytes(reqRoute);
                HTTPBuilder bldr = new HTTPBuilder();
                bldr.appendText("HTTP/1.1 200 OK").appendHeader("Content-length", fileBytes.length).makeLine();
                out.write(ArrayUtils.concat(bldr.build(), fileBytes));
                socket.close();
                continue;
            }

            boolean foundRemote = false;
            for (String upstr : upstream) {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(upstr + initReq.getRoute()).openConnection();
                    conn.setInstanceFollowRedirects(true);
                    conn.setRequestMethod(initReq.getMethod());
                    for (Map.Entry<String, Object> header : initReq.getHeaders().entrySet()) {
                        String key = header.getKey();
                        String value = header.getValue().toString();
                        if (key.equals("Host")) {
                            int ioSl = upstr.indexOf("//") + 2;
                            int ioEnd = upstr.indexOf('/', ioSl + 1);
                            if (ioEnd == -1) {
                                ioEnd = upstr.length();
                            }
                            value = upstr.substring(ioSl, ioEnd);
                        }
                        conn.setRequestProperty(key, value);
                    }
                    conn.setDoOutput(true);

                    if (conn.getResponseCode() == 200) {
                        System.out.println(upstr);
                        HTTPBuilder bldr = httpUrlResponse(conn);
                        OutputStream out = socket.getOutputStream();
                        out.write(bldr.build());
                        socket.close();

                        Files.createDirectories(reqRoute.getParent());
                        FileOutputStream cache = new FileOutputStream(reqRoute.toFile());
                        cache.write(bldr.getBody());
                        cache.close();

                        System.out.println(bldr);
                        System.out.println();
                        foundRemote = true;
                        break;
                    }
                } catch (IOException ignored) {
                }
            }
            if (!foundRemote) {
                System.err.println(initReq.getRoute());
            }
        }
    }

    public static HTTPBuilder httpUrlResponse(HttpURLConnection conn) throws IOException {
        HTTPBuilder builder = new HTTPBuilder();
        builder.appendText("HTTP/1.1 " + conn.getResponseCode() + " " + conn.getResponseMessage());

        Map<String, List<String>> map = conn.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getKey() == null || entry.getKey().equals("null")) {
                continue;
            }
            String value = entry.getValue().toString().replaceAll("]", "").replaceAll("\\[", "");
            if (value.equals("gzip") || (entry.getKey().toLowerCase().equals("transfer-encoding") && value.equals("chunked"))) {
                continue;
            }
            builder.appendHeader(entry.getKey(), value);
        }

        byte[] content = conn.getInputStream().readAllBytes();
        builder.makeLine().setBody(content);
        return builder;
    }

    public static byte[] readInStream(InputStream in) throws IOException {
        byte[] b = new byte[65535];
        int numRead = in.read(b);
        if (numRead == -1) {
            numRead = b.length;
        }
        return ArrayUtils.sliceArr(b, 0, numRead);
    }
}
