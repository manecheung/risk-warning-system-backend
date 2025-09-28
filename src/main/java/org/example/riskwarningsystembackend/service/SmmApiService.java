package org.example.riskwarningsystembackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.example.riskwarningsystembackend.dto.smm.SmmAuthResponse;
import org.example.riskwarningsystembackend.dto.smm.SmmPriceDataResponse;
import org.example.riskwarningsystembackend.dto.smm.SmmQuotaListResponse;
import org.example.riskwarningsystembackend.entity.SmmIndicator;
import org.example.riskwarningsystembackend.entity.SmmPriceData;
import org.example.riskwarningsystembackend.repository.SmmIndicatorRepository;
import org.example.riskwarningsystembackend.repository.SmmPriceDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * SMM API服务类
 * 提供与上海有色金属网(SMM)API交互的功能，包括认证、数据同步等操作
 */
@Service
public class SmmApiService {

    private static final Logger log = LoggerFactory.getLogger(SmmApiService.class);
    private static final String SMM_SOURCE = "datapro";
    private static final String API_REQUEST_SOURCE = "API";
    private static final String FREQUENCY_DAILY = "日";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SmmIndicatorRepository indicatorRepository;
    private final SmmPriceDataRepository priceDataRepository;

    @Value("${smm.api.baseUrl}")
    private String baseUrl;

    @Value("${smm.api.username}")
    private String username;

    @Value("${smm.api.password}")
    private String password;

    @Value("${smm.api.sync.period}")
    private int syncPeriod;

    private String token;
    private LocalDateTime tokenExpirationTime;

