package group.bison.data.data_layer.common.utils;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class NonBlockingInputStreamReadUtil {

    public static byte[] readNulIfTimeout(InputStream ins, long timeout) throws IOException {
        if (timeout <= 0) {
            throw new RuntimeException("timeout <=0");
        }

        long startTimestamp = System.currentTimeMillis();

        byte[] readBytes = new byte[0];
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int retry = 0;
            while ((baos.size() == 0 || retry <= 3) && (System.currentTimeMillis() <= startTimestamp + timeout)) {
                if (ins.available() <= 0) {
                    try {
                        retry++;
                        Thread.sleep(100);
                        continue;
                    } catch (Exception e) {
                        break;
                    }
                }

                retry = 0;

                byte[] buf = new byte[ins.available()];
                ins.read(buf);
                baos.write(buf);
            }

            if (ins.available() > 0) {
                baos.reset();
            }

            readBytes = baos.toByteArray();
        } finally {
            ins.close();
        }
        return readBytes;
    }
}
