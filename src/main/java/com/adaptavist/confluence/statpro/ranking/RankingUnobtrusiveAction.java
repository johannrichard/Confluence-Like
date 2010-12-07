package com.adaptavist.confluence.statpro.ranking;

import com.atlassian.confluence.pages.actions.AbstractPageAction;

public class RankingUnobtrusiveAction extends AbstractPageAction {

    private boolean useful;

    // Getters & Setters

    public boolean isUseful() {
        return useful;
    }

    public void setUseful(boolean useful) {
        this.useful = useful;
    }

    // Entry Point

    public String execute() {
        return SUCCESS;
    }

}