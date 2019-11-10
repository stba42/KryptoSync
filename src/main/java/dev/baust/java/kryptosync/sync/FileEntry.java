package dev.baust.java.kryptosync.sync;

public class FileEntry {

    private String filePath;
    private String sha1;
    private UploadState uploadState;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public UploadState getUploadState() {
        return uploadState;
    }

    public void setUploadState(UploadState uploadState) {
        this.uploadState = uploadState;
    }
}
