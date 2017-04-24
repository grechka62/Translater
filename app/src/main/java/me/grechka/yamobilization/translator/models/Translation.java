package me.grechka.yamobilization.translator.models;

import java.io.Serializable;

public class Translation implements Serializable {
	private int id = 0;
	private String request;
	private String result;
	private final int lang[] = new int[2];
	private boolean isFavorite;
		
	public String getRequest() {
		return request;
	}
	
	public void setRequest(String request) {
		this.request = request;
	}
	
	public String getResult() {
		return result;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	
	public int getLang(int position) {
		return lang[position];
	}
	
	public void setLang(int position, int lang) {
		this.lang[position] = lang;
	}
	
	public boolean getIsFavorite() {
		return isFavorite;
	}
	
	public void setIsFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}