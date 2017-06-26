package local.server;


import android.support.annotation.NonNull;

/**
 * Created by like1 on 2017/5/16.
 */
public class Protocol {
    private static final int length = 0;
    private static final int flag = 1; //requst or ack
    private static final int opt = 2;
    private static final int permit = 3;
    private byte[] data;

    public Protocol(@NonNull byte[] data) {
        if (data.length < 4)
            throw new IllegalArgumentException();
        this.data = data;
    }

    public int getLength() {
        return data[length];
    }

    public boolean isRequest() {
        return data[flag] == 1;
    }

    public int getOpt() {
        return data[opt];
    }

    public boolean isPermit() {
        return data[permit] == 1;
    }

    public byte[] getData() {
        byte[] data = new byte[this.data.length-4];
        for (int i = 0;i<data.length;i++)
        {
            data[i] = this.data[4+i];
        }
        return data;
    }

    public static byte[] createPacket(@NonNull byte flag, @NonNull byte opt, @NonNull byte permit, byte[] others) {
        if (others != null&&others[0] > 96)
            throw new IllegalArgumentException();
        int othersLength = 0;
        if (others != null)
            othersLength = others[0];
        byte[] data = new byte[4+othersLength];
        int pos = 1;
        data[pos++] = flag;
        data[pos++] = opt;
        data[pos++] = permit;
        if (others != null)
            for (int i = 0; i < othersLength; i++) {
                data[pos++] = others[i];
            }
        data[0] = (byte) pos;
        return data;
    }
}
