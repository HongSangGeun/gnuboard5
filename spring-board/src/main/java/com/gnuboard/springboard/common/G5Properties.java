package com.gnuboard.springboard.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.g5")
public class G5Properties {
    private String writePrefix = "g5_write_";
    private String dataPath = "../data";

    public String getWritePrefix() {
        return writePrefix;
    }

    public void setWritePrefix(String writePrefix) {
        this.writePrefix = writePrefix;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }
}
