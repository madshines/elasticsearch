package com.madshines;

import com.madshines.service.ContentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
class SpringbootesJindongApplicationTests {
    @Autowired
    ContentService contentService;
    @Test
    void contextLoads() throws IOException {
        contentService.parseContent("java");
    }
    @Test
    void search() throws IOException {
        List<Map<String, Object>> list = contentService.searchPage("java", 1, 10);
        System.out.println(list);
    }
}
