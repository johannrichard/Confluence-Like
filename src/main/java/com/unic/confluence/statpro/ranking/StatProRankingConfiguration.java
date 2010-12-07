package com.unic.confluence.statpro.ranking;

import java.util.HashMap;
import java.util.Map;

public class StatProRankingConfiguration {

    /** A Map of Space Key against the Email Address to be used */
    Map<String, String> spaceEmails = new HashMap<String, String>();

    // Accessors

    public Map<String, String> getSpaceEmails() {
        return spaceEmails;
    }

    public void setSpaceEmails(Map<String, String> spaceEmails) {
        this.spaceEmails = spaceEmails;
    }
    
}