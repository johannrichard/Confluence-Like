package com.unic.confluence.like;

import java.util.HashMap;
import java.util.Map;

public class StatProRankingData {

    private String spaceKey;
    private Map<Long, StatProRankingDataSet> entities = new HashMap<Long, StatProRankingDataSet>();

    // Accessors

    public String getSpaceKey() {
        return spaceKey;
    }

    public void setSpaceKey(String spaceKey) {
        this.spaceKey = spaceKey;
    }

    public Map<Long, StatProRankingDataSet> getEntities() {
        return entities;
    }

    public void setEntities(Map<Long, StatProRankingDataSet> entities) {
        this.entities = entities;
    }
    
}