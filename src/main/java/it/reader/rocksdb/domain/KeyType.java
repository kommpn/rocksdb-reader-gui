package it.reader.rocksdb.domain;

import lombok.Data;

@Data

public class KeyType {
    boolean isString = false;

    boolean isIp = false;

   boolean isIpRange = false;
}
