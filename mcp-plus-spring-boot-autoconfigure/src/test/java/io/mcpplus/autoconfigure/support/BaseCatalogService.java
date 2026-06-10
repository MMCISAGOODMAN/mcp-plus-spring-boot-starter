package io.mcpplus.autoconfigure.support;

import org.springframework.stereotype.Service;

@Service
public class BaseCatalogService {

    public String baseLookup(String id) {
        return "base:" + id;
    }
}
