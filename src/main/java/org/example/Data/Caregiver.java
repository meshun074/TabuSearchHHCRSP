package org.example.Data;

import java.util.Set;

public class Caregiver {
    private String id;
    private Set<String> abilities;
    private int cacheId = - 1;

    public String getId() {
        return id;
    }
    // This method will be called by Jackson after deserialization
    public void setId(String id) {
        this.id = id;
        // Recalculate cacheId now that id is set
        // Using a helper method to handle the final field
        this.cacheId = calculateCacheId();
    }

    private int calculateCacheId() {
        return Integer.parseInt(id.substring(1)) - 1;
    }

    public int getCacheId() {
        return cacheId;
    }
    public Set<String> getAbilities() {
        return abilities;
    }

}
