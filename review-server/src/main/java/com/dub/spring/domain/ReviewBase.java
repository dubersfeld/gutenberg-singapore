package com.dub.spring.domain;


import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

public class ReviewBase {

	@Id
	private String id;
	
	private LocalDateTime date;
	private String title;
	private String text;
	private int rating;
	private String username;
	private int helpfulVotes;
	
	public ReviewBase() {
		
	}
	
	public ReviewBase(ReviewBase that) {
		this.id = that.id;
		this.date = that.date;
		this.title = that.title;
		this.text = that.text;
		this.rating = that.rating;
		this.username = that.username;
		this.helpfulVotes = that.helpfulVotes;
	}
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public LocalDateTime getDate() {
		return date;
	}
	public void setDate(LocalDateTime date) {
		this.date = date;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public int getHelpfulVotes() {
		return helpfulVotes;
	}
	public void setHelpfulVotes(int helpfulVotes) {
		this.helpfulVotes = helpfulVotes;
	}	
}
