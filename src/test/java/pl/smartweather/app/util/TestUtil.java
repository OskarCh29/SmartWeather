package pl.smartweather.app.util;

import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class TestUtil {

    public static String getJsonResponseFromFile(final String path) throws IOException {
        final File file = new File(TestUtil.class.getResource(path).getFile());
        final String dataResponse = FileUtils.readFileToString(file,"UTF-8");
        return dataResponse;
    }
}
