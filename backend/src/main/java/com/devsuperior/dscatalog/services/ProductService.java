package com.devsuperior.dscatalog.services;


import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.ProductRespository;
import com.devsuperior.dscatalog.services.exceptions.ControllerNotFoundException;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;


@Service
public class ProductService {
	
	@Autowired
	private ProductRespository productRepository;
	
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(PageRequest pageRequest) {
		Page<Product> list = productRepository.findAll(pageRequest);
		return list.map(x -> new ProductDTO(x));
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
		//product.setName(productDTO.getName());
		product = productRepository.save(product);
		return new ProductDTO(product);
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO productDTO) {
		try {
			Product product = productRepository.getOne(id);
			//product.setName(productDTO.getName());
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
}
