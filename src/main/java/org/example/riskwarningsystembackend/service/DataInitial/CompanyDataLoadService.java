package org.example.riskwarningsystembackend.service.DataInitial;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.entity.ProductInfo;
import org.example.riskwarningsystembackend.repository.company.CompanyInfoRepository;
import org.example.riskwarningsystembackend.repository.product.ProductInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务类，用于从CSV文件加载公司和产品数据到数据库中。
 */
@Service
public class CompanyDataLoadService {

    private final CompanyInfoRepository companyInfoRepository;
    private final ProductInfoRepository productInfoRepository;

    /**
     * 构造函数，注入所需的Repository依赖。
     *
     * @param companyInfoRepository 公司信息数据访问接口
     * @param productInfoRepository 产品信息数据访问接口
     */
    public CompanyDataLoadService(CompanyInfoRepository companyInfoRepository, ProductInfoRepository productInfoRepository) {
        this.companyInfoRepository = companyInfoRepository;
        this.productInfoRepository = productInfoRepository;
    }

    /**
     * 从指定的CSV文件中读取公司基础信息，并保存到数据库中。
     * 使用REQUIRES_NEW事务传播机制确保独立提交。
     *
     * @throws IOException            当读取CSV文件时发生IO异常
     * @throws CsvValidationException 当CSV格式不合法时抛出
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void loadCompanyData() throws IOException, CsvValidationException {
        List<CompanyInfo> companies = new ArrayList<>();
        try (var inputStream = new ClassPathResource("data/风电行业公司基础信息v1.6.csv").getInputStream(); var reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            reader.readNext(); // 跳过标题行
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length < 24) continue;
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
                companies.add(company);
            }
        }
        companyInfoRepository.saveAll(companies);
    }

    /**
     * 从指定的CSV文件中读取产品基本信息，并保存到数据库中。
     * 使用REQUIRES_NEW事务传播机制确保独立提交。
     *
     * @throws IOException            当读取CSV文件时发生IO异常
     * @throws CsvValidationException 当CSV格式不合法时抛出
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void loadProductData() throws IOException, CsvValidationException {
        List<ProductInfo> products = new ArrayList<>();
        try (var inputStream = new ClassPathResource("data/风电产品基本信息表v1.6.csv").getInputStream(); var reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            reader.readNext(); // 跳过标题行
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length < 5) continue;
                ProductInfo product = new ProductInfo();
                product.setLevel1(line[0]);
                product.setLevel2(line[1]);
                product.setLevel3(line[2]);
                product.setLevel4(line[3]);
                product.setLevel5(line[4]);
                products.add(product);
            }
        }
        productInfoRepository.saveAll(products);
    }

    /**
     * 安全地将字符串解析为整数。如果输入为空或解析失败，则返回null。
     *
     * @param str 待解析的字符串
     * @return 解析后的整数值，若无法解析则返回null
     */
    private Integer safeParseInt(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 安全地将字符串解析为双精度浮点数。如果输入为空、为"#N/A"或解析失败，则返回null。
     *
     * @param str 待解析的字符串
     * @return 解析后的双精度浮点数值，若无法解析则返回null
     */
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

    /**
     * 检查数据库中的公司数据是否为空。
     *
     * @return 如果公司表中没有记录则返回true，否则返回false
     */
    public boolean isCompanyDataEmpty() {
        // 实现检查公司数据是否为空的逻辑
        // 例如：查询数据库中公司表的记录数
        return companyInfoRepository.count() == 0;
    }

    /**
     * 检查数据库中的产品数据是否为空。
     *
     * @return 如果产品表中没有记录则返回true，否则返回false
     */
    public boolean isProductDataEmpty() {
        // 实现检查产品数据是否为空的逻辑
        // 例如：查询数据库中产品表的记录数
        return productInfoRepository.count() == 0;
    }
}