    public SmmApiService(ObjectMapper objectMapper, SmmIndicatorRepository indicatorRepository, SmmPriceDataRepository priceDataRepository) {
        this.objectMapper = objectMapper;
        this.indicatorRepository = indicatorRepository;
        this.priceDataRepository = priceDataRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * 初始化数据
     * 在服务启动时执行，获取API令牌并同步指标列表和最近的数据
     */
    @PostConstruct
    @Transactional
    public void initializeData() {
        log.info("执行SMM服务启动初始化任务...");
        if (getValidToken() == null) {
            throw new IllegalStateException("SMM服务启动失败：无法在启动时获取有效的API Token，请检查相关配置。");
        }
        syncIndicators();
        syncRecentData();
        log.info("SMM服务启动初始化任务完成。");
    }

    /**
     * 同步指标列表
     * 从SMM API获取所有指标信息并保存到数据库中
     */
    @Transactional
    public void syncIndicators() {
        log.info("开始同步SMM指标列表...");
        int currentPage = 1;
        int totalPages = 1;
        boolean hasError = false;

        while (currentPage <= totalPages && !hasError) {
            SmmQuotaListResponse response = fetchIndicatorPage(currentPage);
            if (response != null && response.getCode() == 0 && response.getData() != null) {
                totalPages = response.getData().getPageCount();
                List<SmmQuotaListResponse.QuotaInfo> quotaInfos = response.getData().getData();
                for (SmmQuotaListResponse.QuotaInfo info : quotaInfos) {
                    SmmIndicator indicator = indicatorRepository.findByQuotaId(info.getQuotaId()).orElse(new SmmIndicator());
                    indicator.setQuotaId(info.getQuotaId());
                    indicator.setQuotaName(info.getQuotaName());
                    indicator.setUnit(info.getUnit());
                    indicator.setFrequency(info.getFrequency());
                    indicator.setSource(info.getSource());
                    try {
                        if (StringUtils.hasText(info.getDataStart())) indicator.setDataStart(LocalDate.parse(info.getDataStart()));
                        if (StringUtils.hasText(info.getDataEnd())) indicator.setDataEnd(LocalDate.parse(info.getDataEnd()));
                    } catch (Exception e) {
                        log.warn("解析指标日期失败: quotaId={}, startDate={}, endDate={}", info.getQuotaId(), info.getDataStart(), info.getDataEnd());
                    }
                    indicatorRepository.save(indicator);
                }
                log.info("已同步第 {}/{} 页指标", currentPage, totalPages);
                currentPage++;
            } else {
                log.error("获取SMM指标列表失败，页码: {}，同步任务中止。", currentPage);
                hasError = true;
            }
        }
        log.info("SMM指标列表同步完成。{}", hasError ? "但过程存在错误" : "");
    }

    /**
     * 同步指定指标的价格数据
     * 根据指标ID和日期范围从SMM API获取价格数据并保存到数据库中
     *
     * @param quotaId 指标配额ID
     * @param startDate 起始日期
     * @param endDate 结束日期
     */
    @Transactional
    public void syncPriceDataForIndicator(String quotaId, LocalDate startDate, LocalDate endDate) {
        log.debug("为指标 {} 同步从 {} 到 {} 的价格数据", quotaId, startDate, endDate);
        indicatorRepository.findByQuotaId(quotaId).ifPresentOrElse(indicator -> {
            SmmPriceDataResponse response = fetchPriceData(quotaId, startDate, endDate);
            if (response != null && response.getCode() == 0) {
                if (response.getData() != null && !response.getData().isEmpty()) {
                    int count = 0;
                    for (SmmPriceDataResponse.QuotaData quotaData : response.getData()) {
                        if (quotaData.getQuotaId().equals(quotaId) && quotaData.getData() != null) {
                            for (SmmPriceDataResponse.PricePoint point : quotaData.getData()) {
                                if (point.getValue() == null || point.getValue().trim().isEmpty()) {
                                    continue; // Skip if value is null or empty
                                }
                                try {
                                    BigDecimal priceValue = new BigDecimal(point.getValue());
                                    if (!priceDataRepository.existsByIndicatorIdAndPriceDate(indicator.getId(), point.getDate())) {
                                        SmmPriceData priceData = new SmmPriceData();
                                        priceData.setIndicator(indicator);
                                        priceData.setPriceDate(point.getDate());
                                        priceData.setValue(priceValue);
                                        priceDataRepository.save(priceData);
                                        count++;
                                    }
                                } catch (NumberFormatException e) {
                                    log.warn("接收到非数字的价格值: [{}], 日期: [{}], 指标ID: [{}]. 已跳过此数据点。",
                                            point.getValue(), point.getDate(), quotaId);
                                }
                            }
                        }
                    }
                    if (count > 0) {
                        log.info("指标 {} 本次同步新增 {} 条价格数据", quotaId, count);
                    } else {
                        log.info("指标 {} 在 {} 到 {} 范围内没有需要更新的数据（可能数据已存在或API未返回有效值）", quotaId, startDate, endDate);
                    }
                } else {
                    log.info("SMM API成功响应，但指标 {} 在 {} 到 {} 范围内没有返回任何价格数据", quotaId, startDate, endDate);
                }
            } else {
                String errorMsg = response != null ? response.getMsg() : "响应为空或认证失败";
                log.error("获取指标 {} 的价格数据失败，错误信息: {}", quotaId, errorMsg);
            }
        }, () -> log.warn("数据库中不存在指标ID: {}, 跳过价格同步", quotaId));
    }

    /**
     * 同步最近数据
     * 同步所有日度指标最近7天的价格数据
     */
    @Transactional
    public void syncRecentData() {
        log.info("开始同步最近{}天的日度SMM价格数据...", syncPeriod);
        List<SmmIndicator> dailyIndicators = indicatorRepository.findByFrequency(FREQUENCY_DAILY);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(syncPeriod);
        for (SmmIndicator indicator : dailyIndicators) {
            try {
                syncPriceDataForIndicator(indicator.getQuotaId(), startDate, endDate);
            } catch (Exception e) {
                log.error("同步指标 {} 最近{}天数据时出错: {}", indicator.getQuotaId(), syncPeriod, e.getMessage(), e);
            }
        }
        log.info("最近{}天的日度SMM价格数据同步完成，共处理 {} 个日度指标。", syncPeriod, dailyIndicators.size());
    }

    /**
     * 定时同步每日数据
     * 每天凌晨3点执行，同步所有日度指标前一天的价格数据
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void scheduledSyncDailyData() {
        log.info("开始执行每日SMM数据同步定时任务...");
        List<SmmIndicator> dailyIndicators = indicatorRepository.findByFrequency(FREQUENCY_DAILY);
        LocalDate yesterday = LocalDate.now().minusDays(1);
        for (SmmIndicator indicator : dailyIndicators) {
            try {
                syncPriceDataForIndicator(indicator.getQuotaId(), yesterday, yesterday);
            } catch (Exception e) {
                log.error("定时同步指标 {} 数据时出错: {}", indicator.getQuotaId(), e.getMessage(), e);
            }
        }
        log.info("每日SMM数据同步定时任务执行完毕，共处理 {} 个日度指标。", dailyIndicators.size());
    }

    /**
     * 检查令牌是否过期
     *
     * @return boolean 如果令牌过期返回true，否则返回false
     */
    private boolean isTokenExpired() {
        return tokenExpirationTime == null || tokenExpirationTime.isBefore(LocalDateTime.now());
    }

    /**
     * 获取有效令牌
     * 如果当前令牌为空或已过期，则重新登录获取新令牌
     *
     * @return String 有效的令牌字符串
     */
    private String getValidToken() {
        if (token == null || isTokenExpired()) {
            login();
        }
        return token;
    }

    /**
     * 登录获取令牌
     * 调用SMM API的认证接口获取访问令牌
     */
    private void login() {
        String url = baseUrl + "/dapi/user/auth";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("user_name", username);
        body.add("password", password);
        body.add("source", SMM_SOURCE);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            SmmAuthResponse authResponse = objectMapper.readValue(response.getBody(), SmmAuthResponse.class);
            if (authResponse != null) {
                if (authResponse.getCode() == 0 && authResponse.getData() != null && authResponse.getData().getToken() != null) {
                    this.token = authResponse.getData().getToken();
                    this.tokenExpirationTime = LocalDateTime.now().plusDays(7);
                    log.info("成功获取SMM API Token");
                } else {
                    log.error("SMM API登录失败: {}", authResponse.getMsg());
                    this.token = null;
                }
            } else {
                log.error("SMM API登录失败: 响应体为空");
                this.token = null;
            }
        } catch (Exception e) {
            log.error("调用SMM API登录接口时发生严重错误", e);
            this.token = null;
        }
    }

