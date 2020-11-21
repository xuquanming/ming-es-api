package com.company.utils;

import com.company.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

@Component
public class HtmlParseUtil {

    public static void main(String[] args) throws IOException {
        new HtmlParseUtil().parseJD("java").forEach(System.out::println);
    }

    public ArrayList<Content> parseJD(String keywords) throws IOException {
        // 获取请求,前提需要联网，ajax不能获取到！
        String url = "https://search.jd.com/Search?keyword=" + keywords + "&enc=utf-8";
        // 解析网页.（Jsoup返回的Document就是页面对象）
        Document document = Jsoup.parse(new URL(url), 30000);
        // 所有你在js中可以使用的方法
        Element element = document.getElementById("J_goodsList");
        // 获取所有的li元素
        Elements elements = element.getElementsByTag("li");
        // 获取元素中的内容,el代表每一个li标签

        ArrayList<Content> goodsList = new ArrayList<>();

        for (Element el : elements) {
            // 关于这种图片特别多的网站，都是延迟加载的
            String image = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();

            Content content = new Content();
            content.setTitle(title);
            content.setImg(image);
            content.setPrice(price);
            goodsList.add(content);

            System.out.println("=========================");
            System.out.println(image);
            System.out.println(price);
            System.out.println(title);
        }

        return goodsList;
    }
}


