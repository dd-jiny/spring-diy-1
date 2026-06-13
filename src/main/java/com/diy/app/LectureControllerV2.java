package com.diy.app;

import com.diy.framework.context.annotation.Controller;
import com.diy.framework.web.mvc.anotation.RequestMapping;
import com.diy.framework.web.mvc.anotation.RequestMethod;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Controller
public class LectureControllerV2 {

    private final LectureService lectureService;

    public LectureControllerV2(final LectureService lectureService) {
        this.lectureService = lectureService;
    }

    @RequestMapping(value = "/lectures", methods = {RequestMethod.POST})
    private String createLecture(final HttpServletRequest req) throws IOException {
        final byte[] bodyBytes = req.getInputStream().readAllBytes();
        final String body = new String(bodyBytes, StandardCharsets.UTF_8);

        final Lecture lecture = new ObjectMapper().readValue(body, Lecture.class);

        if (lecture.getName() == null || lecture.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (lecture.getPrice().compareTo(BigDecimal.ZERO) < 1) {
            throw new IllegalArgumentException("price must be non-negative");
        }

        return "redirect:/lectures";
    }

    @RequestMapping(value = "/lectures", methods = {RequestMethod.GET})
    private String getLectures() {
        return "lecture-list";
    }
}
