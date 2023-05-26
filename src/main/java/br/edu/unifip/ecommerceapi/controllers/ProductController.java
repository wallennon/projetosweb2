package br.edu.unifip.ecommerceapi.controllers;

import br.edu.unifip.ecommerceapi.dtos.ProductDto;
import br.edu.unifip.ecommerceapi.models.Product;
import br.edu.unifip.ecommerceapi.services.ProductService;
import br.edu.unifip.ecommerceapi.utils.FileDownloadUtil;
import br.edu.unifip.ecommerceapi.utils.FileUploadUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("api/products")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {
    final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(productService.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Product>> getProductsIsActive() {
        return ResponseEntity.status(HttpStatus.OK).body(productService.findByActiveTrue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getProductById(@PathVariable(value = "id") UUID id) {
        Optional<Product> productOptional = productService.findById(id);
        if (productOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(productOptional.get());
    }

    @PostMapping
    public ResponseEntity<Object> saveProduct(@Valid ProductDto productDto, HttpServletRequest request) throws IOException {
        var product = new Product();

        BeanUtils.copyProperties(productDto, product); // O que vai ser convertido para o quê vai ser convertido

        UUID categoryId = null;

        if (productDto.getCategory() != null) {
            categoryId = productDto.getCategory();
        }

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartRequest.getFile("image");

        if (multipartFile != null) {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            String uploadDir = "product-images/";

            try {
                String filecode = FileUploadUtil.saveFile(fileName, uploadDir, multipartFile);
                product.setImage("/api/products/product-images/" + filecode);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Image not accepted.");
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(productService.save(product, categoryId));
    }


    //inserir aqui mais um DeleteMapping. Um para o Hard e outro para o Soft

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable(value = "id") UUID id) {
        Optional<Product> productOptional = productService.findById(id);
        if (productOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        productService.delete(productOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully.");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable(value = "id") UUID id, HttpServletRequest request) {
        Optional<Product> productOptional = productService.findById(id);
        if (productOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }

        Map<Object, Object> objectMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            objectMap.put(entry.getKey(), entry.getValue()[0]);
        }

        // Salvar a url da imagem em uma variável separada
        String imageUrl = null;
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartRequest.getFile("image");
        if (multipartFile != null) {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            String uploadDir = "product-images/";

            try {
                String filecode = FileUploadUtil.saveFile(fileName, uploadDir, multipartFile);
                imageUrl = "/api/products/product-images/" + filecode;
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Image not accepted.");
            }
        }

        // Adicionar a url da imagem ao objeto mapeado, se ela foi enviada
        if (imageUrl != null) {
            objectMap.put("image", imageUrl);
        }

        productService.partialUpdate(productOptional.get(), objectMap);

        return ResponseEntity.status(HttpStatus.OK).body(productOptional.get());
    }

    @GetMapping("/findByName")
    public ResponseEntity<List<Product>> getProductByName(@Validated @RequestParam(value = "name") String name) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.findByName(name));
    }

    @GetMapping("/findByCategoryName")
    public ResponseEntity<List<Product>> getProductByCategoryName(@Validated @RequestParam(value = "name") String name) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.findByCategoryName(name));
    }

    @GetMapping("/product-images/{fileCode}")
    public ResponseEntity<?> downloadFile(@PathVariable("fileCode") String fileCode) {
        FileDownloadUtil downloadUtil = new FileDownloadUtil();

        Resource resource = null;
        try {
            resource = downloadUtil.getFileAsResource(fileCode, "product-images");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

        if (resource == null) {
            return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
        }

        MediaType contentType;

        if (Objects.equals(FilenameUtils.getExtension(resource.getFilename()), "jpg")) {
            contentType = MediaType.IMAGE_JPEG;
        } else {
            contentType = MediaType.IMAGE_PNG;
        }

        String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .body(resource);
    }
}
