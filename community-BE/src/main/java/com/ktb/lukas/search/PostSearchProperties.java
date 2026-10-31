package com.ktb.lukas.search;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.search")
public class PostSearchProperties {
    private boolean enabled = false;
    private String index = "community-posts-v1";
    private int batchSize = 100;
    private boolean reindexOnStartup = false;
}
