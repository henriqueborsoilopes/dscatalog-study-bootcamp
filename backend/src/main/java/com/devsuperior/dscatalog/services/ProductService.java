package com.devsuperior.dscatalog.services;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.ControllerNotFoundException;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;


@Service
public class ProductService {
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(Long categoryId, String name, Pageable pageable) {
		List<Category> categories = (categoryId == 0) ? null : Arrays.asList(categoryRepository.getOne(categoryId));
		Page<Product> page = productRepository.find(categories, name, pageable);
		productRepository.findProductsWithCategories(page.getContent());
		return page.map(x -> new ProductDTO(x, x.getCategories()));
	}
	
	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> optional = productRepository.findById(id);
		Product product = optional.orElseThrow(() -> new ControllerNotFoundException("Entity not found!"));
		return new ProductDTO(product, product.getCategories());
	}
	
	@Transactional
	public ProductDTO insert(ProductDTO productDTO) {
		Product product = new Product();
		copyDtoToEntity(productDTO, product);
		product = productRepository.save(product);
		return new ProductDTO(product);
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO productDTO) {
		try {
			Product product = productRepository.getOne(id);
			copyDtoToEntity(productDTO, product);
			product = productRepository.save(product);
			return new ProductDTO(product);
		}
		catch (EntityNotFoundException e) {
			throw new ControllerNotFoundException("Id not found " + id);
		}
	}

	public void delete(Long id) {
		try {
			productRepository.deleteById(id);
		}
		catch (EmptyResultDataAccessException e) {
			throw new ControllerNotFoundException("Id not found " + id);
		}
		catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Integrity violation");
		}
	}
	
	private void copyDtoToEntity(ProductDTO productDTO, Product product) {
		product.setName(productDTO.getName());
		product.setDescription(productDTO.getDescription());
		product.setPrice(productDTO.getPrice());
		product.setImgUrl(productDTO.getImgUrl());
		product.setDate(productDTO.getDate());
		
		product.getCategories().clear();
		for (CategoryDTO categoryDTO : productDTO.getCategories()) {
			Category category = categoryRepository.getOne(categoryDTO.getId());
			product.getCategories().add(category);
		}
	}
}
