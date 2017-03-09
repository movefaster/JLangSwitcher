import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.function.UnaryOperator;

import static org.junit.Assert.*;

/**
 * Created by Morton on 3/8/17.
 */
public class TestShell implements Shell.OnCommandCompletionListener {
    @Test
    public void testSips() {
        File file = new File("/var/tmp/AndroidStudio.icns");
        File outDir = new File("/var/tmp/");
        Shell.execAndWait(Shell.sipsCommandBuilder(file, outDir));
        File outFile = new File("/var/tmp/AndroidStudio.png");
        assertTrue("output file doesn't exist. SIPS command failed.", outFile.exists());
    }

    @Test
    public void testExecForResult() {
        Shell.execForResult(new String[]{"ls"}, this);
    }

    @Test
    public void testAppId() {
        Shell.execForResult(Shell.appIdCommandBuilder("Telegram.app"), this);
    }

    @Test
    public void testDefaults() {
        System.out.println(Arrays.toString(Shell.defaultsWriteCommandBuilder("ru.keepcoder.Telegram", "zh-Hans")));
        Shell.execAndWait(Shell.defaultsWriteCommandBuilder("ru.keepcoder.Telegram", "zh-Hans"));
    }

    public void onResult(String s) {
        System.out.println(s);
    }

}
