package com.wcagapp.wcagathome.web;

import com.wcagapp.wcagathome.SiteProcessor;
import com.wcagapp.wcagathome.colorblindness.VisionType;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Controller
public class IndexController {

    private final SiteProcessor siteProcessor;

    public IndexController(SiteProcessor siteProcessor) {
        this.siteProcessor = siteProcessor;
    }

    @GetMapping(value = "/")
    public String indexPage() {
        return "index.html";
    }

    @GetMapping(value = "/testPage")
    public String testPage() {
        return "testSite.html";
    }

    @PostMapping(value = "/sendUrl")
    @ResponseBody
    public String processUrl(HttpServletRequest request) {

//        try {
//            FileUtils.cleanDirectory(new File("src/main/resources/static/tmpImg/"));
//            FileUtils.cleanDirectory(new File("src/main/resources/static/tmpCss"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        try {
            siteProcessor.setOriginalSite(request.getParameter("url"));
//        } catch (Exception e) {
//            return "<p>" + e.getMessage() + "</p>";
//        }
        siteProcessor.modifyColors(VisionType.ACHROMATOPSIA);

        return siteProcessor.getOriginalSite();
    }
}
