package org.example.riskwarningsystembackend.dto.uestc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainedModelDTO {
    private Integer id;
    private String trainedModelName;
    private Integer originalModelId;
    private String originalModelName;
    private Integer trainingTaskId;
    private String modelSavePath;
    private String logFileName;
    private Integer trainingEpochs;
    private Double learningRate;
    private Integer bestEpoch;
    private Integer modelSize;
    private Integer trainingDuration;
    private String datasetName;
    private String optimizerName;
    private String status;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updateTime;
}
