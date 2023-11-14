package it.reader.rocksdb.service;

import it.reader.rocksdb.domain.KeyType;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.springframework.stereotype.Repository;
import org.springframework.util.SerializationUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Pattern;

@Repository
@Slf4j
public class RocksDBRepo {
    RocksDB db;

    ObjectMapper objectMapper = new ObjectMapper();



    public void initDbReadWrite(String dbPath) {
        RocksDB.loadLibrary();
        try {
            db = RocksDB.open(new File(dbPath).getAbsolutePath());
        } catch (RocksDBException ex) {
            log.error("Error initializing rocksDB, check configurations and permissions," +
                    "exception: {}, message: {}",
                    ex.getCause(), ex.getMessage());
        }
    }

    public void initDbReadOnly(String dbPath) throws Exception{
        RocksDB.loadLibrary();
        try(final Options options = new Options()) {
            options.setOptimizeFiltersForHits(true)
                    .optimizeForPointLookup(16)
                    .setMaxWriteBufferNumber(0);

            if((folderSize(new File(dbPath)) / 1024) / 1024 < 150) {
                options.optimizeForSmallDb();
            }

            db = RocksDB.openReadOnly(new File(dbPath).getAbsolutePath());
        } catch (RocksDBException ex) {
            log.error("Error initializing rocksDB, check configurations and permissions," +
                            "exception: {}, message: {}",
                    ex.getCause(), ex.getMessage());
            throw new Exception("Not a rocksdb");
        }
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isFile()) {
                length += file.length();
            } else {
                length += folderSize(file);
            }
        }
        return length;
    }

    public static final Pattern IP_PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
    );

    public static final Pattern IP_RANGE_PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])" +
                    "-(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
    );

    public synchronized TreeMap<String, String> getWholeDb(KeyType keyType, boolean isJson) throws Exception{

        LinkedHashMap<String, String> resultsMap = new LinkedHashMap<>();
        RocksIterator iterator = this.db.newIterator();
        iterator.seekToFirst();
        if(keyType.isIpRange()) {
            if(!IP_RANGE_PATTERN.matcher(getKey(keyType, iterator.key())).matches()) {
                keyType.setIpRange(false);
                throw new Exception("The key type is wrong.");
            }
        }
        if(keyType.isIp()) {
            if(!IP_PATTERN.matcher(getKey(keyType, iterator.key())).matches()) {
                keyType.setIp(false);
                throw new Exception("The key type is wrong.");
            }
        }
        while(iterator.isValid()) {
            if(isJson) {
                try {
                    JsonNode jsonNode = objectMapper.readValue(SerializationUtils.deserialize(iterator.value()).toString(), JsonNode.class);
                    resultsMap.put(getKey(keyType, iterator.key()), objectMapper.writeValueAsString(jsonNode));
                } catch (IllegalArgumentException ex) {
                    throw new Exception("An object contained is not a Json.");
                }
            } else {
                resultsMap.put(getKey(keyType, iterator.key()), new String(iterator.value()));
            }
            iterator.next();
        }
        iterator.close();
        keyType.setIp(false);
        keyType.setIpRange(false);
        keyType.setString(false);
        return new TreeMap<>(resultsMap);
    }

    public String getKey(KeyType keyType, byte[] keyValue) {
        if(keyType.isIp()) {
            return byteToIp(keyValue);
        } else if(keyType.isIpRange()) {
          return byteToIpRange(keyValue);
        } else {
            return new String(keyValue, StandardCharsets.UTF_8);
        }
    }


    public synchronized void save(byte[] key, String value) {
        try {
            db.put(key, value.getBytes(StandardCharsets.UTF_8));
        } catch (RocksDBException e) {
            log.error("Error saving entry in RocksDB, cause: {}, message :{}",
                    e.getCause(), e.getMessage());
        }
    }

    public void close() {
        this.db.close();
    }

    private static String byteToIp(byte[] ipBytes) {
        StringBuilder startString = new StringBuilder();
        for ( byte ipByte : ipBytes) {
            startString.append((long) ipByte & 0xFF).append(".");
        }
        return  startString.substring(0, startString.lastIndexOf("."));
    }

    private static String byteToIpRange(byte[] ipBytes) {
        StringBuilder startString = new StringBuilder();
        for( int i = 0; i<ipBytes.length; i++) {
            if(i == 3) {
                startString.append((long) ipBytes[i] & 0xFF).append("-");
            } else {
                startString.append((long) ipBytes[i] & 0xFF).append(".");
            }

        }
        return  startString.substring(0, startString.lastIndexOf("."));
    }
}
