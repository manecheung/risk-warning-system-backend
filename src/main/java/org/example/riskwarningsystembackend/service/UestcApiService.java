package org.example.riskwarningsystembackend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.common.ResultCode;
import org.example.riskwarningsystembackend.dto.uestc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 电子科技大学相关API的服务类
 */
@Service
public class UestcApiService {

    private static final Logger log = LoggerFactory.getLogger(UestcApiService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Autowired
    public UestcApiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Value("${uestc.api.baseUrl}")
    private String baseUrl;

    @Value("${uestc.api.username}")
    private String username;

    @Value("${uestc.api.password}")
    private String password;

    private String token;

    // 添加token过期时间字段
    private Date tokenExpirationTime;

    /**
     * 获取有效的认证Token
     *
     * @return 认证Token
     */
    private String getValidToken() {
        // 检查Token是否为空或已过期
        if (token == null || token.isEmpty() || isTokenExpired()) {
            login();
        }
        return token;
    }

    /**
     * 检查Token是否已过期
     *
     * @return 如果Token已过期返回true，否则返回false
     */
    private boolean isTokenExpired() {
        if (tokenExpirationTime == null) {
            return true;
        }
        return tokenExpirationTime.before(new Date());
    }


    /**
     * 登录第三方API以获取Token
     */
    private void login() {
        String url = baseUrl + "/user/login";
        Map<String, String> body = new ConcurrentHashMap<>();
        body.put("username", username);
        body.put("password", password);
        try {
            String response = restTemplate.postForObject(url, body, String.class);
            JsonNode root = objectMapper.readTree(response);
            if (root.path("code").asInt() == 200) {
                this.token = root.path("data").path("token").asText();
                // 解析token过期时间
                parseTokenExpiration();
                log.info("成功获取电子科大API Token");
            } else {
                log.error("电子科大API登录失败: {}", root.path("message").asText());
            }
        } catch (IOException | RestClientException e) {
            log.error("调用电子科大API登录接口时发生错误", e);
        }
    }

    /**
     * 解析Token中的过期时间
     */
    private void parseTokenExpiration() {
        try {
            // 解码JWT Token获取过期时间
            String[] chunks = token.split("\\.");
            if (chunks.length >= 2) {
                // 解码payload部分
                String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
                JsonNode payloadNode = objectMapper.readTree(payload);
                long exp = payloadNode.path("exp").asLong(0);
                if (exp > 0) {
                    this.tokenExpirationTime = new Date(exp * 1000);
                }
            }
        } catch (Exception e) {
            log.warn("解析Token过期时间失败，将使用默认过期策略", e);
            // 如果解析失败，设置一个较短的过期时间(例如5分钟)
            this.tokenExpirationTime = new Date(System.currentTimeMillis() + 5 * 60 * 1000);
        }
    }

    /**
     * 使用认证Token发起GET请求
     *
     * @param path          请求路径
     * @param typeReference 返回类型
     * @param <T>           泛型
     * @return RestResult封装的响应
     */
    private <T> RestResult<T> getWithAuth(String path, TypeReference<T> typeReference) {
        String url = baseUrl + path;
        HttpHeaders headers = new HttpHeaders();
        String token = getValidToken();
        if (token == null || token.isEmpty()) {
            return RestResult.failure(ResultCode.UNAUTHORIZED, "无法获取第三方API的认证Token");
        }
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.path("code").asInt() == 200) {
                T data = objectMapper.convertValue(root.path("data"), typeReference);
                return RestResult.success(data);
            }
            return RestResult.failure(ResultCode.FAILURE, root.path("message").asText());
        } catch (IOException | RestClientException e) {
            log.error("调用电子科大API失败: {} - {}", path, e.getMessage());
            return RestResult.failure(ResultCode.FAILURE, "从路径 " + path + " 获取数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取产业链列表
     *
     * @return 产业链列表
     */
    public RestResult<List<Map<String, Object>>> getIndustryChainList() {
        return getWithAuth("/industry-chain/list", new TypeReference<>() {
        });
    }

    /**
     * 获取风险图谱
     *
     * @param industryChainId 产业链ID
     * @return 风险图谱数据
     */
    public RestResult<UestcGraphDTO> getRiskGraph(Integer industryChainId) {
        RestResult<UestcGraphDTO> result = getWithAuth("/risk-status/graph/" + industryChainId, new TypeReference<>() {
        });
        if (result.getCode() == ResultCode.SUCCESS.getCode() && result.getData() != null) {
            UestcGraphDTO graph = result.getData();
            // 数据缩减逻辑: 如果节点数超过100，则进行截断处理
            if (graph.getNodes().size() > 200) {
                log.info("数据量超过200，进行缩减处理。");
                List<UestcNodeDTO> nodes = graph.getNodes().stream().limit(200).collect(Collectors.toList());
                Set<String> nodeIds = nodes.stream().map(UestcNodeDTO::getId).collect(Collectors.toSet());
                List<UestcLinkDTO> links = graph.getLinks().stream()
                        .filter(link -> nodeIds.contains(link.getSource()) && nodeIds.contains(link.getTarget()))
                        .collect(Collectors.toList());
                graph.setNodes(nodes);
                graph.setLinks(links);
            }
        }
        return result;
    }


    /**
     * 获取风险状态可用时段
     *
     * @param industryChainId 产业链ID
     * @return 可用时段列表
     */
    public RestResult<List<String>> getRiskStatusPeriods(Integer industryChainId) {
        String path = "/risk-status/periods/" + industryChainId;
        return getWithAuth(path, new TypeReference<>() {
        });
    }

    /**
     * 获取风险状态概览
     *
     * @param industryChainId 产业链ID
     * @param dataPeriod      数据时段
     * @return 风险状态概览
     */
    public RestResult<RiskStatusOverviewDTO> getRiskStatusOverview(Integer industryChainId, String dataPeriod) {
        String path = "/risk-status/overview/" + industryChainId + "?dataPeriod=" + dataPeriod;
        return getWithAuth(path, new TypeReference<>() {
        });
    }

    /**
     * 获取已训练模型列表（分页）
     *
     * @param current 当前页
     * @param size    每页大小
     * @return 分页的模型数据
     */
    public RestResult<UestcPageDTO<TrainedModelDTO>> getTrainedModels(int current, int size) {
        String path = "/trained-models?current=" + current + "&size=" + size;
        return getWithAuth(path, new TypeReference<>() {
        });
    }

    /**
     * 获取模型训练图表
     *
     * @param id 模型ID
     * @return 训练图表列表
     */
    public RestResult<List<TrainingPlotDTO>> getTrainingPlots(Integer id) {
        String path = "/trained-models/" + id + "/training-plots";
        RestResult<List<TrainingPlotDTO>> result = getWithAuth(path, new TypeReference<>() {
        });
        if (result.getCode() == ResultCode.SUCCESS.getCode() && result.getData() != null) {
            result.getData().forEach(plot -> {
                if (plot.getUrl() != null && !plot.getUrl().isEmpty()) {
                    plot.setUrl("/uestc/trained-models/" + id + "/training-plots/" + plot.getFilename());
                } else {
                    plot.setUrl("/uestc/trained-models/" + id + "/training-plots/default-image.png");
                }
            });
        }
        return result;
    }

    /**
     * 获取训练图表图片
     *
     * @param id       模型ID
     * @param filename 图片文件名
     * @return 图片字节数据的响应实体
     */
    public ResponseEntity<byte[]> getTrainingPlotImage(Integer id, String filename) {
        try {
            // 处理默认图片请求
            if ("default-image.png".equals(filename)) {
                return getDefaultImageResponse();
            }

            try {
                String path = "/trained-models/" + id + "/training-plots/" + filename;
                String url = baseUrl + path;

                // 获取图片字节数据
                ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
                // 获取第三方API的图片，判断是否成功
                if (!response.getStatusCode().is2xxSuccessful()) {
                    log.warn("从URL获取图片失败，HTTP状态码: {}", response.getStatusCode());
                    return getDefaultImageResponse();
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                return new ResponseEntity<>(response.getBody(), headers, HttpStatus.OK);
            } catch (RestClientException e) {
                log.error("从URL获取图片失败: {}", "/trained-models/" + id + "/training-plots/" + filename, e);
                // 当获取图片失败时，返回默认图片
                return getDefaultImageResponse();
            }
        } catch (Exception e) {
            // 捕获所有未预期的异常，确保返回默认图片
            log.error("处理图片请求时发生未预期的错误: ", e);
        }

        // 当找不到匹配的文件时，返回默认图片
        return getDefaultImageResponse();
    }


    /**
     * 获取默认图片响应
     *
     * @return 默认图片的ResponseEntity
     */
    private ResponseEntity<byte[]> getDefaultImageResponse() {
        try {
            ClassPathResource imgFile = new ClassPathResource("images/模型详情图片/测试曲线.png");
            byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("读取默认图片失败", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}