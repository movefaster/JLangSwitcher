import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/**
 * Created by Morton on 3/8/17.
 */
public class TestPlist {

    @Test
    public void testPlistRead() throws Exception {
        File plist = new File(System.getProperty("user.home") + "/Library/Preferences/.GlobalPreferences.plist");
        NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(plist);
        NSObject[] langs = ((NSArray) rootDict.objectForKey("AppleLanguages")).getArray();
        Stream.of(langs).forEach(System.out::println);
    }

    @Test
    public void testReadIconFile() throws Exception {
        File plist = new File("/Applications/Android Studio.app/Contents/info.plist");
        assertTrue("plist file should exist", plist.exists());
        NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(plist);
        System.out.println(rootDict.objectForKey("CFBundleIconFile"));
        System.out.println(Arrays.toString(rootDict.allKeys()));
    }
}
