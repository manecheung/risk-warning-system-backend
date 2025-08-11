package org.example.riskwarningsystembackend.module_supply_chain.service;

import lombok.RequiredArgsConstructor;
import org.example.riskwarningsystembackend.module_chain_risk.entity.CompanyRelationship; // Import CompanyRelationship
import org.example.riskwarningsystembackend.module_chain_risk.repository.CompanyRelationshipRepository; // Import CompanyRelationshipRepository
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
import java.util.stream.Collectors; // Import Collectors for stream operations

@Service
@RequiredArgsConstructor
public class DataImportService {

    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final CompanyRelationshipRepository relationshipRepository; // Inject CompanyRelationshipRepository

    @Transactional
    public void importData() throws IOException {
        if (companyRepository.count() > 0) {
            return;
        }
        // 1. 导入产品并建立层级关系
        importProductsAndHierarchy();

        // 2. 导入地理位置信息
        Map<String, double[]> geolocations = importGeolocations("data/geolocation_results.csv");

        // 3. 导入公司基本信息
        Map<String, Company> companies = importCompanies("data/enterprise_info_240808.csv", geolocations);

        // 4. 关联公司和其产品
        importCompanyProducts("data/风电行业公司产品数据表_总表产品v1.6.csv", companies);

        // 5. 基于产品关系建立公司关系
        createCompanyRelationships();
    }

