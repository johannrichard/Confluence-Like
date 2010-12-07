package com.unic.confluence.statpro.ranking;

import com.atlassian.confluence.pages.actions.AbstractPageAction;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.opensymphony.util.TextUtils;

public class RankingAction extends AbstractPageAction {

    private StatProRankingManager manager;
    private boolean ajax;
    private boolean useful;
    private String feedback;

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

    public void setAjax(boolean ajax) {
        this.ajax = ajax;
    }

    public void setUseful(boolean useful) {
        this.useful = useful;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    // Entry Point

    public String execute() {
        // Check I am logged in
        if (AuthenticatedUserThreadLocal.getUser() == null) return SUCCESS;
        // Record the value
        StatProRankingManager manager = getStatProLoginManager();
        manager.rankContent(getPage(), useful);
        // Do I have any feedback?
        if (TextUtils.stringSet( feedback )) {
            manager.sendFeedback(getPage(), feedback);
        }
        // Success!
        return SUCCESS + (ajax ? "-xml" : "-redirect");
    }

}
