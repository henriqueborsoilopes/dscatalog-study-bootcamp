package com.devsuperior.dscatalog.controllers;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.ControllerNotFoundException;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.tests.Factory;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductController.class)
public class ProductControllerTests {

	private PageImpl<ProductDTO> page;
	private ProductDTO productDTO;
	private long existingId;
	private long nonExistingId;
	private long dependentId;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductService productService;
	
	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void setup() throws Exception {
		productDTO = Factory.createProductDTO();
		page = new PageImpl<>(List.of(productDTO));
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;

		Mockito.when(productService.findAllPaged(ArgumentMatchers.any())).thenReturn(page);
		Mockito.when(productService.findById(existingId)).thenReturn(productDTO);
		Mockito.when(productService.findById(nonExistingId)).thenThrow(ControllerNotFoundException.class);
		Mockito.when(productService.update(ArgumentMatchers.eq(existingId), ArgumentMatchers.any())).thenReturn(productDTO);
		Mockito.when(productService.update(ArgumentMatchers.eq(nonExistingId), ArgumentMatchers.any())).thenThrow(ControllerNotFoundException.class);
		
		Mockito.doNothing().when(productService).delete(existingId);
		Mockito.doThrow(ControllerNotFoundException.class).when(productService).delete(nonExistingId);
		Mockito.doThrow(DatabaseException.class).when(productService).delete(dependentId);
		
	}
	
	@Test
	public void updateShouldReturnProductDTOWhenIdExists() throws Exception {
		String jsonBody = objectMapper.writeValueAsString(productDTO); 
		
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}", existingId).content(jsonBody).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.name").exists());
	}
	
	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
		String jsonBody = objectMapper.writeValueAsString(productDTO); 
		
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}", nonExistingId).content(jsonBody).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON));

		result.andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	public void findAllShouldReturnPage() throws Exception {
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/products").accept(MediaType.APPLICATION_JSON));

		result.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void findByIdShouldReturnProductWhenIdExists() throws Exception {
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/products/{id}", existingId).accept(MediaType.APPLICATION_JSON));

		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.name").exists());
	}

	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/products/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));

		result.andExpect(MockMvcResultMatchers.status().isNotFound());
	}
}
