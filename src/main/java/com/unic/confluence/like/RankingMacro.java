package com.unic.confluence.like;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;

import java.util.Map;

public class RankingMacro extends BaseMacro {

    private StatProRankingManager statProRankingManager;

    // Components

    public StatProRankingManager getStatProLoginManager() {
        if (statProRankingManager == null) {
            statProRankingManager = StatProRankingManager.getInstance();
        }
        return statProRankingManager;
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
        // Get the page
        AbstractPage page = (AbstractPage) ((PageContext) renderContext).getEntity();
        // Build the velcoity context
        Map<String, Object> velocityContext = MacroUtils.defaultVelocityContext();
        // TODO: check this works
		boolean hasRanked = getStatProLoginManager().hasRanked( page );
        velocityContext.put("hasRanked", hasRanked);
		int rankingCount = getStatProLoginManager().rankingCount( page );
		// Make sure we don't count our vote if we hadn't ranked before
		velocityContext.put("rankingCount", hasRanked ? rankingCount -1 : rankingCount);
		velocityContext.put("rankingUsers", getStatProLoginManager().rankingUsers( page ));
        velocityContext.put("pageId", page.getIdAsString());
        return VelocityUtils.getRenderedTemplate("/templates/ranking/ranking.vm", velocityContext);
    }

}
