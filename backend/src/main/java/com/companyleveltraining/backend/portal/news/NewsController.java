package com.companyleveltraining.backend.portal.news;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.companyleveltraining.backend.portal.news.dto.NewsCategoryResponse;
import com.companyleveltraining.backend.portal.news.dto.NewsResponse;

/** 门户校园资讯接口：登录用户查看资讯列表、分类、详情。 */
@RestController
@RequestMapping("/api/portal/news")
@Tag(name = "校园资讯", description = "门户校园资讯分类、列表和详情接口")
public class NewsController {

    private final NewsService service;

    public NewsController(NewsService service) {
        this.service = service;
    }

    @GetMapping("/categories")
    @Operation(summary = "资讯分类", description = "查询校园资讯分类列表")
    public List<NewsCategoryResponse> categories() {
        return service.categories();
    }

    @GetMapping
    @Operation(summary = "资讯列表", description = "按分类和关键字查询已发布校园资讯")
    public List<NewsResponse> list(@RequestParam(required = false) Long categoryId,
                                   @RequestParam(required = false) String keyword) {
        return service.portalList(categoryId, keyword);
    }

    @GetMapping("/{id}")
    @Operation(summary = "资讯详情", description = "查询已发布校园资讯详情")
    public NewsResponse detail(@PathVariable Long id) {
        return service.portalDetail(id);
    }
}
