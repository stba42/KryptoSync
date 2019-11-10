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

    @Override
    public String getFileIndexFilename() {
        return null;
    }

    @Override
    public File decryptFile(File file, String passphrase) {
        return file;
    }
}
