package io.mcpplus.autoconfigure.support;

import org.springframework.stereotype.Service;

@Service
public class ExtendedCatalogService extends BaseCatalogService {

    @Override
    public String baseLookup(String id) {
        return "extended:" + id;
    }

    public String extendedOnly(String id) {
        return "only:" + id;
    }
}
