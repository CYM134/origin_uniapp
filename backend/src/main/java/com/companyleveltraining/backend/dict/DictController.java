package com.companyleveltraining.backend.dict;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公共字典接口：学院列表、学生预约申请类型。替换前端各页硬编码的 colleges / applicationTypes 数组。
 * 这些为相对固定的字典，直接在服务端集中维护即可。
 */
@RestController
@RequestMapping("/api")
public class DictController {

    private static final List<String> COLLEGES = List.of(
        "计算机学院", "数学科学学院", "物理学院", "化学学院", "生命科学学院",
        "经济与管理学院", "法学院", "教育科学学院", "心理学院", "外国语言文化学院",
        "文学院", "历史文化学院", "政治与公共管理学院", "马克思主义学院", "地理科学学院",
        "环境学院", "体育科学学院", "音乐学院", "美术学院", "新闻传播学院",
        "信息光电子科技学院", "软件学院"
    );

    private static final List<Map<String, String>> APPLICATION_TYPES = List.of(
        Map.of("id", "course", "name", "课程实验"),
        Map.of("id", "research", "name", "科研项目"),
        Map.of("id", "competition", "name", "竞赛培训"),
        Map.of("id", "activity", "name", "学术活动"),
        Map.of("id", "other", "name", "其他")
    );

    @GetMapping("/colleges")
    public List<String> colleges() {
        return COLLEGES;
    }

    @GetMapping("/dict/application-types")
    public List<Map<String, String>> applicationTypes() {
        return APPLICATION_TYPES;
    }
}
