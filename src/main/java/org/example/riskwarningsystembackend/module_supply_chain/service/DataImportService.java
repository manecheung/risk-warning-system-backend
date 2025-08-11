package org.example.riskwarningsystembackend.module_supply_chain.service;

import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.module_chain_risk.entity.CompanyRelationship;
import org.example.riskwarningsystembackend.module_chain_risk.repository.CompanyRelationshipRepository;
import org.example.riskwarningsystembackend.module_supply_chain.entity.Company;
import org.example.riskwarningsystembackend.module_supply_chain.entity.Product;
import org.example.riskwarningsystembackend.module_supply_chain.repository.CompanyRepository;
import org.example.riskwarningsystembackend.module_supply_chain.repository.ProductRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataImportService {

    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final CompanyRelationshipRepository relationshipRepository;

    /**
     * 主方法，用于触发数据导入流程。
     * 它首先导入产品及其层级结构，然后导入公司信息并将其与产品关联，
     * 最后根据公司的产品供应链位置建立它们之间的关系。
     *
     * @throws IOException 如果读取数据文件时发生错误。
     */
    @Transactional
    public void importData() throws IOException {
        // 如果数据已存在，则阻止重新导入，以避免重复数据
        if (companyRepository.count() > 0) {
            return;
        }

        // 1. 从产品信息文件导入产品并建立其层级结构。
        importProductsAndHierarchy("data/风电产品基本信息表v1.6.csv");

        // 2. 从合并后的公司信息文件导入公司信息，并将其与产品关联。
        importCompaniesAndProducts("data/风电行业公司基础信息v1.6.csv");

        // 3. 基于产品连接创建公司关系（供应商、客户、竞争对手）。
        createCompanyRelationships();
    }

    /**
     * 从指定文件导入产品并构建其父子层级关系。
     *
     * @param productHierarchyFile 产品层级结构CSV文件的路径。
     * @throws IOException 如果文件无法读取。
     */
    private void importProductsAndHierarchy(String productHierarchyFile) throws IOException {
        Set<String> allProductNames = new HashSet<>();
        Map<String, String> childToParentMap = new HashMap<>();
        Map<String, Integer> productNameToLevelMap = new HashMap<>();

        ClassPathResource resource = new ClassPathResource(productHierarchyFile);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            br.readLine(); // 跳过表头行
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1);
                String parentName = null;
                for (int i = 0; i < values.length; i++) {
                    String currentName = values[i].trim();
                    if (StringUtils.hasText(currentName)) {
                        allProductNames.add(currentName);
                        productNameToLevelMap.putIfAbsent(currentName, i + 1); // 层级是基于1的索引
                        if (parentName != null) {
                            childToParentMap.putIfAbsent(currentName, parentName);
                        }
                        parentName = currentName;
                    } else {
                        // 如果某个单元格为空，则表示该行的层级链中断。
                        break;
                    }
                }
            }
        }

        // 首先创建并保存所有唯一的产品
        Map<String, Product> savedProducts = new HashMap<>();
        for (String productName : allProductNames) {
            Product product = productRepository.findByProductName(productName).orElseGet(() -> {
                Product newProduct = new Product();
                newProduct.setProductName(productName);
                newProduct.setLevel(productNameToLevelMap.getOrDefault(productName, 0));
                return productRepository.save(newProduct);
            });
            savedProducts.put(product.getProductName(), product);
        }

        // 批量更新父子关系
        List<Product> productsToUpdate = new ArrayList<>();
        for (Map.Entry<String, String> entry : childToParentMap.entrySet()) {
            Product childProduct = savedProducts.get(entry.getKey());
            Product parentProduct = savedProducts.get(entry.getValue());

            if (childProduct != null && parentProduct != null && childProduct.getParent() == null) {
                childProduct.setParent(parentProduct);
                productsToUpdate.add(childProduct);
            }
        }

        if (!productsToUpdate.isEmpty()) {
            productRepository.saveAll(productsToUpdate);
        }
    }

    /**
     * 从合并后的CSV文件导入公司数据。该文件包含基本信息、财务数据、产品关联和地理坐标。
     *
     * @param companyInfoFile 合并后的公司信息CSV文件的路径。
     * @throws IOException 如果文件无法读取。
     */
    private void importCompaniesAndProducts(String companyInfoFile) throws IOException {
        ClassPathResource resource = new ClassPathResource(companyInfoFile);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            br.readLine(); // 跳过表头行
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1); // 使用-1参数以保留末尾的空字符串
                if (values.length < 24) continue; // 确保行中有足够的列

                String companyName = values[0].trim();
                if (!StringUtils.hasText(companyName)) continue;

                // 使用 orElseGet 提高性能，仅在需要时创建新对象
                Company company = companyRepository.findByName(companyName).orElseGet(Company::new);
                company.setName(companyName);

                // CSV列映射: 0:公司, 8:注册资本, 9:实缴资本, 10:企业规模, 11:员工人数, 12:资质证书数量, 13:税务评级,
                // 14:舆情数量, 15:法律诉讼数量, 16:国标行业, 17:股价/大盘指数, 18:营收, 19:资产, 20:利润,
                // 21:注册地, 22:纬度, 23:经度
                company.setRegisteredCapital(values[8]);
                company.setPaidInCapital(values[9]);
                company.setScale(values[10]);
                company.setEmployeeCount(safeParseInt(values[11]));
                company.setCertificateCount(safeParseInt(values[12]));
                company.setTaxRating(values[13]);
                company.setPublicSentimentCount(safeParseInt(values[14]));
                company.setLegalCaseCount(safeParseInt(values[15]));
                company.setIndustry(values[16]);
                company.setStockIndex(safeParseDouble(values[17]));
                company.setRevenue(parseFinancialValue(values[18]));
                company.setAssets(parseFinancialValue(values[19]));
                company.setProfit(parseFinancialValue(values[20]));
                company.setRegisteredAddress(values[21]);
                company.setLatitude(safeParseDouble(values[22]));
                company.setLongitude(safeParseDouble(values[23]));

                // 设置默认风险值，因为源CSV中不包含这些字段
                company.setTech("低");
                company.setFinance("低");
                company.setLaw("低");
                company.setCredit("低");
                company.setReason("无明显风险");

                Company savedCompany = companyRepository.save(company);

                // 将产品与公司关联
                // 产品相关列: 1:主营产品1, 2:主营产品2, 6:主营产品总结, 7:相关产品
                Set<String> productNames = new HashSet<>();
                if (StringUtils.hasText(values[1])) productNames.add(values[1].trim());
                if (StringUtils.hasText(values[2])) productNames.add(values[2].trim());
                if (StringUtils.hasText(values[6])) productNames.add(values[6].trim());
                if (StringUtils.hasText(values[7])) productNames.add(values[7].trim());

                for (String productName : productNames) {
                    productRepository.findByProductName(productName).ifPresent(product -> {
                        savedCompany.getProducts().add(product);
                    });
                }
                companyRepository.save(savedCompany);
            }
        }
    }

    /**
     * 基于产品关联和产品层级，创建所有公司之间的关系（供应商、客户、竞争对手）。
     */
    private void createCompanyRelationships() {
        List<Company> companies = companyRepository.findAll();
        if (companies.size() < 2) return;

        relationshipRepository.deleteAllInBatch(); // 高效地清除旧关系

        List<CompanyRelationship> newRelationships = new ArrayList<>();

        for (int i = 0; i < companies.size(); i++) {
            for (int j = i + 1; j < companies.size(); j++) {
                Company c1 = companies.get(i);
                Company c2 = companies.get(j);

                // 检查供应商/客户关系
                for (Product p1 : c1.getProducts()) {
                    for (Product p2 : c2.getProducts()) {
                        if (isAncestor(p1, p2)) { // c1 为 c2 的产品提供上游原料
                            newRelationships.add(createRelationship(c1, c2, "供应", "supplier"));
                            newRelationships.add(createRelationship(c2, c1, "采购", "customer"));
                        } else if (isAncestor(p2, p1)) { // c2 为 c1 的产品提供上游原料
                            newRelationships.add(createRelationship(c2, c1, "供应", "supplier"));
                            newRelationships.add(createRelationship(c1, c2, "采购", "customer"));
                        }
                    }
                }

                // 检查竞争关系（简化逻辑：如果它们生产的产品拥有共同的父产品）
                Set<Product> c1ParentProducts = c1.getProducts().stream()
                        .map(Product::getParent).filter(Objects::nonNull).collect(Collectors.toSet());
                Set<Product> c2ParentProducts = c2.getProducts().stream()
                        .map(Product::getParent).filter(Objects::nonNull).collect(Collectors.toSet());

                c1ParentProducts.retainAll(c2ParentProducts); // 获取父产品的交集
                if (!c1ParentProducts.isEmpty()) {
                    newRelationships.add(createRelationship(c1, c2, "竞争", "competitor"));
                    newRelationships.add(createRelationship(c2, c1, "竞争", "competitor"));
                }
            }
        }
        relationshipRepository.saveAll(newRelationships);
    }

    // --- 辅助方法 ---

    /**
     * 创建一个新的公司关系实体。
     */
    private CompanyRelationship createRelationship(Company source, Company target, String label, String type) {
        CompanyRelationship rel = new CompanyRelationship();
        rel.setSource(source);
        rel.setTarget(target);
        rel.setLabel(label);
        rel.setType(type);
        return rel;
    }

    /**
     * 检查一个产品是否是另一个产品的上游（祖先）。
     */
    private boolean isAncestor(Product potentialAncestor, Product product) {
        if (potentialAncestor == null || product == null) return false;
        Product current = product.getParent();
        while (current != null) {
            if (current.equals(potentialAncestor)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /**
     * 解析带单位（万、亿）的财务数值字符串。
     */
    private double parseFinancialValue(String value) {
        if (!StringUtils.hasText(value) || "0".equals(value.trim()) || "---".equals(value.trim())) {
            return 0.0;
        }
        String cleanedValue = value.trim();
        try {
            if (cleanedValue.endsWith("亿")) {
                return Double.parseDouble(cleanedValue.substring(0, cleanedValue.length() - 1)) * 100_000_000;
            } else if (cleanedValue.endsWith("万")) {
                return Double.parseDouble(cleanedValue.substring(0, cleanedValue.length() - 1)) * 10_000;
            }
            return Double.parseDouble(cleanedValue);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * 安全地将字符串解析为整数，处理无效输入。
     */
    private int safeParseInt(String value) {
        if (!StringUtils.hasText(value)) return 0;
        try {
            // 处理类似 "1,905" 或 "1885\n" 的情况，移除非数字字符
            return Integer.parseInt(value.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 安全地将字符串解析为双精度浮点数，处理无效输入。
     */
    private double safeParseDouble(String value) {
        if (!StringUtils.hasText(value) || "---".equals(value.trim())) return 0.0;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
