package org.sagebionetworks.audit;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

public class GzipUtils {

    public static byte[] compress(String text) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                GZIPOutputStream gos = new GZIPOutputStream(bos);
                OutputStreamWriter osw = new OutputStreamWriter(gos, "UTF-8");
                BufferedWriter bw = new BufferedWriter(osw)) {
            bw.write(text);
            bw.flush();
            gos.finish();
            return bos.toByteArray();
        }
    }

    public static String decompress(byte[] bytes) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                GZIPInputStream gis = new GZIPInputStream(bis)) {
            return new String(IOUtils.toByteArray(gis), "UTF-8");
        }
    }
}