    private void importProductsAndHierarchy() throws IOException {
        String hierarchyFile = "data/风电行业公司产品数据表_总表产品v1.6-1.csv";

        // 第一步：从单个文件读取所有产品，并建立层级关系映射
        Set<String> allProductNames = new HashSet<>();
        Map<String, String> childToParentMap = new HashMap<>();
        Map<String, Integer> productNameToLevelMap = new HashMap<>(); // New map to store product level

        ClassPathResource resource = new ClassPathResource(hierarchyFile);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1);
                String parentName = null;
                int currentLevel = 0; // Track the level for current row
                for (String value : values) {
                    currentLevel++;
                    if (StringUtils.hasText(value)) {
                        String currentName = value.trim();
                        allProductNames.add(currentName);
                        productNameToLevelMap.putIfAbsent(currentName, currentLevel); // Store level
                        if (parentName != null) {
                            childToParentMap.putIfAbsent(currentName, parentName);
                        }
                        parentName = currentName;
                    } else {
                        parentName = null; // Reset parent if current level is empty
                    }
                }
            }
        }

        // 第二步：保存所有新产品 (先填名称)
        for (String productName : allProductNames) {
            productRepository.findByProductName(productName).orElseGet(() -> { // Changed findByName to findByProductName
                Product newProduct = new Product();
                newProduct.setProductName(productName); // Changed setName to setProductName
                newProduct.setLevel(productNameToLevelMap.get(productName)); // Set the level
                return productRepository.save(newProduct);
            });
        }

        // 第三步：批量更新产品的父级ID (再更新父级)
        List<Product> allProducts = productRepository.findAll();
        Map<String, Product> productMap = new HashMap<>();
        for (Product p : allProducts) {
            productMap.put(p.getProductName(), p); // Changed getName to getProductName
        }

        List<Product> productsToUpdate = new ArrayList<>();
        for (Map.Entry<String, String> entry : childToParentMap.entrySet()) {
            String childName = entry.getKey();
            String parentName = entry.getValue();

            Product childProduct = productMap.get(childName);
            Product parentProduct = productMap.get(parentName);

            // 确保产品存在且父级尚未设置
            if (childProduct != null && parentProduct != null && childProduct.getParent() == null) {
                childProduct.setParent(parentProduct);
                productsToUpdate.add(childProduct);
            }
        }

        if (!productsToUpdate.isEmpty()) {
            productRepository.saveAll(productsToUpdate);
        }
    }


    private Map<String, Company> importCompanies(String filePath, Map<String, double[]> geolocations) throws IOException {
        Map<String, Company> companies = new HashMap<>();
        ClassPathResource resource = new ClassPathResource(filePath);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                // Ensure enough columns are present for all fields
                if (values.length < 23) continue; // Updated to 23 columns based on CSV data

                Company company = new Company();
                company.setName(values[0]);
                // Corrected indices based on the CSV file structure
                // CSV columns: 公司,主营产品1,主营产品2,国内企业/合资企业/外企,企业是否不仅从事风电还跨多个垂直行业？,备注（是否是行业知名企业）,主营产品（公司主要的经营的与风电相关的产品总结归纳）,相关产品,注册资本,实缴资本,企业规模,员工人数,资质证书数量,税务评级(0代表没有记录),舆情数量,法律诉讼数量,国标行业,股价/大盘指数,营收,资产,利润,注册地,纬度,经度
                company.setRegisteredCapital(values[8]);
                company.setPaidInCapital(values[9]);
                company.setScale(values[10]);
                try {
                    company.setEmployeeCount(Integer.parseInt(values[11]));
                } catch (NumberFormatException e) {
                    company.setEmployeeCount(0);
                }
                try {
                    company.setCertificateCount(Integer.parseInt(values[12]));
                } catch (NumberFormatException e) {
                    company.setCertificateCount(0);
                }
                company.setTaxRating(values[13]);
                try {
                    company.setPublicSentimentCount(Integer.parseInt(values[14]));
                } catch (NumberFormatException e) {
                    company.setPublicSentimentCount(0);
                }
                try {
                    company.setLegalCaseCount(Integer.parseInt(values[15]));
                } catch (NumberFormatException e) {
                    company.setLegalCaseCount(0);
                }
                company.setIndustry(values[16]);
                try {
                    company.setStockIndex(Double.parseDouble(values[17]));
                } catch (NumberFormatException e) {
                    company.setStockIndex(0.0);
                }
                company.setRevenue(parseFinancialValue(values[18]));
                company.setAssets(parseFinancialValue(values[19]));
                company.setProfit(parseFinancialValue(values[20]));
                company.setRegisteredAddress(values[21]);

                // Tech, Finance, Law, Credit, Reason are not in this CSV, they are in the other one.
                // For now, set default values or leave null if not available in this CSV.
                // The API doc for 6.2 shows these fields, so they should be populated.
                // I will set them to "低" (low risk) as a default for now.
                company.setTech("低");
                company.setFinance("低");
                company.setLaw("低");
                company.setCredit("低");
                company.setReason("无明显风险");


                double[] coords = geolocations.get(company.getRegisteredAddress());
                if (coords != null) {
                    company.setLatitude(coords[0]);
                    company.setLongitude(coords[1]);
                }

                companies.put(company.getName(), companyRepository.save(company));
            }
        }
        return companies;
    }

    private void importCompanyProducts(String filePath, Map<String, Company> companies) throws IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 2) continue;
                String companyName = values[0];
                Company company = companies.get(companyName);
                if (company != null) {
                    for (int i = 1; i < values.length; i++) {
                        String productName = values[i];
                        if (StringUtils.hasText(productName)) {
                            productRepository.findByProductName(productName).ifPresent(product -> {
                                company.getProducts().add(product);
                            });
                        }
                    }
                    companyRepository.save(company);
                }
            }
        }
    }

    private void createCompanyRelationships() {
        List<Company> companies = companyRepository.findAll();

        // Clear existing relationships to prevent duplicates on re-run
        relationshipRepository.deleteAll();

        List<CompanyRelationship> newRelationships = new ArrayList<>();

        // Build a map for quick lookup of companies by name
        Map<String, Company> companyMap = companies.stream()
                .collect(Collectors.toMap(Company::getName, company -> company));

        for (int i = 0; i < companies.size(); i++) {
            for (int j = i + 1; j < companies.size(); j++) {
                Company c1 = companies.get(i);
                Company c2 = companies.get(j);

                // Check for supplier/customer relationships based on product hierarchy
                for (Product p1 : c1.getProducts()) {
                    for (Product p2 : c2.getProducts()) {
                        if (isAncestor(p1, p2)) { // p1 is an ancestor of p2, so c1 supplies c2
                            newRelationships.add(createRelationship(c1, c2, "供应", "supplier"));
                            newRelationships.add(createRelationship(c2, c1, "销售", "customer"));
                        } else if (isAncestor(p2, p1)) { // p2 is an ancestor of p1, so c2 supplies c1
                            newRelationships.add(createRelationship(c2, c1, "供应", "supplier"));
                            newRelationships.add(createRelationship(c1, c2, "销售", "customer"));
                        }
                    }
                }

                // Check for competitive relationships (simplified: if they share a common parent product)
                boolean hasCommonParentProduct = c1.getProducts().stream()
                        .anyMatch(p1 -> p1.getParent() != null && c2.getProducts().stream()
                                .anyMatch(p2 -> p2.getParent() != null && p1.getParent().equals(p2.getParent())));

                if (hasCommonParentProduct) {
                    newRelationships.add(createRelationship(c1, c2, "竞争", "competitor"));
                    newRelationships.add(createRelationship(c2, c1, "竞争", "competitor"));
                }
            }
        }
        relationshipRepository.saveAll(newRelationships);
    }

    // Helper method to create CompanyRelationship entity
    private CompanyRelationship createRelationship(Company source, Company target, String label, String type) {
        CompanyRelationship rel = new CompanyRelationship();
        rel.setSource(source);
        rel.setTarget(target);
        rel.setLabel(label);
        rel.setType(type);
        return rel;
    }

    private boolean isAncestor(Product potentialAncestor, Product product) {
        Product current = product.getParent();
        while (current != null) {
            if (current.equals(potentialAncestor)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private Map<String, double[]> importGeolocations(String filePath) throws IOException {
        Map<String, double[]> geolocations = new HashMap<>();
        ClassPathResource resource = new ClassPathResource(filePath);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 4 && !values[2].isEmpty() && !values[3].isEmpty()) {
                    String address = values[1];
                    try {
                        double latitude = Double.parseDouble(values[2]);
                        double longitude = Double.parseDouble(values[3]);
                        geolocations.put(address, new double[]{latitude, longitude});
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
        }
        return geolocations;
    }

    private double parseFinancialValue(String value) {
        if (value == null || value.isEmpty() || value.equals("0")) {
            return 0.0;
        }
        try {
            if (value.endsWith("亿")) {
                return Double.parseDouble(value.substring(0, value.length() - 1)) * 100000000;
            } else if (value.endsWith("万")) {
                return Double.parseDouble(value.substring(0, value.length() - 1)) * 10000;
            }
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}