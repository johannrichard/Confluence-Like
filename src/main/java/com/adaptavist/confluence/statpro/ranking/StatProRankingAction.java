package com.adaptavist.confluence.statpro.ranking;

import com.atlassian.confluence.core.Administrative;
import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.opensymphony.util.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class StatProRankingAction extends ConfluenceActionSupport implements Administrative {

    private StatProRankingManager manager;

    private StatProRankingConfiguration config = new StatProRankingConfiguration();
    private String command;

    // Components

    public StatProRankingManager getStatProLoginManager() {
        if (manager == null) {
            manager = StatProRankingManager.getInstance();
        }
        return manager;
    }

    public void setStatProLoginManager(StatProRankingManager manager) {
        this.manager = manager;
    }

    // Getters & Setters

    public StatProRankingConfiguration getConfig() {
        return config;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    // Custom Getters & Setters

    public String getSpaceEmailCompound() {
        StringBuffer sb = new StringBuffer();
        for (String spaceKey : config.getSpaceEmails().keySet()) {
            if (sb.length() > 0) sb.append(",");
            sb.append( spaceKey ).append(":").append( config.getSpaceEmails().get( spaceKey ) );
        }
        return sb.toString();
    }

    public void setSpaceEmailCompound(String compound) {
        Map<String, String> spaceEmails = new HashMap<String, String>();
        for (String eachGroup : compound.split(",")) {
            if (TextUtils.stringSet( eachGroup ) && eachGroup.indexOf(":") > -1) {
                String[] split = eachGroup.split(":");
                spaceEmails.put(split[0], split[1]);
            }
        }
        config.setSpaceEmails( spaceEmails );
    }

    // Entry Point

    public String execute() throws Exception {
        getStatProLoginManager();
        // Check for a command
        if (!TextUtils.stringSet( command )) {
            config = manager.getConfig(true);
            return INPUT;
        }
        // Validate
        // TODO: Check each space key exists, and has a validly formed email address
//        if (!TextUtils.stringSet( manager.getURLfromLink( config.getLogoutLink() ) )) {
//            addFieldError("config.logoutLink", "Unable to resolve the link to a URL.");
//        }
        if (hasErrors()) return ERROR;
        // Save
        manager.setConfig(config);
        return SUCCESS;
    }

}
