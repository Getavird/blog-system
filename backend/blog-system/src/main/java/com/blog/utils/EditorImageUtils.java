package com.blog.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EditorImageUtils {
    
    /**
     * 从文章内容中提取所有图片URL（base64和普通URL）
     */
    public List<String> extractImageUrls(String htmlContent) {
        List<String> imageUrls = new ArrayList<>();
        
        if (htmlContent == null || htmlContent.isEmpty()) {
            return imageUrls;
        }
        
        Document doc = Jsoup.parse(htmlContent);
        Elements imgElements = doc.select("img");
        
        for (Element img : imgElements) {
            String src = img.attr("src");
            if (src != null && !src.isEmpty()) {
                imageUrls.add(src);
            }
        }
        
        return imageUrls;
    }
    
    /**
     * 提取Base64格式的图片数据
     */
    public List<String> extractBase64Images(String htmlContent) {
        List<String> base64Images = new ArrayList<>();
        Pattern pattern = Pattern.compile("data:image/([^;]+);base64,([^\"]+)");
        Matcher matcher = pattern.matcher(htmlContent);
        
        while (matcher.find()) {
            base64Images.add(matcher.group(0)); // 完整的base64字符串
        }
        
        return base64Images;
    }
    
    /**
     * 将base64图片替换为服务器URL
     */
    public String convertBase64ToUrls(String htmlContent, List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return htmlContent;
        }
        
        Document doc = Jsoup.parse(htmlContent);
        Elements imgElements = doc.select("img[src^=data:image/]");
        
        int index = 0;
        for (Element img : imgElements) {
            if (index < fileUrls.size()) {
                img.attr("src", fileUrls.get(index));
                index++;
            }
        }
        
        return doc.body().html();
    }
    
    /**
     * 为上传的图片生成唯一文件名
     */
    public String generateImageFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}