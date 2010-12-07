package com.unic.confluence.statpro.ranking;

import java.util.HashMap;
import java.util.Map;

public class StatProRankingDataSet {

    /** ID of the Page this data set relates to */
    private long entityId;
    /** A Map of Usernames and the date they ranked positively on (YYYYMMDD) */
    private Map<String, Integer> positive = new HashMap<String, Integer>();
    /** A Map of Usernames and the date they ranked negatively on (YYYYMMDD) */
    private Map<String, Integer> negative = new HashMap<String, Integer>();

    // Accessors

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public Map<String, Integer> getPositive() {
        return positive;
    }

    public void setPositive(Map<String, Integer> positive) {
        this.positive = positive;
    }

    public Map<String, Integer> getNegative() {
        return negative;
    }

    public void setNegative(Map<String, Integer> negative) {
        this.negative = negative;
    }

}