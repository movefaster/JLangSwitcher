import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.UnaryOperator;

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

    public static void execForResult(String[] command, OnCommandCompletionListener callback) {
        try {
            Process proc = rt.exec(command);
            String output = getStdOut(proc);
            // no need to wait for proc because getStdOut is blocking
            callback.onResult(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] sipsCommandBuilder(File iconFile, File outFile) {
        return new String[]{"sips", "-s", "format", "png", iconFile.getAbsolutePath(), "--out",
                    outFile.getAbsolutePath()};
    }

    public static String[] appIdCommandBuilder(String appName) {
        return new String[]{"osascript", "-e",
                    String.format("id of app \"%s\"", appName)};
    }

    public static String[] defaultsWriteCommandBuilder(String id, String locale) {
        return new String[]{"defaults", "write", id, "AppleLanguages",
                    String.format("(\"%s\")", locale)};
    }

    public static String[] defaultsDeleteCommandBuilder(String id) {
        return new String[]{"defaults", "delete", id, "AppleLanguages"};
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

    static String getStdOut(Process proc) {
        Scanner scanner = new Scanner(proc.getInputStream()).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : null;
    }

    interface OnCommandCompletionListener {
        void onResult(String result);
    }
}
