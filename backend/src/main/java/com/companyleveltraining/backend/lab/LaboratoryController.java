package com.companyleveltraining.backend.lab;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.lab.dto.LabResponse;

/**
 * 公共实验室接口：供学生/教师预约页、课表页的实验室下拉选择使用，只返回可用（active）实验室。
 */
@RestController
@RequestMapping("/api/laboratories")
public class LaboratoryController {

    private final LaboratoryService service;

    public LaboratoryController(LaboratoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<LabResponse> list() {
        return service.list(null, true);
    }

    @GetMapping("/{id}")
    public LabResponse get(@PathVariable Long id) {
        return service.get(id);
    }
}
