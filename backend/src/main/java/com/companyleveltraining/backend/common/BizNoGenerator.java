package com.companyleveltraining.backend.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

/**
 * 业务编号生成器。用于生成各表的 *_no 业务编号，例如预约编号 RA20260614xxxx、通知编号 N...、备份编号 BKP... 等。
 * 规则：前缀 + yyyyMMddHHmmssSSS（精确到毫秒）+ 6 位随机数，足够课程项目使用且基本不重复。
 */
@Component
public class BizNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public String generate(String prefix) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int random = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return prefix + timestamp + random;
    }
}
