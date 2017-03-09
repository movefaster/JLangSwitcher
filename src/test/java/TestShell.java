import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by Morton on 3/8/17.
 */
public class TestShell {
    @Test
    public void testSips() {
        File file = new File("/var/tmp/AndroidStudio.icns");
        File outDir = new File("/var/tmp/");
        Shell.execAndWait(Shell.sipsCommandBuilder(file, outDir));
        File outFile = new File("/var/tmp/AndroidStudio.png");
        assertTrue("output file doesn't exist. SIPS command failed.", outFile.exists());
    }
}
