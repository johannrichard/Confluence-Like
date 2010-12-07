package com.adaptavist.confluence.statpro.ranking;

public class RankingResult {

    private String urlPath;
    private String title;

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RankingResult that = (RankingResult) o;

        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (urlPath != null ? !urlPath.equals(that.urlPath) : that.urlPath != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (urlPath != null ? urlPath.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }
    
}
