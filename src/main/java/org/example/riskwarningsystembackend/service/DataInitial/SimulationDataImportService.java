package org.example.riskwarningsystembackend.service.DataInitial;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.riskwarningsystembackend.entity.CompanySimulation.CompanySimulationData;
import org.example.riskwarningsystembackend.entity.CompanySimulation.KRI;
import org.example.riskwarningsystembackend.entity.Simulation;
import org.example.riskwarningsystembackend.repository.SimulationCompanyDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 模拟数据导入服务类
 * 负责处理风险蔓延模拟数据的导入功能
 */
@Slf4j
@Service
public class SimulationDataImportService {

    private final ObjectMapper objectMapper;
    private final SimulationCompanyDataRepository simulationCompanyDataRepository;

    public SimulationDataImportService(ObjectMapper objectMapper, SimulationCompanyDataRepository simulationCompanyDataRepository) {
        this.objectMapper = objectMapper;
        this.simulationCompanyDataRepository = simulationCompanyDataRepository;
    }

    /**
     * 导入与特定模拟场景关联的模拟数据。
     * @param inputStream 包含JSON格式模拟数据的输入流
     * @param simulation  要关联的模拟场景实体
     * @throws IOException 当读取或解析数据失败时抛出
     */
    @Transactional
    public void importSimulationData(InputStream inputStream, Simulation simulation) throws IOException {
        // 1. 读取风险蔓延模拟初始化数据到一个Map中
        Map<String, List<CompanySimulationData>> simulationDataMap = objectMapper.readValue(
            inputStream,
            new com.fasterxml.jackson.core.type.TypeReference<>() {}
        );

        // 2. 将所有时间点的数据聚合到一个总列表中
        List<CompanySimulationData> allSimulationData = new ArrayList<>();
        for (List<CompanySimulationData> companyDataList : simulationDataMap.values()) {
            if (companyDataList != null && !companyDataList.isEmpty()) {
                // 关键步骤：将每条数据与父Simulation对象关联
                companyDataList.forEach(data -> data.setSimulation(simulation));
                allSimulationData.addAll(companyDataList);
            }
        }

        // 3. 对聚合后的全量数据进行预处理
        if (!allSimulationData.isEmpty()) {
            log.info("开始为模拟场景ID {} 处理 {} 条模拟数据...", simulation.getId(), allSimulationData.size());
            allSimulationData.forEach(this::prepareKriData);

            // 4. 一次性将所有数据存入数据库
            simulationCompanyDataRepository.saveAll(allSimulationData);
            log.info("模拟数据导入成功，共 {} 条记录，关联到模拟场景ID {}", allSimulationData.size(), simulation.getId());
        } else {
            log.warn("未从输入流中加载到任何模拟数据。");
        }
    }

    private void prepareKriData(CompanySimulationData data) {
        KRI kri = data.getKris();
        if (kri == null) {
            return;
        }
        if (kri.getKc1() != null) kri.setKc1W(kri.getKc1().getW());
        if (kri.getKc2() != null) kri.setKc2W(kri.getKc2().getW());
        if (kri.getKc3() != null) kri.setKc3W(kri.getKc3().getW());
        if (kri.getKc4() != null) kri.setKc4W(kri.getKc4().getW());
        if (kri.getKc5() != null) kri.setKc5W(kri.getKc5().getW());
        if (kri.getKc6() != null) kri.setKc6W(kri.getKc6().getW());
        if (kri.getKc7() != null) kri.setKc7W(kri.getKc7().getW());
        if (kri.getKc8() != null) kri.setKc8W(kri.getKc8().getW());
        if (kri.getKc9() != null) kri.setKc9W(kri.getKc9().getW());
        if (kri.getKc10() != null) kri.setKc10W(kri.getKc10().getW());
        if (kri.getKc11() != null) kri.setKc11W(kri.getKc11().getW());
        if (kri.getKc12() != null) kri.setKc12W(kri.getKc12().getW());
        if (kri.getKc13() != null) kri.setKc13W(kri.getKc13().getW());
    }
}

