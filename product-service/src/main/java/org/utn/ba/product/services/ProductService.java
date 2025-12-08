package org.utn.ba.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.utn.ba.product.dto.ProductInputDTO;
import org.utn.ba.product.dto.ProductOutputDTO;
import org.utn.ba.product.mappers.ProductMapper;
import org.utn.ba.product.models.entities.Product;
import org.utn.ba.product.models.repositories.ProductRepository;
import java.util.List;

@Service
public class ProductService implements IProductService {

  @Autowired
  private ProductRepository productRepository;

  @Override
  public List<ProductOutputDTO> findAll() {
    return this.productRepository.findAll().stream().map(ProductMapper::createFrom).toList();
  }

  @Override
  public ProductOutputDTO findById(Long id) {
    return this.productRepository.findById(id).map(ProductMapper::createFrom).orElse(null);
  }

  @Override
  public Long createProduct(ProductInputDTO product, String idempotencyKey, IdempotencyKeyManager idempotencyKeyManager) {

    idempotencyKeyManager.tryLock(idempotencyKey);


    Product newProduct = null;
    try {
      newProduct = new ObjectMapper().convertValue(product, Product.class);
      newProduct.setIdempotencyKey(idempotencyKey);
      this.productRepository.save(newProduct);
      idempotencyKeyManager.storeResponse(idempotencyKey, newProduct.getId());
      return newProduct.getId();
    } catch (Exception e) {
      idempotencyKeyManager.cleanupKey(idempotencyKey);
      throw new RuntimeException(e);
    }
  }
}
