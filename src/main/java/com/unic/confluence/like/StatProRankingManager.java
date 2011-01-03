package com.unic.confluence.like;

import com.atlassian.bandana.BandanaContext;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.ListQuery;
import com.atlassian.confluence.core.SmartListManager;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.search.actions.SearchResultWithExcerpt;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.core.user.UserUtils;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.User;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class StatProRankingManager {

    protected final static String bandanaKey = "com.unic.confluence.like.LikeManager";

    private static final Logger log = Logger.getLogger( StatProRankingManager.class );
    private static final int EXPIRY_IN_DAYS = 30;
    private static final int TOP_COUNT = 5;
    private static BandanaContext bandanaContext = new ConfluenceBandanaContext();

    private BandanaManager bandanaManager;
    private PageManager pageManager;
    private SmartListManager smartListManager;
    private XStream xStream;

	private UserAccessor userAccessor = (UserAccessor) com.atlassian.spring.container.ContainerManager.getComponent("userAccessor");

    // Constructor

    public StatProRankingManager() {
        xStream = new XStream();
        xStream.setClassLoader( getClass().getClassLoader() );
    }

    // Components

    public void setBandanaManager(BandanaManager bandanaManager) {
        this.bandanaManager = bandanaManager;
    }

    public void setPageManager(PageManager pageManager) {
        this.pageManager = pageManager;
    }

    public void setSmartListManager(SmartListManager smartListManager) {
        this.smartListManager = smartListManager;
    }

    // Data Access

    public StatProRankingConfiguration getConfig(boolean useDefault) {
        Object banData = bandanaManager.getValue(bandanaContext, bandanaKey +":config");
        if (banData instanceof String) {
            Object xData = xStream.fromXML((String) banData);
            if (xData instanceof StatProRankingConfiguration) {
                return (StatProRankingConfiguration) xData;
            }
        }
        //
        return useDefault ? new StatProRankingConfiguration() : null;
    }

    public void setConfig(StatProRankingConfiguration config) {
        bandanaManager.setValue(bandanaContext, bandanaKey +":config", xStream.toXML(config));
    }

    public StatProRankingData getData(String spaceKey, boolean useDefault) {
        Object banData = bandanaManager.getValue(bandanaContext, bandanaKey +":data:"+ spaceKey);
        if (banData instanceof String) {
            Object xData = xStream.fromXML((String) banData);
            if (xData instanceof StatProRankingData) {
                return (StatProRankingData) xData;
            }
        }
        //
        if (!useDefault) return null;
        StatProRankingData data = new StatProRankingData();
        data.setSpaceKey(spaceKey);
        return data;
    }

    public void setData(String spaceKey, StatProRankingData data) {
        bandanaManager.setValue(bandanaContext, bandanaKey +":data:"+ spaceKey, xStream.toXML(data));
    }

    // API Methods

    /**
     * Get the date expressed as the number of days since 1st Jan 1970 (the epoch)
     * @return the date as described
     */
    public int getDateNow() {
        return (int) (System.currentTimeMillis() / 1000L / 60L / 60L / 24L);
    }

    /**
     * Load the data for this space and remove any ranks which have expired, then save the data
     * @param spaceKey Space for the key
     */
    public void cleanDataSet(String spaceKey) {
        // Get the data set
        StatProRankingData data = getData(spaceKey, false);
        if (data == null) return;
        // Work out the expiry date expressed as the number of days since 1st Jan 1970 (the epoch)
        int expiresAt = getDateNow() - EXPIRY_IN_DAYS;
        // Scan through each entity and remove those which have expired
        boolean changed = false;
        for (StatProRankingDataSet dataSet : data.getEntities().values()) {
            ArrayList<String> deadKeys = new ArrayList<String>();
            for (String username : dataSet.getPositive().keySet()) {
                int rankedAt = dataSet.getPositive().get(username);
                // if (rankedAt < expiresAt) deadKeys.add(username);
            }
            if (!deadKeys.isEmpty()) {
                changed = true;
                for (String username : deadKeys) dataSet.getPositive().remove(username);
                deadKeys.clear();
            }
            for (String username : dataSet.getNegative().keySet()) {
                int rankedAt = dataSet.getNegative().get(username);
                // if (rankedAt < expiresAt) deadKeys.add(username);
            }
            if (!deadKeys.isEmpty()) {
                changed = true;
                for (String username : deadKeys) dataSet.getNegative().remove(username);
            }
        }
        // If the data was changed, save the data set
        if (changed) {
            setData(spaceKey, data);
        }
    }
	
	public String likeUserList(AbstractPage page) {
        String spaceKey = page.getSpaceKey();
 		String authenticatedUser = AuthenticatedUserThreadLocal.getUsername().toLowerCase();
        // Get the dataset for this space
        StatProRankingData data = getData(spaceKey, true);
        StatProRankingDataSet dataSet = data.getEntities().get( page.getId() );
        if (dataSet == null) {
            dataSet = new StatProRankingDataSet();
            dataSet.setEntityId( page.getId() );
            data.getEntities().put(page.getId(), dataSet);
        }

	    StringBuilder builder = new StringBuilder();
		boolean first = true;
		int c = dataSet.getPositive().size();
		for (String username : dataSet.getPositive().keySet()) {
			String userFullName = new String();
			User user = userAccessor.getUser( username.toLowerCase() );
			if (user != null ) {
				userFullName = user.getFullName();
			} else {
				userFullName = username;
			}
			if( !authenticatedUser.equals( username.toLowerCase()) ) {
				if( !first && c > 1) {
					builder.append( ", " );		        
				} else if( !first ) {
					builder.append( " and " );
				}
				builder.append( "<a class=\"confluence-userlink username:" );
				builder.append( username );
				builder.append( " url fn\" href=\"/display/~" );
				builder.append( username );
				builder.append( "\"><strong>" );
		        builder.append( userFullName );
				builder.append( "</strong></a>" );
				builder.append( "<!-- c:" );
				builder.append( c );
				builder.append( " -->");
				first = false;
			}
			c--;
	    }
	    return builder.toString();
	}
	
    public int likeCount(AbstractPage page) {
        String spaceKey = page.getSpaceKey();
        // Get the dataset for this space
        StatProRankingData data = getData(spaceKey, true);
        StatProRankingDataSet dataSet = data.getEntities().get( page.getId() );
        if (dataSet == null) {
            dataSet = new StatProRankingDataSet();
            dataSet.setEntityId( page.getId() );
            data.getEntities().put(page.getId(), dataSet);
        }
        return dataSet.getPositive().size();
    }

	public String dislikeUserList(AbstractPage page) {
        String spaceKey = page.getSpaceKey();
 		String authenticatedUser = AuthenticatedUserThreadLocal.getUsername().toLowerCase();
        // Get the dataset for this space
        StatProRankingData data = getData(spaceKey, true);
        StatProRankingDataSet dataSet = data.getEntities().get( page.getId() );
        if (dataSet == null) {
            dataSet = new StatProRankingDataSet();
            dataSet.setEntityId( page.getId() );
            data.getEntities().put(page.getId(), dataSet);
        }

	    StringBuilder builder = new StringBuilder();
		boolean first = true;
		int c = dataSet.getPositive().size();
		for (String username : dataSet.getNegative().keySet()) {
			String userFullName = new String();
			User user = userAccessor.getUser( username.toLowerCase() );
			if (user != null ) {
				userFullName = user.getFullName();
			} else {
				userFullName = username;
			}
			if( !first && c > 1) {
				builder.append( ", " );		        
			} else if( !first ) {
				builder.append( " and " );
			}
			builder.append( "<a class=\"confluence-userlink username:" );
			builder.append( username );
			builder.append( " url fn\" href=\"/display/~" );
			builder.append( username );
			builder.append( "\"><strong>" );
	        builder.append( userFullName );
			builder.append( "</strong></a>" );
			builder.append( "<!-- c:" );
			builder.append( c );
			builder.append( " -->");
			first = false;
			c--;
	    }
	    return builder.toString();
	}
	
	public int dislikeCount(AbstractPage page) {
        String spaceKey = page.getSpaceKey();
        // Get the dataset for this space
        StatProRankingData data = getData(spaceKey, true);
        StatProRankingDataSet dataSet = data.getEntities().get( page.getId() );
        if (dataSet == null) {
            dataSet = new StatProRankingDataSet();
            dataSet.setEntityId( page.getId() );
            data.getEntities().put(page.getId(), dataSet);
        }
        return dataSet.getNegative().size();
    }

    public boolean hasRanked(AbstractPage page) {
        String spaceKey = page.getSpaceKey();
        // Get the dataset for this space
        StatProRankingData data = getData(spaceKey, true);
        StatProRankingDataSet dataSet = data.getEntities().get( page.getId() );
        if (dataSet == null) {
            dataSet = new StatProRankingDataSet();
            dataSet.setEntityId( page.getId() );
            data.getEntities().put(page.getId(), dataSet);
        }
        String username = AuthenticatedUserThreadLocal.getUsername();
        // Check the ranking
        return hasRanked(username, dataSet);
    }

    public boolean hasRanked(String username, StatProRankingDataSet dataSet) {
        // Check I've not already ranked this content
        return dataSet.getPositive().containsKey(username) || dataSet.getNegative().containsKey(username);
    }

    public boolean hasLiked(AbstractPage page) {
        String spaceKey = page.getSpaceKey();
        // Get the dataset for this space
        StatProRankingData data = getData(spaceKey, true);
        StatProRankingDataSet dataSet = data.getEntities().get( page.getId() );
        if (dataSet == null) {
            dataSet = new StatProRankingDataSet();
            dataSet.setEntityId( page.getId() );
            data.getEntities().put(page.getId(), dataSet);
        }
        String username = AuthenticatedUserThreadLocal.getUsername();
        // Check the ranking
        return hasLiked(username, dataSet);
    }

    public boolean hasLiked(String username, StatProRankingDataSet dataSet) {
        // Check I've not already ranked this content
        return dataSet.getPositive().containsKey(username);
    }

    public boolean hasDisliked(AbstractPage page) {
        String spaceKey = page.getSpaceKey();
        // Get the dataset for this space
        StatProRankingData data = getData(spaceKey, true);
        StatProRankingDataSet dataSet = data.getEntities().get( page.getId() );
        if (dataSet == null) {
            dataSet = new StatProRankingDataSet();
            dataSet.setEntityId( page.getId() );
            data.getEntities().put(page.getId(), dataSet);
        }
        String username = AuthenticatedUserThreadLocal.getUsername();
        // Check the ranking
        return hasDisliked(username, dataSet);
    }

    public boolean hasDisliked(String username, StatProRankingDataSet dataSet) {
        // Check I've not already ranked this content
        return dataSet.getNegative().containsKey(username);
    }

    public void rankContent(AbstractPage page, boolean positive) {
        String spaceKey = page.getSpaceKey();
        // Get the dataset for this space
        StatProRankingData data = getData(spaceKey, true);
        StatProRankingDataSet dataSet = data.getEntities().get( page.getId() );
        if (dataSet == null) {
            dataSet = new StatProRankingDataSet();
            dataSet.setEntityId( page.getId() );
            data.getEntities().put(page.getId(), dataSet);
        }
        // Check I've not already ranked this content
        String username = AuthenticatedUserThreadLocal.getUsername();
        // if (hasRanked(username, dataSet)) return;
        // Rank the page
        int dateNow = getDateNow();
        if (positive && dataSet.getNegative().containsKey(username) ) {
            dataSet.getNegative().put(username, dateNow);
	    }else if (positive && !dataSet.getPositive().containsKey(username) ) {
	        dataSet.getPositive().remove( username );
        } else if(!positive && dataSet.getPositive().containsKey(username) ) {
            dataSet.getPositive().remove( username );
        } else if(!positive && !dataSet.getNegative().containsKey(username) ) {
			dataSet.getNegative().put(username, dateNow);
		}
        // Save the data
        setData(spaceKey, data);
    }

    public void rankContent(AbstractPage page, boolean like, boolean dislike) {
        String spaceKey = page.getSpaceKey();
        // Get the dataset for this space
        StatProRankingData data = getData(spaceKey, true);
        StatProRankingDataSet dataSet = data.getEntities().get( page.getId() );
        if (dataSet == null) {
            dataSet = new StatProRankingDataSet();
            dataSet.setEntityId( page.getId() );
            data.getEntities().put(page.getId(), dataSet);
        }
        // Check I've not already ranked this content
        String username = AuthenticatedUserThreadLocal.getUsername();
        // if (hasRanked(username, dataSet)) return;
        // Rank the page
        int dateNow = getDateNow();

		// Reset like from negative view
        if (like && dataSet.getNegative().containsKey(username) ) {
            dataSet.getNegative().remove(username);
	    } 
		// Like 
		if (like && !dataSet.getPositive().containsKey(username) ) {
	        dataSet.getPositive().put(username, dateNow);
        } 
		// Reset dislike from positive view
		if(dislike && dataSet.getPositive().containsKey(username) ) {
            dataSet.getPositive().remove( username );
        } 
		// Dislike
		if(dislike && !dataSet.getNegative().containsKey(username) ) {
			dataSet.getNegative().put(username, dateNow);
		}
		// Reset anyhow
		if(dislike && like) {
            dataSet.getPositive().remove( username );
            dataSet.getNegative().remove( username );
		}
        // Save the data
        setData(spaceKey, data);
    }
    public void sendFeedback(AbstractPage page, String feedback) {
        // Get configuration for the space key
        StatProRankingConfiguration config = getConfig(true);
        // Find the email address
        String emailAddress = config.getSpaceEmails().get( page.getSpaceKey() );
        if (emailAddress == null) return;
        // Get the logged in user
        User user = AuthenticatedUserThreadLocal.getUser();
        // Build up and Send the email
        try {
            Email mail = new Email( emailAddress );
            mail.setFrom( user.getEmail() );
            mail.setFromName( user.getFullName() );
            mail.setSubject("Feedback for page: "+ page.getTitle() +" ("+ page.getSpaceKey() +")");
            mail.setBody( feedback );
            SMTPMailServer mailServer = MailFactory.getServerManager().getDefaultSMTPMailServer();
            if (mailServer == null) {
                throw new RuntimeException("Unfortunatly there are no available mail servers to send you message with.");
            } else {
                mailServer.send(mail);
            }
        } catch (RuntimeException e) {
            log.error(e, e);
            throw e;
        } catch (Exception e) {
            log.error(e, e);
            throw new RuntimeException("An error has occurred with sending the test email:\n" + ExceptionUtils.getStackTrace(e));
        }
    }

    // TODO: Cache for a period of time? 1hr?
    public List<RankingResult> getTopRankedContent(Space space) {
        // Get the data for this space
        StatProRankingData data = getData(space.getKey(), true);
        // Put the map into a tree map with custom comparator
        List<StatProRankingDataSet> entities = new ArrayList<StatProRankingDataSet>( data.getEntities().values() );
        Collections.sort(entities, new Comparator<StatProRankingDataSet>() {
            public int compare(StatProRankingDataSet dataSet1, StatProRankingDataSet dataSet2) {
                int pos1 = dataSet1.getPositive().size();
                int pos2 = dataSet2.getPositive().size();
                return pos1 == pos2 ? 0 : pos1 < pos2 ? 1 : -1; 
            }
        } );
        // Convert the entities into results
        List<RankingResult> results = new ArrayList<RankingResult>();
        for (StatProRankingDataSet dataSet : entities) {
            // Grab the page
            AbstractPage page = pageManager.getAbstractPage( dataSet.getEntityId() );
            if (page == null || page.isDeleted()) continue;            
            // Build the result
            RankingResult result = new RankingResult();
            result.setTitle( page.getTitle() );
            result.setUrlPath( page.getUrlPath() );
            results.add( result );
            // Stop if we've hit the top count
            if (results.size() >= TOP_COUNT) break;
        }
        // If the list has less items than the top count, pad it out with recently updated content from this space
        if (results.size() < TOP_COUNT) {
            List<RankingResult> recentlyUpdated = getRecentlyUpdatedContent(space);
            // Add the items that aren't already in the list
            for (RankingResult each : recentlyUpdated) {
                if (results.size() >= TOP_COUNT) break;
                if (!results.contains( each )) results.add( each );
            }
        }
        // Return the pages
        return results;
    }

    @SuppressWarnings("unchecked")
    public List<RankingResult> getRecentlyUpdatedContent(Space space) {
        ListQuery query = new ListQuery();
        query.setSpaceList( Arrays.asList( space ) );
        query.addStatus( ContentEntityObject.CREATED ).addStatus( ContentEntityObject.MODIFIED );
        query.setTypeList( Arrays.asList( Page.CONTENT_TYPE ) );
        query.setMaxResults( 1000 );
        query.setUser( AuthenticatedUserThreadLocal.getUser() );
        query.setTimeSpan( ListQuery.UNLIMITED_TIMESPAN );
        List<SearchResultWithExcerpt> searchResults = (List<SearchResultWithExcerpt>) smartListManager.getListQueryResults(query, false);
        // Convert the search results
        List<RankingResult> results = new ArrayList<RankingResult>();
        for (SearchResultWithExcerpt searchResult : searchResults) {
            // TODO: Check this is reliable on pages with lots of content
            if (((String) searchResult.get("contentBody")).indexOf("{rank-this-page}") > -1) {
                RankingResult result = new RankingResult();
                result.setTitle( (String) searchResult.get("title") );
                result.setUrlPath( (String) searchResult.get("urlPath") );
                results.add( result );
            }
        }
        return results;
    }

    // Singleton API

    private static StatProRankingManager instance;

    public static StatProRankingManager getInstance() {
        if (instance == null) {
            instance = new StatProRankingManager();
            ContainerManager.autowireComponent( instance );
        }
        return instance;
    }

}
