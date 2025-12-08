package org.utn.ba.product.controllers;

import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.utn.ba.product.dto.ProductInputDTO;
import org.utn.ba.product.dto.ProductOutputDTO;
import org.utn.ba.product.services.IFileUploadService;
import org.utn.ba.product.services.IProductService;
import org.utn.ba.product.services.IdempotencyKeyManager;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/products", produces = "application/json")
public class ProductController {


  @Autowired
  private IProductService productService;

  @Autowired
  private IFileUploadService fileUploadService;

  @Autowired
  private IdempotencyKeyManager idempotencyKeyManager;

  @GetMapping
  public ResponseEntity<List<ProductOutputDTO>> getAllProducts() {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(this.productService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductOutputDTO> getProductById(@PathVariable Long id) {

    ProductOutputDTO product = this.productService.findById(id);

    if (product == null)
      throw new NotFoundException("No product was found with ID" + id);

    return ResponseEntity.ok(product);
  }

  @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})

  public ResponseEntity<Long> createProduct(@RequestPart("product") ProductInputDTO productInputDTO,
                                            @RequestPart("file") MultipartFile file,
  @RequestHeader(name = "Idempotency-Key", required = true) String idempotencyKey) {

    Optional<Long> cachedId = idempotencyKeyManager.getResponse(idempotencyKey, Long.class);
    if (cachedId.isPresent()) {
      return ResponseEntity.status(HttpStatus.CREATED).body(cachedId.get());
    }

    String imageUrl = fileUploadService.saveImage(file);

    productInputDTO.setImageUrl(imageUrl);

    Long id = this.productService.createProduct(productInputDTO,idempotencyKey,idempotencyKeyManager);

    return ResponseEntity.status(HttpStatus.CREATED).body(id);
  }
}
