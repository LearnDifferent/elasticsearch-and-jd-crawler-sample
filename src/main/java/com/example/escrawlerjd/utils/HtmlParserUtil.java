package com.example.escrawlerjd.utils;

import com.example.escrawlerjd.pojo.JdGoods;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析网页并放入 ES 中
 */
public class HtmlParserUtil {

    public static List<JdGoods> parseJdGoodsByKeyword(String keyword) throws IOException {
        // 链接
        String url = "https://search.jd.com/Search?keyword=" + keyword;

        // 存放内容的容器
        List<JdGoods> jdGoods = new ArrayList<>();

        // Document parse(URL url, int timeoutMillis)
        Document document = Jsoup.parse(new URL(url), 3000);

        // jd 展示商品的页面的元素是：div id="J_goodsList"
        Element goodsList = document.getElementById("J_goodsList");

        // 每个商品都有 li 标签：<li class="gl-item">
        Elements lis = goodsList.getElementsByTag("li");

        // 遍历所有 li 标签
        for (Element li : lis) {
            // <li class="gl-item"> 有些 li 标签的 class 不是 gl-item，要被排除
            if (li.attr("class").equalsIgnoreCase("gl-item"))  {
                // <img width="220" height="220" data-lazy-img="done" src="...jpg">
                // 注意，jd 的图片使用了懒加载，data-lazy-img="done" 表示懒加载完成，所以看不到图片地址
                // 也就是说，图片的地址不是在 src，而是在还没变成 "done" 的 data-lazy-img
                String img = li.getElementsByTag("img").eq(0).attr("data-lazy-img");

                // <div class="p-price"> 这里直接获取 text
                String price = li.getElementsByClass("p-price").eq(0).text();

                // <div class="p-name">
                String desc = li.getElementsByClass("p-name").eq(0).text();

                jdGoods.add(new JdGoods(img, price, desc));
            }
        }
        return jdGoods;
    }


}
