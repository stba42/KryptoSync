package dev.baust.java.kryptosync.encryption;

import java.io.File;

public class NullEncryptionService implements EncryptionService {


    @Override
    public File encryptFile(File file, String plainSha1) {
        return file;
    }

    @Override
    public String getRemoteFilename(File file, String plainSha1, String plainRelativePath) {
        return plainRelativePath;
    }
}
