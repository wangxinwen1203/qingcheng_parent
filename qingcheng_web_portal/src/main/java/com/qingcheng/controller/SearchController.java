package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.SkuSearchService;
import com.qingcheng.util.WebUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class SearchController {

    @Reference
    private SkuSearchService skuSearchService;

    @GetMapping("/search")
    public String search(Model model, @RequestParam Map<String, String> searchMap) throws Exception {
        //解决乱码
        searchMap = WebUtil.convertCharsetToUTF8(searchMap);
        //判断传过来的当前页码是否为空,默认为1

        if (searchMap.get("pageNo") == null) {
            searchMap.put("pageNo", "1");
        }


        if (searchMap.get("sort") == null) {
            searchMap.put("sort", "");
        }
        if (searchMap.get("sortOrder") == null) {
            searchMap.put("sortOrder", "DESC");
        }

        Map result = skuSearchService.search(searchMap);
        StringBuilder url = new StringBuilder("/search.do?");
        for (String key : searchMap.keySet()) {
            url.append("&" + key + "=" + searchMap.get(key));
        }
        model.addAttribute("url", url);

        model.addAttribute("result", result);//调方法传回结果
        model.addAttribute("searchMap", searchMap);

        //把当前页传回页面
        int pageNo = Integer.parseInt(searchMap.get("pageNo"));
        model.addAttribute("pageNo", pageNo);
        int startPage = 1;
        Long totalPages = (Long) result.get("totalPages");
        int endPage = totalPages.intValue();//截至代码
        if (totalPages > 5) {
            startPage = pageNo - 2;
            if (startPage < 1) {
                startPage = 1;
            }
            endPage = startPage + 4;
            if (endPage > totalPages) {
                endPage = Math.toIntExact(totalPages);
                startPage = endPage - 5;
            }

        }
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);



        return "search";
    }
}
