package com.genymobile.transferclient;

public class VideoPacket {
   private int size;
   private long time;
   private byte[] data = new byte[1_000_000];


//    private int dumpSize;
//    private long dumpTime;
//    private byte[] dumpData = new byte[1_000_000];



//    public VideoPacket dump() {
//        newPacket.setSize(getSize());
//        newPacket.setTime(getTime());
//        System.arraycopy(getFullyArray(), 0, newPacket.getFullyArray(), 0, getSize());
//        return newPacket;
//    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public byte[] getFullyArray() {
        return data;
    }
}
