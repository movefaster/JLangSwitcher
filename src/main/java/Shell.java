import org.apache.commons.io.FilenameUtils;

import java.io.*;

/**
 * A primitive shell executor with simple methods.
 */
public class Shell {
    private static final Runtime rt = Runtime.getRuntime();
    public static void execAndWait(String... command) {
        try {
            Process proc = rt.exec(command);
            proc.waitFor();
            monitor(proc);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void exec(String... command) {
        try {
            Process proc = rt.exec(command);
            monitor(proc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] sipsCommandBuilder(File iconFile, File outFile) {
        return new String[]{"sips", "-s", "format", "png", iconFile.getAbsolutePath(), "--out",
                    outFile.getAbsolutePath()};
    }

    static void monitor(Process proc) {
        new Thread(() -> {
            BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String err = null, line = null;
            try {
                while ((err = stderr.readLine()) != null || (line = stdout.readLine()) != null) {
                    if (err != null) {
                        System.out.println("stderr: " + err);
                    }
                    if (line != null) {
                        System.out.println("stdout: " + line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
