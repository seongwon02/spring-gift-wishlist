package gift.service;

import gift.dto.ProductRequestDto;
import gift.dto.ProductResponseDto;
import gift.entity.Product;
import gift.exception.product.ProductNotFoundException;
import gift.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponseDto> findAllProducts(){

        List<Product> findList = productRepository.findAllProducts();

        List<ProductResponseDto> dtoList = findList
                .stream()
                .map(x -> new ProductResponseDto(
                        x.getId(),
                        x.getName(),
                        x.getPrice(),
                        x.getImageUrl(),
                        x.getApproved(),
                        x.getDescription()
                        ))
                .toList();

        return dtoList;
    }

    @Override
    public ProductResponseDto findProductById(Long id) {

        return productRepository.findProductById(id)
                .map(product -> new ProductResponseDto(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getImageUrl(),
                        product.getApproved(),
                        product.getDescription()))
                .orElse(null);
    }

    @Override
    public ProductResponseDto findProductByIdElseThrow(Long id) {
        return productRepository.findProductById(id)
                .map(product -> new ProductResponseDto(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getImageUrl(),
                        product.getApproved(),
                        product.getDescription()))
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "해당 ID의 상품을 찾을 수 없습니다."
                        )
                );
    }

    @Override
    public ProductResponseDto saveProduct(ProductRequestDto dto) {

        String name = dto.getName();
        Long price = dto.getPrice();
        String imageUrl = dto.getImageUrl();
        boolean approved = !name.contains("카카오");
        String description = "";

        if (!approved)
            description = "카카오 문구가 담긴 상품은 담당 MD와 협의 후 사용가능합니다.";

        Product product = productRepository.saveProduct(name, price, imageUrl, approved, description);

        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageUrl(),
                product.getApproved(),
                product.getDescription());
    }

    @Transactional
    @Override
    public ProductResponseDto updateProduct(Long id, ProductRequestDto dto) {

        boolean approved = !dto.getName().contains("카카오");
        String description = "";

        if (!approved)
            description = "카카오 문구가 담긴 상품은 담당 MD와 협의 후 사용가능합니다.";

        int updatedNum = productRepository.updateProduct(
                id,
                dto.getName(),
                dto.getPrice(),
                dto.getImageUrl(),
                approved,
                description
        );

        if (updatedNum == 0) {
            throw new ProductNotFoundException(
                    "해당 ID의 상품은 존재하지 않습니다."
            );
        }

        return findProductById(id);
    }

    @Override
    public void deleteProduct(Long id) {

        int deletedNum = productRepository.deleteProduct(id);

        if (deletedNum == 0) {
            throw new ProductNotFoundException(
                    "해당 ID의 상품은 존재하지 않습니다."
            );
        }
    }
}
