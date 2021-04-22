package com.dub.client.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CategoryWebList {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<Category> categories;

	public CategoryWebList() { }

	// shallow copy
	public CategoryWebList(List<Category> source) {
		categories = source;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	

}
