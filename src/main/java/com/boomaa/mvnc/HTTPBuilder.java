package com.boomaa.mvnc;

public class HTTPBuilder {
    private final StringBuilder sb;
    private byte[] body;

    public HTTPBuilder() {
        this.sb = new StringBuilder();
    }

    public HTTPBuilder appendText(Object obj) {
        sb.append(obj);
        makeLine();
        return this;
    }

    public HTTPBuilder appendHeader(String key, Object value) {
        sb.append(key).append(": ").append(value);
        makeLine();
        return this;
    }

    public HTTPBuilder makeLine() {
        sb.append("\r\n");
        return this;
    }

    public HTTPBuilder setBody(byte[] body) {
        this.body = body;
        return this;
    }

    public byte[] getBody() {
        return body;
    }

    public byte[] build() {
        byte[] data = sb.toString().getBytes();
        return body != null ? ArrayUtils.concat(data, body) : data;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
