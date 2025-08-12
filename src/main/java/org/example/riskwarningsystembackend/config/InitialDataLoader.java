package org.example.riskwarningsystembackend.config;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.entity.ProductInfo;
import org.example.riskwarningsystembackend.repository.CompanyInfoRepository;
import org.example.riskwarningsystembackend.repository.ProductInfoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
@Order(1)
public class InitialDataLoader implements CommandLineRunner {

    private final CompanyInfoRepository companyInfoRepository;
    private final ProductInfoRepository productInfoRepository;

    public InitialDataLoader(CompanyInfoRepository companyInfoRepository, ProductInfoRepository productInfoRepository) {
        this.companyInfoRepository = companyInfoRepository;
        this.productInfoRepository = productInfoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (companyInfoRepository.count() == 0) {
            loadCompanyData();
        }
        if (productInfoRepository.count() == 0) {
            loadProductData();
        }
    }

    private void loadCompanyData() throws IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(new InputStreamReader(
                new ClassPathResource("data/风电行业公司基础信息v1.6.csv").getInputStream(), StandardCharsets.UTF_8))) {
            reader.readNext(); // 跳过标题行
            String[] line;
            while ((line = reader.readNext()) != null) {
                CompanyInfo company = new CompanyInfo();
                company.setName(line[0]);
                company.setMajorProduct1(line[1]);
                company.setMajorProduct2(line[2]);
                company.setCompanyType(line[3]);
                company.setIsDiversified(line[4]);
                company.setIsWellKnown(line[5]);
                company.setMainProductsSummary(line[6]);
                company.setRelatedProducts(line[7]);
                company.setRegisteredCapital(line[8]);
                company.setPaidInCapital(line[9]);
                company.setCompanySize(line[10]);
                company.setEmployeeCount(line[11]);
                company.setQualificationCertificateCount(safeParseInt(line[12]));
                company.setTaxRating(line[13]);
                company.setPublicOpinionCount(safeParseInt(line[14]));
                company.setLegalDisputeCount(safeParseInt(line[15]));
                company.setIndustry(line[16]);
                company.setStockPriceIndex(line[17]);
                company.setRevenue(line[18]);
                company.setAssets(line[19]);
                company.setProfit(line[20]);
                company.setRegisteredAddress(line[21]);
                company.setLatitude(safeParseDouble(line[22]));
                company.setLongitude(safeParseDouble(line[23]));
                companyInfoRepository.save(company);
            }
        }
    }

    private void loadProductData() throws IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(new InputStreamReader(
                new ClassPathResource("data/风电产品基本信息表v1.6.csv").getInputStream(), StandardCharsets.UTF_8))) {
            reader.readNext(); // 跳过标题行
            String[] line;
            while ((line = reader.readNext()) != null) {
                ProductInfo product = new ProductInfo();
                product.setLevel1(line[0]);
                product.setLevel2(line[1]);
                product.setLevel3(line[2]);
                product.setLevel4(line[3]);
                product.setLevel5(line[4]);
                productInfoRepository.save(product);
            }
        }
    }

    private Integer safeParseInt(String str) {
        if (str == null || str.trim().isEmpty() || !str.trim().matches("\\d+")) {
            return null;
        }
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double safeParseDouble(String str) {
        if (str == null || str.trim().isEmpty() || str.trim().equalsIgnoreCase("#N/A")) {
            return null;
        }
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
