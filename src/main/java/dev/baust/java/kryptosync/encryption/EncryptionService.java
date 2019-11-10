package dev.baust.java.kryptosync.encryption;

import java.io.File;

public interface EncryptionService {
    File encryptFile(File file, String plainSha1);

    String getRemoteFilename(File file, String plainSha1, String plainRelativePath);

    String getFileIndexFilename();

    File decryptFile(File file, String passphrase);
}
