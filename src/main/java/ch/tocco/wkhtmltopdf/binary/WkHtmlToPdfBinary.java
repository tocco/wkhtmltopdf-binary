/*
 * Copyright 2016 Tocco AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.tocco.wkhtmltopdf.binary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Urs Wolfer
 */
public class WkHtmlToPdfBinary {
    private static final String WKHTMLTOPDF = "wkhtmltopdf";

    private static WkHtmlToPdfBinary instance;

    private URI exe;

    private WkHtmlToPdfBinary() {
        exe = getFile();
    }

    public static WkHtmlToPdfBinary getInstance() {
        if (instance == null) {
            instance = new WkHtmlToPdfBinary();
        }
        return instance;
    }

    public URI getExe() {
        if (!new File(exe).exists()) { // it probably got deleted from tmp...
            exe = getFile(); // ... then create it again instead of failing
        }
        return exe;
    }

    private static URI getFile() {
        try {
            URI fileURI = WkHtmlToPdfBinary.class.getResource(WKHTMLTOPDF).toURI();
            if (!fileURI.getScheme().startsWith("file")) {
                File tempFile = File.createTempFile(WKHTMLTOPDF, ".bin");
                tempFile.deleteOnExit();
                tempFile.setExecutable(true);
                try (InputStream zipStream = WkHtmlToPdfBinary.class.getResourceAsStream(WKHTMLTOPDF);
                     OutputStream fileStream = new FileOutputStream(tempFile)) {
                    byte[] buf = new byte[1024];
                    int i;
                    while ((i = zipStream.read(buf)) != -1) {
                        fileStream.write(buf, 0, i);
                    }
                }
                fileURI = tempFile.toURI();
            }
            return fileURI;
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
