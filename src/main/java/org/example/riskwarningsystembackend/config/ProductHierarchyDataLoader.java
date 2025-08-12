package org.example.riskwarningsystembackend.config;

import org.example.riskwarningsystembackend.entity.ProductHierarchy;
import org.example.riskwarningsystembackend.entity.ProductInfo;
import org.example.riskwarningsystembackend.repository.ProductHierarchyRepository;
import org.example.riskwarningsystembackend.repository.ProductInfoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(2)
public class ProductHierarchyDataLoader implements CommandLineRunner {

    private final ProductInfoRepository productInfoRepository;
    private final ProductHierarchyRepository productHierarchyRepository;

    public ProductHierarchyDataLoader(ProductInfoRepository productInfoRepository, ProductHierarchyRepository productHierarchyRepository) {
        this.productInfoRepository = productInfoRepository;
        this.productHierarchyRepository = productHierarchyRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (productHierarchyRepository.count() == 0) {
            loadProductHierarchyData();
        }
    }

    private void loadProductHierarchyData() {
        List<ProductInfo> allProducts = productInfoRepository.findAll();
        Map<String, ProductHierarchy> hierarchyMap = new HashMap<>();

        for (ProductInfo productInfo : allProducts) {
            String parentName = null;
            String[] levels = {productInfo.getLevel1(), productInfo.getLevel2(), productInfo.getLevel3(), productInfo.getLevel4(), productInfo.getLevel5()};

            for (int i = 0; i < levels.length; i++) {
                String currentProduct = levels[i];
                if (StringUtils.hasText(currentProduct)) {
                    // Only add the product if it hasn't been processed yet.
                    // This ensures that each product has only one parent (the first one encountered).
                    hierarchyMap.putIfAbsent(currentProduct, new ProductHierarchy(currentProduct, parentName, i + 1));
                    parentName = currentProduct; // The current product becomes the parent for the next level.
                }
            }
        }

        productHierarchyRepository.saveAll(hierarchyMap.values());
    }
}
