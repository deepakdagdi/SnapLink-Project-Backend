package com.url.shortner.controller;

import com.url.shortner.dtos.ClickEventDTO;
import com.url.shortner.dtos.UrlMappingDTO;
import com.url.shortner.models.User;
import com.url.shortner.service.UrlMappingService;
import com.url.shortner.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/urls")
@AllArgsConstructor
public class UrlMappingController {

    private UrlMappingService urlMappingService;

    private UserService userService;

    //{"originalUrl":"https://example.com"}
    // https://abc.com/0Ntv4Ds3 --> https://example.com

    // whenever we post request of URL is->  http://localhost:8091/api/urls/shorten 
    // then 1st you pass login user token with the header section and then in request RequestBody
    // you pass the original URL in JSON format like this : {"originalUrl":"https://example.com"}

    @PostMapping("/shorten")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlMappingDTO> createShortener( @RequestBody
                                                             Map<String,String> request,
                                                         Principal principal)
    {

        String originalUrl= request.get("originalUrl");
       User user =userService.findByUsername(principal.getName());
        UrlMappingDTO urlMappingDTO  =urlMappingService.createShortUrl(originalUrl,user);
        return ResponseEntity.ok(urlMappingDTO);


    }


    @GetMapping("/myurls")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UrlMappingDTO>> getUserUrl(Principal principal){
        User user=userService.findByUsername(principal.getName());
        List<UrlMappingDTO>  urls=urlMappingService.getUrlByUser(user);
        return ResponseEntity.ok(urls);
    }

    @GetMapping("/analytics/{shortUrl}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ClickEventDTO>> getUrlAnalytics(@PathVariable String shortUrl,
                                                               @RequestParam("startDate") String startDate,
                                                               @RequestParam("endDate") String endDate){
        DateTimeFormatter formatter =DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime start=LocalDateTime.parse(startDate,formatter);
        LocalDateTime end=LocalDateTime.parse(endDate,formatter);
        List<ClickEventDTO> clickEventDTO=urlMappingService.getClickEventByDate(shortUrl,start,end);
        return ResponseEntity.ok(clickEventDTO);

    }

    @GetMapping("/totalClicks")
    @PreAuthorize("hasRole('USER')")
     public ResponseEntity<Map<LocalDate,Long>> totalClicksByDate(Principal principal,
                                                                  @RequestParam("startDate") String startDate,
                                                                  @RequestParam("endDate") String endDate){

        DateTimeFormatter formatter =DateTimeFormatter.ISO_LOCAL_DATE;
        User user=userService.findByUsername(principal.getName());
        LocalDate start=LocalDate.parse(startDate,formatter);
        LocalDate end=LocalDate.parse(endDate,formatter);

        Map<LocalDate,Long> totalClicks=urlMappingService.getTotalClicksByUserAndDate(user,start,end);
       return ResponseEntity.ok(totalClicks);


    }

}
