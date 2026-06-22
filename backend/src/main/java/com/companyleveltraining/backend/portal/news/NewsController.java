package com.companyleveltraining.backend.portal.news;

import java.util.List;

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
public class NewsController {

    private final NewsService service;

    public NewsController(NewsService service) {
        this.service = service;
    }

    @GetMapping("/categories")
    public List<NewsCategoryResponse> categories() {
        return service.categories();
    }

    @GetMapping
    public List<NewsResponse> list(@RequestParam(required = false) Long categoryId,
                                   @RequestParam(required = false) String keyword) {
        return service.portalList(categoryId, keyword);
    }

    @GetMapping("/{id}")
    public NewsResponse detail(@PathVariable Long id) {
        return service.portalDetail(id);
    }
}
