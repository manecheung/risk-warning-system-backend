package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.dto.PriceDataDto;
import org.example.riskwarningsystembackend.repository.SmmPriceDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import smile.timeseries.ARMA;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 价格预测服务
 * 使用ARIMA模型对时间序列价格数据进行预测。
 * 优化：采用对数变换来处理价格序列，以保证预测结果为正，并稳定方差。
 */
@Service
public class PricePredictionService {

    private static final Logger log = LoggerFactory.getLogger(PricePredictionService.class);
    private final SmmPriceDataRepository priceDataRepository;

    // ARIMA 模型参数，建议通过配置（如 @Value）或外部服务动态确定
    private final int p;
    private final int d;
    private final int q;

    public PricePredictionService(SmmPriceDataRepository priceDataRepository) {
        this.priceDataRepository = priceDataRepository;
        // 默认参数，应根据数据分析进行优化
        this.p = 2;
        this.d = 1;
        this.q = 1;
    }

    private static double calculateStandardDeviation(List<PriceDataDto> data) {
        if (data == null || data.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        double squareSum = 0.0;
        int count = 0;

        for (PriceDataDto dto : data) {
            double value = dto.getValue().doubleValue();
            sum += value;
            squareSum += value * value;
            count++;
        }

        if (count < 2) { // 至少需要两个点才能计算标准差
            return 0.0;
        }

        double mean = sum / count;
        double variance = (squareSum / count) - (mean * mean);
        // 确保方差非负，避免浮点数误差导致负值
        return Math.sqrt(Math.max(0, variance));
    }

    /**
     * 使用ARIMA模型预测给定指标的未来价格。
     *
     * @param indicatorId  需要预测的指标ID
     * @param forecastDays 需要预测的未来天数
     * @return 包含预测日期和价格的DTO列表
     */
    public List<PriceDataDto> predictFuturePrices(Long indicatorId, int forecastDays) {
        log.info("开始为指标ID {} 预测未来 {} 天的价格...", indicatorId, forecastDays);

        // 1. 定义ARIMA模型参数
        // int p = 2, d = 1, q = 1; // 已提升为成员变量
        log.info("ARIMA模型参数: p={}, d={}, q={}", p, d, q);

        // 2. 获取历史数据
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(3);
        List<PriceDataDto> historicalData = priceDataRepository.findPricesByIndicatorIdAndDateRange(indicatorId, startDate, endDate);

        // 3. 检查数据量是否足够
        int minDataPoints = p + q + d + 10; // 增加一个更安全的缓冲值
        if (historicalData == null || historicalData.size() < minDataPoints) {
            log.warn("指标ID {} 的历史数据不足(少于{}个点)，无法进行ARIMA预测。", indicatorId, minDataPoints);
            return List.of();
        }

        // 记录历史数据统计信息
        BigDecimal historicalMin = historicalData.stream().map(PriceDataDto::getValue).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal historicalMax = historicalData.stream().map(PriceDataDto::getValue).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal historicalAvg = historicalData.stream().map(PriceDataDto::getValue).reduce(BigDecimal.ZERO, BigDecimal::add).divide(BigDecimal.valueOf(historicalData.size()), RoundingMode.HALF_UP);
        double historicalStdDevValue = calculateStandardDeviation(historicalData);
        log.info("历史数据统计: Min={}, Max={}, Avg={}, StdDev={}", historicalMin, historicalMax, historicalAvg, historicalStdDevValue);

        // 4. 应用对数变换
        // 过滤掉非正数价格，并对价格取自然对数
        double[] logPrices = historicalData.stream()
                .mapToDouble(point -> point.getValue().doubleValue())
                .filter(price -> price > 0)
                .map(Math::log)
                .toArray();

        // 再次检查对数变换后的数据量是否足够，因为过滤操作可能减少数据点
        if (logPrices.length < minDataPoints) {
            log.warn("对数变换和过滤后，指标ID {} 的有效历史数据不足(少于{}个点)，无法进行预测。", indicatorId, minDataPoints);
            return List.of();
        }
        log.debug("对数变换后的序列长度: {}", logPrices.length);

        // 5. 对对数序列进行差分
        double[] differencedData = difference(logPrices, d);
        log.debug("{}阶差分后的序列长度: {}", d, differencedData.length);

        // 6. 拟合ARMA模型
        ARMA model = ARMA.fit(differencedData, p, q);
        log.info("ARMA(p={}, q={})模型已在对数差分序列上拟合成功。", p, q);

        // 7. 预测对数差分序列的未来值
        double[] differencedForecast = model.forecast(forecastDays);

        // 8. 逆差分，还原对数序列的预测值
        double[] logForecast = invertDifference(logPrices, differencedForecast, d);

        // 9. 指数化，将对数预测值还原为原始价格尺度
        double[] finalForecast = Arrays.stream(logForecast).map(Math::exp).toArray();
        log.info("已生成 {} 个最终预测点", finalForecast.length);
        // 记录前几个原始预测值，以便观察模型在应用边界之前的原始预测趋势
        IntStream.range(0, Math.min(finalForecast.length, 5))
                .forEach(idx -> log.info("原始预测值[{}]: {}", idx, finalForecast[idx]));

        // 10. 为最终预测值创建DTO对象
        LocalDate lastDate = historicalData.getLast().getDate();

        // 获取最后一个历史价格作为动态边界的中心
        BigDecimal lastHistoricalPrice = historicalData.getLast().getValue();
        log.info("最后一个历史价格: {}", lastHistoricalPrice);

        // 计算历史价格的标准差
        double historicalStdDev = calculateStandardDeviation(historicalData);
        final BigDecimal stdDevBigDecimal = BigDecimal.valueOf(historicalStdDev);

        // 定义标准差的倍数，用于设定动态边界
        // 例如，10.0表示在最后一个历史价格的±10个标准差范围内
        final BigDecimal STANDARD_DEVIATION_FACTOR = BigDecimal.valueOf(10.0);

        // 基于最后一个历史价格和标准差设定动态上限
        final BigDecimal maxAllowedForecastValue = lastHistoricalPrice.add(stdDevBigDecimal.multiply(STANDARD_DEVIATION_FACTOR));

        // 基于最后一个历史价格和标准差设定动态下限
        BigDecimal calculatedMinAllowedForecastValue = lastHistoricalPrice.subtract(stdDevBigDecimal.multiply(STANDARD_DEVIATION_FACTOR));
        // 确保下限至少为0.01，避免出现负数或0
        final BigDecimal minAllowedForecastValue = calculatedMinAllowedForecastValue.max(BigDecimal.valueOf(0.01));

        log.info("动态预测边界: 上限={}, 下限={}", maxAllowedForecastValue, minAllowedForecastValue);

        return IntStream.range(0, finalForecast.length)
                .mapToObj(i -> {
                    double forecast = finalForecast[i];
                    // 检查预测值是否为有限数，防止 Math.exp() 返回 Infinity 或 NaN
                    if (!Double.isFinite(forecast)) {
                        log.warn("预测模型为指标ID {} 在第 {} 天生成了一个非有限值 ({}). 将跳过此数据点。", indicatorId, i + 1, forecast);
                        return null; // 返回null，以便后续过滤
                    }
                    LocalDate forecastDate = lastDate.plusDays(i + 1);
                    BigDecimal forecastValue = BigDecimal.valueOf(forecast).setScale(4, RoundingMode.HALF_UP);

                    // 应用上限和下限，避免对 forecastValue 进行重新赋值
                    BigDecimal cappedForecastValue = forecastValue.min(maxAllowedForecastValue).max(minAllowedForecastValue);

                    // 记录警告日志（如果需要）
                    if (forecastValue.compareTo(maxAllowedForecastValue) > 0) {
                        log.warn("预测值 {} 超过了允许的上限 {}，已将其限制为上限值。", forecastValue, maxAllowedForecastValue);
                    }
                    if (forecastValue.compareTo(minAllowedForecastValue) < 0) {
                        log.warn("预测值 {} 低于允许的下限 {}，已将其限制为下限值。", forecastValue, minAllowedForecastValue);
                    }

                    return new PriceDataDto(forecastDate, cappedForecastValue);
                })
                .filter(java.util.Objects::nonNull) // 过滤掉所有无效的null条目
                .collect(Collectors.toList());
    }

    private static double[] difference(double[] data, int order) {
        if (order <= 0) return data;
        double[] diffData = new double[data.length - 1];
        for (int i = 0; i < diffData.length; i++) {
            diffData[i] = data[i + 1] - data[i];
        }
        return difference(diffData, order - 1);
    }

    private static double[] invertDifference(double[] history, double[] forecast, int order) {
        if (order <= 0) return forecast;
        double[] inverted = new double[forecast.length];
        // 使用历史数据的最后一个点作为逆差分的起点
        double lastValue = history[history.length - 1];
        inverted[0] = lastValue + forecast[0];
        for (int i = 1; i < forecast.length; i++) {
            inverted[i] = inverted[i - 1] + forecast[i];
        }
        // 如果有多阶差分，需要用还原后的序列作为新的历史序列进行递归还原，但此处我们简化处理，因为d通常为1或2
        // 对于d>1的正确递归还原会更复杂，但当前d=1的实现是正确的。
        return inverted;
    }
}