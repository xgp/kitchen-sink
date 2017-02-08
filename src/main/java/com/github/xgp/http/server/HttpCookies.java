package com.github.xgp.http.server;

import java.net.HttpCookie;

/**
 * Static utilities and Builder for making HttpCookies
 */
public class HttpCookies {

    public static class Builder {

	private final HttpCookie cookie;

	public Builder(String name, String value) {
	    this.cookie = new HttpCookie(name, value);
	}

	public HttpCookie build() {
	    return this.cookie;
	}

	public Builder comment(String comment) {
	    this.cookie.setComment(comment);
	    return this;
	}

	public Builder commentUrl(String commentUrl) {
	    this.cookie.setCommentURL(commentUrl);
	    return this;
	}

	public Builder discard(boolean discard) {
	    this.cookie.setDiscard(discard);
	    return this;
	}

	public Builder domain(String domain) {
	    this.cookie.setDomain(domain);
	    return this;
	}

	public Builder httpOnly(boolean httpOnly) {
	    this.cookie.setHttpOnly(httpOnly);
	    return this;
	}

	public Builder maxAge(long maxAge) {
	    this.cookie.setMaxAge(maxAge);
	    return this;
	}

	public Builder path(String path) {
	    this.cookie.setPath(path);
	    return this;
	}

	public Builder portList(String portList) {
	    this.cookie.setPortlist(portList);
	    return this;
	}

	public Builder secure(boolean secure) {
	    this.cookie.setSecure(secure);
	    return this;
	}

	public Builder value(String value) {
	    this.cookie.setValue(value);
	    return this;
	}

	public Builder version(int version) {
	    this.cookie.setVersion(version);
	    return this;
	}
	
    }
    
    public static HttpCookie cookie(String name, String value) {
	return new Builder(name, value).build();
    }

    public static HttpCookie cookie(String name, String value, int maxAge) {
	return new Builder(name, value).maxAge(maxAge).build();
    }
    
    public static HttpCookie cookie(String name, String value, int maxAge, boolean secured) {
	return new Builder(name, value).maxAge(maxAge).secure(secured).build();
    }

    public static HttpCookie cookie(String name, String value, int maxAge, boolean secured, boolean httpOnly) {
	return new Builder(name, value).maxAge(maxAge).secure(secured).httpOnly(httpOnly).build();
    }
    
    public static HttpCookie cookie(String path, String name, String value, int maxAge, boolean secured) {
	return new Builder(name, value).path(path).maxAge(maxAge).secure(secured).build();
    }

    public static HttpCookie cookie(String path, String name, String value, int maxAge, boolean secured, boolean httpOnly) {
	return new Builder(name, value).path(path).maxAge(maxAge).secure(secured).httpOnly(httpOnly).build();
    }

}
