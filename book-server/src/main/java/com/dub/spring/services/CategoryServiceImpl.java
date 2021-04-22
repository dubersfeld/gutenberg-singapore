package com.dub.spring.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dub.spring.domain.Category;
import com.dub.spring.domain.CategoryDocument;
import com.dub.spring.repository.CategoryRepository;
import com.dub.spring.utils.CategoryUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;

	
	@Override
	public Mono<Category> getCategory(String categorySlug) {
		Mono<CategoryDocument> doc = categoryRepository.findOneBySlug(categorySlug);
		return doc.map(d -> CategoryUtils.documentToCategory(d));
	}
	
	
	@Override
	public Flux<Category> findAllCategories() {
			
		Flux<CategoryDocument> docs = categoryRepository.findAll();
	
		Flux<Category> cats = docs.map(d -> CategoryUtils.documentToCategory(d));
		
		return cats;
	}

/*
	@Override
	public List<Category> getLeaveCategories() {
		
		List<Category> cats = getAllCategories();
		List<Category> leaves = new ArrayList<>();
		
		for (Category cat : cats) {
			if (cat.getChildren().isEmpty()) {
				leaves.add(cat);
			}
		}
		return leaves;
	}

	@Override
	public Category getCategory(String categorySlug) {
		DocumentCategory doc = categoryRepository.findOneBySlug(categorySlug);
		if (doc != null) {
			return CategoryUtils.documentToCategory(doc);
		} else {
			throw new CategoryNotFoundException();
		}
		
	}
*/
}
