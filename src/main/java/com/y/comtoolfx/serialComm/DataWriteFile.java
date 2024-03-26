package com.y.comtoolfx.serialComm;

import com.y.comtoolfx.AppLauncher;
import com.y.comtoolfx.tools.TimeUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author Y
 * @version 1.0
 * @date 2024/3/23 14:56
 */
public class DataWriteFile {
    private static final File dataFile = new File(AppLauncher.startFile, "data");
    private static final OpenOption[] options = {StandardOpenOption.CREATE, StandardOpenOption.APPEND};

    static {
        if (!dataFile.exists()) {
            dataFile.mkdirs();
        }
    }

    private Path readFile;
    private Path writeFile;

    private DataWriteFile() {

    }

    public DataWriteFile(String serialPortName) {
        File read = new File(dataFile, serialPortName + "-Read" + TimeUtils.getFileName() + ".txt");
        File write = new File(dataFile, serialPortName + "-Write" + TimeUtils.getFileName() + ".txt");
        read.delete();
        write.delete();
        readFile = read.toPath();
        writeFile = write.toPath();
    }

    public void read(byte[] bytes) {
        try {
            Files.writeString(readFile, bytesToString(bytes), options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(byte[] bytes) {
        try {
            Files.writeString(writeFile, bytesToString(bytes), options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String bytesToString(byte[] bytes) {
        StringBuilder b = new StringBuilder();
        for (byte aByte : bytes) {
            b.append(aByte).append(" ");
        }
        return b.toString();
    }
}
