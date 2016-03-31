package com.xh.usb.accessory.mode;

/**
 * 缓冲接受到的数据，识别“回车”（结束）
 */
public class SerialReadBuffer {
    private StringBuffer readBuffer = new StringBuffer();

    public void append(byte[] inputByte, int bytes) {
        synchronized (readBuffer) {
            for (int i = 0; i < bytes; i++) {
                readBuffer.append((char) inputByte[i]);
            }
        }
    }

    public void append(String s) {
        synchronized (readBuffer) {
            readBuffer.append(s);
            readBuffer.append("\n");
        }
    }

    public String read() {
        synchronized (readBuffer) {
            int index = readBuffer.indexOf("\n");
            while (index == 0) {//去除开头的回车
                readBuffer.delete(0, 1);
                index = readBuffer.indexOf("\n");
            }
            if (index != -1) {
                String s = readBuffer.substring(0, index);
                readBuffer.delete(0, index + 1);
                return s;
            } else {
                return null;
            }
        }
    }

    public void clean() {
        readBuffer.setLength(0);
    }
}
