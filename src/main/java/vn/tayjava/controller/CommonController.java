package vn.tayjava.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.tayjava.dto.respond.ResponseData;
import vn.tayjava.dto.respond.ResponseError;
import vn.tayjava.service.MailService;

@RestController
@RequestMapping("/common")
@Slf4j
@RequiredArgsConstructor
public class CommonController {

    private final MailService mailService;

    @PostMapping("/send-email")
    public ResponseData<String> sendEmail(@RequestParam String recipients, @RequestParam String subject, @RequestParam String content, @RequestParam(required = false) MultipartFile[] files) {

        try {
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), mailService.sendEmail(recipients, subject, content, files));
        } catch (Exception e) {
            log.error("Sending email was failure, errorMessage={}", e.getMessage());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Sending email was failure");
        }
    }
}
