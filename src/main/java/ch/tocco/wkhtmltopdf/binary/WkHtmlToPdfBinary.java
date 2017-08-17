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

import java.io.*;
import java.lang.ProcessBuilder.Redirect;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;

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

    /**
     * @return the URI of wkhtmltopdf binary.
     */
    public URI getExe() {
        if (!new File(exe).exists()) { // it probably got deleted from tmp...
            exe = getFile(); // ... then create it again instead of failing
        }
        return exe;
    }

    /**
     * Runs wkhtmltopdf with provided parameters. See wkhtmltopdf documentation for supported parameters:
     * http://wkhtmltopdf.org/usage/wkhtmltopdf.txt
     *
     * On failure, a RuntimeException is thrown which contains wkhtmltopdf error output.
     */
    public void run(String[] params) {
        String[] paramsWithExe = new String[params.length + 1];
        paramsWithExe[0] = getExe().getPath();
        System.arraycopy(params, 0, paramsWithExe, 1, params.length);
        File logFile = null;
        try {
            HashSet<PosixFilePermission> perms = new HashSet<>();
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            logFile = Files.createTempFile(WKHTMLTOPDF, ".log", PosixFilePermissions.asFileAttribute(perms)).toFile();
            logFile.deleteOnExit();

            Process process = new ProcessBuilder(paramsWithExe)
                    .redirectOutput(Redirect.to(new File("/dev/null")))
                    .redirectError(Redirect.to(logFile))
                    .start();
            int exitStatus = process.waitFor();
            if (exitStatus != 0) {
                handleError(exitStatus, logFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if(logFile != null) {
                logFile.delete();
            }
        }
    }

    private static void handleError(int exitStatus, File logFile) throws IOException {
        RandomAccessFile readableLogFile = new RandomAccessFile(logFile, "r");
        StringBuilder stringBuilder = new StringBuilder("ERROR:\n");
        String currentLine = readableLogFile.readLine();
        while (currentLine != null) {
            stringBuilder.append(currentLine).append('\n');
            currentLine = readableLogFile.readLine();
        }
        throw new RuntimeException(String.format("wkhtmltopdf exited with code %d: %s", exitStatus, stringBuilder.toString()));
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
