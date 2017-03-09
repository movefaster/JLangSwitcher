import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import org.junit.Test;

import java.io.File;
import java.util.stream.Stream;

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
}
