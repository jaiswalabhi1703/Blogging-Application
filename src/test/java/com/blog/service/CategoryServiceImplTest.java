package com.blog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.blog.entities.Category;
import com.blog.exceptions.ResourceNotFoundException;
import com.blog.payloads.CategoryDto;
import com.blog.repositories.CategoryRepo;
import com.blog.service.impl.CategoryServiceImpl;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

	@Mock
	private CategoryRepo categoryRepo;

	@Mock
	private ModelMapper modelMapper;

	@InjectMocks
	private CategoryServiceImpl categoryService;

	@Test
	void createCategoryPersistsAndReturnsDto() {
		CategoryDto input = new CategoryDto();
		input.setCategoryTitle("Tech");
		input.setCategoryDescription("Technology posts");

		Category entity = new Category();
		entity.setCategoryTitle("Tech");

		when(modelMapper.map(input, Category.class)).thenReturn(entity);
		when(categoryRepo.save(entity)).thenReturn(entity);
		when(modelMapper.map(entity, CategoryDto.class)).thenReturn(input);

		CategoryDto result = categoryService.createCategory(input);

		assertThat(result.getCategoryTitle()).isEqualTo("Tech");
	}

	@Test
	void getCategoryThrowsWhenMissing() {
		when(categoryRepo.findById(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> categoryService.getCategory(99))
				.isInstanceOf(ResourceNotFoundException.class);
	}
}
