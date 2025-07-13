package xyz.quartzframework;

import lombok.val;
import xyz.quartzframework.aop.NoProxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@NoProxy
public interface Quartz<T> {

    String getName();

    File getDataFolder();

    T getPlugin();

    void main();

    void close();

//    default void close() {
//        val context = getContext();
//        if (Objects.isNull(context)) return;
//        context.close();
//    }

    default void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
        resourcePath = resourcePath.replace('\\', '/');
        val in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found.");
        }
        val outFile = new File(System.getProperty("user.dir"), resourcePath);
        val lastIndex = resourcePath.lastIndexOf('/');
        val outDir = new File(System.getProperty("user.dir"), resourcePath.substring(0, Math.max(lastIndex, 0)));

        if (!outDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                val out = new FileOutputStream(outFile);
                val buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {
                throw new IOException(String.format("Could not save %s to %s because %s already exists.", outFile.getName(), outFile, outFile.getName()));
            }
        } catch (IOException ex) {
            throw new RuntimeException(String.format("Could not save %s to %s: %s", outFile.getName(), outFile, ex.getMessage()), ex);
        }
    }

    default InputStream getResource(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }
        try {
            val url = Thread.currentThread().getContextClassLoader().getResource(filename);
            if (url == null) {
                return null;
            }
            val connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

}