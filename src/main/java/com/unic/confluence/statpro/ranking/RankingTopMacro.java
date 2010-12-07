package com.unic.confluence.statpro.ranking;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;

import java.util.Map;

public class RankingTopMacro extends BaseMacro {

    private StatProRankingManager manager;

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

    // Base Macro API

    public boolean isInline() {
        return false;
    }

    public boolean hasBody() {
        return false;
    }

    public RenderMode getBodyRenderMode() {
        return RenderMode.NO_RENDER;
    }

    public String execute(Map params, String body, RenderContext renderContext) throws MacroException {
        // Find the space key
        Space space = ((AbstractPage) ((PageContext) renderContext).getEntity()).getSpace();
        // Build the output
        StringBuffer sb = new StringBuffer();
        sb.append("<ol class=\"topRankingContent\">");
        for (RankingResult result : getStatProLoginManager().getTopRankedContent( space )) {
            sb.append("<li><a href=\"").append( result.getUrlPath() ).append("\">").append( result.getTitle() ).append("</a></li>");
        }
        sb.append("</ol>");
        return sb.toString();
    }

}