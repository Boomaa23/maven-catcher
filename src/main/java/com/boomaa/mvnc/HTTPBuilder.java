package com.boomaa.mvnc;

public class HTTPBuilder {
    private final StringBuilder sb;

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

    public byte[] build() {
        return sb.toString().getBytes();
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
