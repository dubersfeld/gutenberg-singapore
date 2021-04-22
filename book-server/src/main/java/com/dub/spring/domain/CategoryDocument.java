package com.dub.spring.domain;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="categories")
public class CategoryDocument extends CategoryBase {
	
	private ObjectId parentId;	
	private List<ObjectId> children;
	
	public CategoryDocument() {
		super(new CategoryBase());
		this.children = new ArrayList<>();
	}
	
	public CategoryDocument(CategoryBase that) {
		super(that);
		this.children = new ArrayList<>();
	}
	

	public ObjectId getParentId() {
		return parentId;
	}

	public void setParentId(ObjectId parentId) {
		this.parentId = parentId;
	}

	public List<ObjectId> getChildren() {
		return children;
	}

	public void setChildren(List<ObjectId> children) {
		this.children = children;
	}
}