    /**
     * 获取指标列表的指定页面
     * 调用SMM API获取指标列表的指定页面数据
     *
     * @param page 页码
     * @return SmmQuotaListResponse 指标列表响应对象
     */
    private SmmQuotaListResponse fetchIndicatorPage(int page) {
        String url = baseUrl + "/dapi/quota/quota_list?token=" + getValidToken() + "&page=" + page + "&page_size=500";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return objectMapper.readValue(response.getBody(), SmmQuotaListResponse.class);
        } catch (Exception e) {
            log.error("调用SMM API获取指标列表时发生错误 (Page: {})", page, e);
            return null;
        }
    }

    /**
     * 获取价格数据
     * 调用SMM API获取指定指标和日期范围的价格数据
     *
     * @param quotaId 指标配额ID
     * @param startDate 起始日期
     * @param endDate 结束日期
     * @return SmmPriceDataResponse 价格数据响应对象
     */
    private SmmPriceDataResponse fetchPriceData(String quotaId, LocalDate startDate, LocalDate endDate) {
        String url = baseUrl + "/dapi/quota/data_origin";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("token", getValidToken());
        body.add("request_source", API_REQUEST_SOURCE);
        body.add("quota_ids", quotaId);
        body.add("start_date", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        body.add("end_date", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        body.add("sort_type", "1");
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            return objectMapper.readValue(response.getBody(), SmmPriceDataResponse.class);
        } catch (Exception e) {
            log.error("调用SMM API获取价格数据时发生错误 (QuotaID: {})", quotaId, e);
            return null;
        }
    }
}
