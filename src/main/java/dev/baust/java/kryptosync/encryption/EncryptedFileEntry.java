package dev.baust.java.kryptosync.encryption;

public class EncryptedFileEntry {

    private String encryptedFilename;
    private String plainFilename;
    private String encryptedSha1;
    private String plainSha1;

    public String getEncryptedFilename() {
        return encryptedFilename;
    }

    public void setEncryptedFilename(String encryptedFilename) {
        this.encryptedFilename = encryptedFilename;
    }

    public String getPlainFilename() {
        return plainFilename;
    }

    public void setPlainFilename(String plainFilename) {
        this.plainFilename = plainFilename;
    }

    public String getEncryptedSha1() {
        return encryptedSha1;
    }

    public void setEncryptedSha1(String encryptedSha1) {
        this.encryptedSha1 = encryptedSha1;
    }

    public String getPlainSha1() {
        return plainSha1;
    }

    public void setPlainSha1(String plainSha1) {
        this.plainSha1 = plainSha1;
    }
}
