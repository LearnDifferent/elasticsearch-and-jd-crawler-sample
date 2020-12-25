package com.example.escrawlerjd.controller;

import com.example.escrawlerjd.service.EsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@CrossOrigin
@RestController
public class EsController {

    @Autowired
    private EsService esService;

    @GetMapping("/jd/{keyword}")
    public Boolean getJdGoodsThenPut(@PathVariable("keyword") String keyword) {
        try {
            return esService.putJdGoodsIntoEs(keyword);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @GetMapping("/search/{keyword}/{currentPage}/{pageSize}")
    public Map<String, Object> searchPage(
            @PathVariable("keyword") String keyword
            , @PathVariable("currentPage") int currentPage
            , @PathVariable("pageSize") int pageSize
    ) {
        try {
            return esService.limitSearch(keyword, currentPage, pageSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
