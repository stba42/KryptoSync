package dev.baust.java.kryptosync.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.baust.java.kryptosync.encryption.EncryptionService;
import dev.baust.java.kryptosync.encryption.NullEncryptionService;
import dev.baust.java.kryptosync.sync.FileSyncService;
import dev.baust.java.kryptosync.sync.SyncConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class KryptoSync {

    private static Logger logger = LoggerFactory.getLogger(KryptoSync.class);
    private static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        File f = new File("config-local.json");
        SyncConfig config = mapper.readValue(f, SyncConfig.class);
        EncryptionService encryptionService = new NullEncryptionService();
        FileSyncService fileSyncService = new FileSyncService(config, encryptionService);
        fileSyncService.sync();
    }
}
