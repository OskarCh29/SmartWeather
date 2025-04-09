package pl.smartweather.app.utils;

import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class TestUtils {
    public static String getJsonFromFile(final String path) throws IOException {
        final File file = new File(TestUtils.class.getResource(path).getFile());
        final String jsonData = FileUtils.readFileToString(file, "UTF-8");
        return jsonData;
    }

}
