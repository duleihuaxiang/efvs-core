package cn.iocoder.yudao.module.reliability.controller.admin.reliabilitydata.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReliabilityDataPartCascadeFillRespVO {

    private String partCode;

    private String partNumber;

    private String deliveryTime;

    private String installationDate;
}

