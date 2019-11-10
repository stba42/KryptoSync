package dev.baust.java.kryptosync.sync;

import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.B2StorageClientFactory;
import com.backblaze.b2.client.contentSources.B2ContentSource;
import com.backblaze.b2.client.contentSources.B2ContentTypes;
import com.backblaze.b2.client.contentSources.B2FileContentSource;
import com.backblaze.b2.client.exceptions.B2Exception;
import com.backblaze.b2.client.structures.B2FileVersion;
import com.backblaze.b2.client.structures.B2UploadFileRequest;
import com.backblaze.b2.client.structures.B2UploadListener;
import dev.baust.java.kryptosync.encryption.EncryptionService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.backblaze.b2.util.B2ExecutorUtils.createThreadFactory;

public class FileSyncService {

    private SyncConfig syncConfig;
    private EncryptionService encryptionService;
    private final B2StorageClient client;
    private static Logger logger = LoggerFactory.getLogger(FileSyncService.class);
    private ExecutorService executor = Executors.newFixedThreadPool(10, createThreadFactory("FileSyncService-executor-%02d"));

    public FileSyncService(SyncConfig syncConfig, EncryptionService encryptionService) {
        this.syncConfig = syncConfig;
        this.encryptionService = encryptionService;
        client = B2StorageClientFactory
                .createDefaultFactory()
                .create(syncConfig.getKeyID(), syncConfig.getApplicationKey(), "KryptoSync");
    }

    public void sync() throws IOException, B2Exception {
        List<Path> allFiles = enumerateAllFiles();
        Path basePath = Paths.get(syncConfig.getLocalPath());
        String basePathString = basePath.normalize().toString() + File.separator;
        int i = 0;
        for (Path path : allFiles) {
            String b2FileName = getRelativePath(basePathString, path);
            String sha1 = calcSHA1(path.toFile());
            logger.debug(b2FileName);
            if (getUploadState(b2FileName, sha1) == UploadState.TO_UPLOAD) {
                uploadFile(b2FileName, path.toFile(), sha1);
            }
            i++;
            if (i == 5) {
                break;
            }
        }
        client.close();
        executor.shutdown();
    }

    UploadState getUploadState(String fileName, String sha1) throws IOException, B2Exception {
        try {
            B2FileVersion fileVersion = client.getFileInfoByName(syncConfig.getBucketName(), fileName);
            String remoteSha1 = getRemoteSha1(fileVersion);
            if (sha1.equals(remoteSha1)) {
                return UploadState.UPLOADED;
            } else {
                return UploadState.TO_UPLOAD;
            }
        } catch (B2Exception e) {
            return UploadState.TO_UPLOAD;
        }

    }

    String getRemoteSha1(B2FileVersion fileVersion) {
        // large_file_sha1
        if (StringUtils.isNotEmpty(fileVersion.getContentSha1()) && !fileVersion.getContentSha1().equals("none")) {
            return fileVersion.getContentSha1();
        } else {
            return fileVersion.getLargeFileSha1OrNull();
        }
    }

    public B2FileVersion uploadFile(String fileName, File localFile, String localSha1) throws IOException, B2Exception {
        File file = encryptionService.encryptFile(localFile, localSha1);
        String remoteFilename = encryptionService.getRemoteFilename(localFile, localSha1, fileName);
        String sha1 = calcSHA1(file);
        PrintWriter writer = new PrintWriter(System.out, true);
        final B2UploadListener uploadListener = (progress) -> {
            final double percent = (100. * (progress.getBytesSoFar() / (double) progress.getLength()));
            logger.debug(String.format("%s  progress(%3.2f, )", progress.getState(), percent, progress.toString()));
        };
        B2ContentSource source = B2FileContentSource.builder(file).setSha1(sha1).build();
        B2UploadFileRequest request = B2UploadFileRequest
                .builder(syncConfig.getBucketId(), remoteFilename, B2ContentTypes.APPLICATION_OCTET, source)
                .setListener(uploadListener).build();

        B2FileVersion fileVersion = client.uploadLargeFile(request, executor);
        return fileVersion;
    }

    public String getRelativePath(String basePathString, Path path) {

        String filePath = path.normalize().toAbsolutePath().toString();
        return filePath.replace(basePathString, "").replace(File.separator, "/");


    }

    public List<Path> enumerateAllFiles() throws IOException {
        List<Path> pathList = new ArrayList<>();
        Files.walk(Paths.get(syncConfig.getLocalPath()))
                .filter(Files::isRegularFile)
                .forEach(pathList::add);
        return pathList;
    }

    private static String calcSHA1(File file) throws IOException {
        MessageDigest sha1 = null;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");

            try (InputStream input = new FileInputStream(file)) {

                byte[] buffer = new byte[8192];
                int len = input.read(buffer);

                while (len != -1) {
                    sha1.update(buffer, 0, len);
                    len = input.read(buffer);
                }

                return DigestUtils.sha1Hex(sha1.digest());
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
