package com.offcn.search.controller;

import com.offcn.entity.Page;
import com.offcn.search.feign.SkuFeign;
import com.offcn.search.pojo.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping(value = "/search")
public class SkuController {

    @Autowired
    private SkuFeign skuFeign;

    /**
     * 搜索
     *
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false) Map searchMap, Model model) {
        //dongyimai-search-service微服务
        Map resultMap = skuFeign.search(searchMap);
        //搜索数据结果
        model.addAttribute("result", resultMap);
        //搜索条件，用于数据回显，用户按条件搜索以后，跳转的页面时不一样的，但原本输入的内容还需要给用户保留
        model.addAttribute("searchMap", searchMap);
        //记住之前的URL
        //拼接url
        String url = this.setUrl(searchMap);
        model.addAttribute("url", url);
        Page<SkuInfo> infoPage = new Page<SkuInfo>(
                Long.valueOf(resultMap.get("total").toString()),
                Integer.valueOf(resultMap.get("pageNum").toString()),
                Integer.valueOf(resultMap.get("pageSize").toString())
        );
        model.addAttribute("page", infoPage);
        return "search";
    }

    //setUrl()方法作用是将新的筛选条件拼接在原条件尾部
    private String setUrl(Map<String, String> searchMap) {
        String url = "/search/list";
        if (searchMap != null && searchMap.size() > 0) {
            url += "?";
            for (Map.Entry<String, String> stringStringEntry : searchMap.entrySet()) {
                String key = stringStringEntry.getKey();// keywords / brand  / category
                String value = stringStringEntry.getValue();//华为  / 华为  / 笔记本
                if (stringStringEntry.getKey().equals("sortField") || stringStringEntry.getKey().equals("sortRule")) {
                    continue;//结束本次，循环下次
                }
                if(stringStringEntry.getKey().equals("pageNum")){
                    continue;
                }
                url += key + "=" + value + "&";
            }
            //去掉多余的&
            if (url.lastIndexOf("&") != -1) {
                url = url.substring(0, url.lastIndexOf("&"));
            }

        }
        return url;
    }
}