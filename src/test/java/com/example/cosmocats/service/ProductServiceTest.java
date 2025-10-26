package com.example.cosmocats.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.cosmocats.domain.Product;
import com.example.cosmocats.dto.product.ProductDto;
import com.example.cosmocats.dto.product.ProductListDto;
import com.example.cosmocats.dto.product.ProductUpdateDto;
import com.example.cosmocats.exception.ProductAlreadyExistsException;
import com.example.cosmocats.exception.ProductNotFoundException;
import com.example.cosmocats.repository.ProductRepository;
import com.example.cosmocats.service.mapper.ProductMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private UUID productId;
    private Product product;
    private ProductDto productDto;
    private ProductUpdateDto productUpdateDto;

    @BeforeEach
    void setUp() {
        productId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

        product = Product.builder()
                .productId(productId)
                .category("Electronics")
                .name("Quantum Phone X1")
                .description("Advanced smartphone with quantum processor")
                .price(999.99f)
                .build();

        productDto = ProductDto.builder()
                .productId(productId)
                .category("Electronics")
                .name("Quantum Phone X1")
                .description("Advanced smartphone with quantum processor")
                .price(999.99f)
                .build();

        productUpdateDto = ProductUpdateDto.builder()
                .category("Electronics")
                .name("Quantum Phone X1")
                .description("Advanced smartphone with quantum processor")
                .price(999.99f)
                .build();
    }

    @Test
    @DisplayName("createProduct - should create product successfully")
    void createProduct_ShouldCreateProductSuccessfully() {
        // Arrange
        when(productRepository.existsByName(productUpdateDto.getName())).thenReturn(false);
        when(productMapper.toProduct(productUpdateDto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toProductDto(product)).thenReturn(productDto);

        // Act
        ProductDto result = productService.createProduct(productUpdateDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("Quantum Phone X1");

        verify(productRepository).existsByName(productUpdateDto.getName());
        verify(productMapper).toProduct(productUpdateDto);
        verify(productRepository).save(product);
        verify(productMapper).toProductDto(product);
    }

    @Test
    @DisplayName("createProduct - should throw exception when product already exists")
    void createProduct_WhenProductExists_ShouldThrowException() {
        // Arrange
        when(productRepository.existsByName(productUpdateDto.getName())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(productUpdateDto))
                .isInstanceOf(ProductAlreadyExistsException.class)
                .hasMessage("Product already exists with name: " + productUpdateDto.getName());

        verify(productRepository).existsByName(productUpdateDto.getName());
        verify(productMapper, never()).toProduct(any());
        verify(productRepository, never()).save(any());
        verify(productMapper, never()).toProductDto(any());
    }

    @Test
    @DisplayName("getProductById - should return product when exists")
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toProductDto(product)).thenReturn(productDto);

        // Act
        ProductDto result = productService.getProductById(productId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("Quantum Phone X1");

        verify(productRepository).findById(productId);
        verify(productMapper).toProductDto(product);
    }

    @Test
    @DisplayName("getProductById - should throw exception when product not found")
    void getProductById_WhenProductNotExists_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProductById(productId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id: " + productId);

        verify(productRepository).findById(productId);
        verify(productMapper, never()).toProductDto(any());
    }

    @Test
    @DisplayName("getAllProducts - should return all products")
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        List<Product> products = List.of(product);
        ProductListDto productListDto = ProductListDto.builder()
                .products(List.of(productDto))
                .build();

        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toProductListDto(products)).thenReturn(productListDto);

        // Act
        ProductListDto result = productService.getAllProducts();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).hasSize(1);
        assertThat(result.getProducts().get(0).getProductId()).isEqualTo(productId);

        verify(productRepository).findAll();
        verify(productMapper).toProductListDto(products);
    }

    @Test
    @DisplayName("getAllProducts - should return empty list when no products")
    void getAllProducts_WhenNoProducts_ShouldReturnEmptyList() {
        // Arrange
        List<Product> emptyProducts = List.of();
        ProductListDto emptyProductListDto = ProductListDto.builder()
                .products(List.of())
                .build();

        when(productRepository.findAll()).thenReturn(emptyProducts);
        when(productMapper.toProductListDto(emptyProducts)).thenReturn(emptyProductListDto);

        // Act
        ProductListDto result = productService.getAllProducts();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).isEmpty();

        verify(productRepository).findAll();
        verify(productMapper).toProductListDto(emptyProducts);
    }

    @Test
    @DisplayName("updateProduct - should update product successfully")
    void updateProduct_ShouldUpdateProductSuccessfully() {
        // Arrange
        Product updatedProduct = product.toBuilder()
                .name("Updated Quantum Phone")
                .price(1099.99f)
                .build();

        ProductDto updatedProductDto = productDto.toBuilder()
                .name("Updated Quantum Phone")
                .price(1099.99f)
                .build();

        when(productRepository.existsById(productId)).thenReturn(true);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsByName(updatedProduct.getName())).thenReturn(false);
        when(productMapper.toProductWithId(productId, productUpdateDto)).thenReturn(updatedProduct);
        when(productRepository.save(updatedProduct)).thenReturn(updatedProduct);
        when(productMapper.toProductDto(updatedProduct)).thenReturn(updatedProductDto);

        // Act
        ProductDto result = productService.updateProduct(productId, productUpdateDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Quantum Phone");
        assertThat(result.getPrice()).isEqualTo(1099.99f);

        verify(productRepository).existsById(productId);
        verify(productRepository).findById(productId);
        verify(productRepository).existsByName(updatedProduct.getName());
        verify(productMapper).toProductWithId(productId, productUpdateDto);
        verify(productRepository).save(updatedProduct);
        verify(productMapper).toProductDto(updatedProduct);
    }

    @Test
    @DisplayName("updateProduct - should throw exception when product not found for update")
    void updateProduct_WhenProductNotExists_ShouldThrowException() {
        // Arrange
        when(productRepository.existsById(productId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(productId, productUpdateDto))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id: " + productId);

        verify(productRepository).existsById(productId);
        verify(productRepository, never()).save(any());
        verify(productMapper, never()).toProductDto(any());
    }

    @Test
    @DisplayName("updateProduct - should throw exception when product name already exists")
    void updateProduct_WhenProductNameExists_ShouldThrowException() {
        // Arrange
        String existingProductName = "Existing Product";
        ProductUpdateDto updateDtoWithExistingName = productUpdateDto.toBuilder()
                .name(existingProductName)
                .build();

        when(productRepository.existsById(productId)).thenReturn(true);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsByName(existingProductName)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(productId, updateDtoWithExistingName))
                .isInstanceOf(ProductAlreadyExistsException.class)
                .hasMessage("Product already exists with name: " + existingProductName);

        verify(productRepository).existsById(productId);
        verify(productRepository).findById(productId);
        verify(productRepository).existsByName(existingProductName);
        verify(productRepository, never()).save(any());
        verify(productMapper, never()).toProductDto(any());
    }

    @Test
    @DisplayName("updateProduct - should allow update when name unchanged")
    void updateProduct_WhenNameUnchanged_ShouldUpdateSuccessfully() {
        // Arrange
        when(productRepository.existsById(productId)).thenReturn(true);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        // existsByName should not be called when name is unchanged
        when(productMapper.toProductWithId(productId, productUpdateDto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toProductDto(product)).thenReturn(productDto);

        // Act
        ProductDto result = productService.updateProduct(productId, productUpdateDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);

        verify(productRepository).existsById(productId);
        verify(productRepository).findById(productId);
        verify(productRepository, never()).existsByName(any());
        verify(productMapper).toProductWithId(productId, productUpdateDto);
        verify(productRepository).save(product);
        verify(productMapper).toProductDto(product);
    }

    @Test
    @DisplayName("deleteProduct - should delete product when exists")
    void deleteProduct_WhenProductExists_ShouldDeleteProduct() {
        // Arrange
        when(productRepository.existsById(productId)).thenReturn(true);

        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(productRepository).existsById(productId);
        verify(productRepository).deleteById(productId);
    }

    @Test
    @DisplayName("deleteProduct - should not throw exception when product not found")
    void deleteProduct_WhenProductNotExists_ShouldNotThrowException() {
        // Arrange
        when(productRepository.existsById(productId)).thenReturn(false);

        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(productRepository).existsById(productId);
        verify(productRepository, never()).deleteById(productId);
    }

    @Test
    @DisplayName("getProductsByCategory - should return products by category")
    void getProductsByCategory_ShouldReturnProducts() {
        // Arrange
        String category = "Electronics";
        List<Product> electronicsProducts = List.of(product);
        ProductListDto productListDto = ProductListDto.builder()
                .products(List.of(productDto))
                .build();

        when(productRepository.findAll()).thenReturn(electronicsProducts);
        when(productMapper.toProductListDto(electronicsProducts)).thenReturn(productListDto);

        // Act
        ProductListDto result = productService.getProductsByCategory(category);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).hasSize(1);
        assertThat(result.getProducts().get(0).getCategory()).isEqualTo(category);

        verify(productRepository).findAll();
        verify(productMapper).toProductListDto(electronicsProducts);
    }

    @Test
    @DisplayName("getProductsByCategory - should return empty list when no products in category")
    void getProductsByCategory_WhenNoProductsInCategory_ShouldReturnEmptyList() {
        // Arrange
        String category = "NonExistentCategory";
        List<Product> allProducts = List.of(product); // Only electronics products
        ProductListDto emptyProductListDto = ProductListDto.builder()
                .products(List.of())
                .build();

        when(productRepository.findAll()).thenReturn(allProducts);
        when(productMapper.toProductListDto(List.of())).thenReturn(emptyProductListDto);

        // Act
        ProductListDto result = productService.getProductsByCategory(category);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).isEmpty();

        verify(productRepository).findAll();
        verify(productMapper).toProductListDto(List.of());
    }

    @Test
    @DisplayName("getProductsByCategory - should be case insensitive")
    void getProductsByCategory_ShouldBeCaseInsensitive() {
        // Arrange
        String categoryUpperCase = "ELECTRONICS";
        List<Product> electronicsProducts = List.of(product);
        ProductListDto productListDto = ProductListDto.builder()
                .products(List.of(productDto))
                .build();

        when(productRepository.findAll()).thenReturn(electronicsProducts);
        when(productMapper.toProductListDto(electronicsProducts)).thenReturn(productListDto);

        // Act
        ProductListDto result = productService.getProductsByCategory(categoryUpperCase);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).hasSize(1);

        verify(productRepository).findAll();
        verify(productMapper).toProductListDto(electronicsProducts);
    }
}