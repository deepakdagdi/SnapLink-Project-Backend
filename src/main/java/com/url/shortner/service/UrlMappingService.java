package com.url.shortner.service;

import com.url.shortner.dtos.ClickEventDTO;
import com.url.shortner.dtos.UrlMappingDTO;
import com.url.shortner.models.ClickEvent;
import com.url.shortner.models.UrlMapping;
import com.url.shortner.models.User;
import com.url.shortner.repository.ClickEventRepo;
import com.url.shortner.repository.UrlMappingRepo;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UrlMappingService {


    private UrlMappingRepo urlMappingRepo;


    private ClickEventRepo clickEventRepo;

    public UrlMappingDTO createShortUrl(String originalUrl, User user){

        String shortUrl=generateShortUrl();
        UrlMapping urlMapping=new UrlMapping();

         urlMapping.setOriginalUrl(originalUrl);
         urlMapping.setShortUrl(shortUrl);
         urlMapping.setUser(user);
         urlMapping.setCreateDate(LocalDateTime.now());
         UrlMapping savedUrlMapping=urlMappingRepo.save(urlMapping);
          return covertToDto(savedUrlMapping);

 }

 private   UrlMappingDTO covertToDto(UrlMapping urlMapping){
        UrlMappingDTO urlDto=new UrlMappingDTO();
        urlDto.setId(urlMapping.getId());
        urlDto.setOriginalUrl(urlMapping.getOriginalUrl());
        urlDto.setShortUrl(urlMapping.getShortUrl());
        urlDto.setClickCount(urlMapping.getClickCount());
        urlDto.setCreateDate(urlMapping.getCreateDate());
        urlDto.setUsername(urlMapping.getUser().getUsername());
        return urlDto;

 }
 private  String  generateShortUrl()
 {
     String  character="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

     Random random=new Random();

     StringBuilder shortUrl=new StringBuilder(8);

     for(int i=0;i<8;i++)
     {
         shortUrl.append(character.charAt(random.nextInt(character.length())));
     }
     return shortUrl.toString();
  }

    public List<UrlMappingDTO> getUrlByUser(User user) {

        return urlMappingRepo
                .findByUser(user)
                .stream().map(this :: covertToDto)
                .toList();

    }


    public List<ClickEventDTO> getClickEventByDate(String shortUrl, LocalDateTime start, LocalDateTime  end)
    {
        UrlMapping urlMapping=urlMappingRepo.findByShortUrl(shortUrl);
        if(urlMapping!=null)
        {
            return clickEventRepo.findByUrlMappingAndClickDateBetween(urlMapping,start,end).stream()
                    .collect(Collectors.groupingBy(click -> click.getClickDate().toLocalDate(),Collectors.counting()))
                    .entrySet().stream()
                    .map(entry ->{
                        ClickEventDTO clickEventDTO=new ClickEventDTO();
                        clickEventDTO.setClickDate(entry.getKey());
                        clickEventDTO.setCount(entry.getValue());
                        return clickEventDTO;
                    })
                    .collect(Collectors.toList());

        }
        return null;
    }

    public Map<LocalDate, Long> getTotalClicksByUserAndDate(User user, LocalDate start, LocalDate end) {

        List<UrlMapping> urlMappings=urlMappingRepo.findByUser(user);
        List<ClickEvent> clickEvents=clickEventRepo.findByUrlMappingInAndClickDateBetween(urlMappings,start.atStartOfDay(),end.plusDays(1).atStartOfDay());

        return  clickEvents
                .stream()
                .collect(Collectors
                         .groupingBy(click -> click
                                 .getClickDate()
                                 .toLocalDate(),Collectors.counting()));
    }

    public UrlMapping getOriginalUrl(String shortUrl) {
        UrlMapping urlMapping=urlMappingRepo.findByShortUrl(shortUrl);
        if(urlMapping!=null){
            urlMapping.setClickCount(urlMapping.getClickCount() +1);
            urlMappingRepo.save(urlMapping);

            //record click event
            ClickEvent clickEvent=new ClickEvent();
            clickEvent.setClickDate(LocalDateTime.now());
            clickEvent.setUrlMapping(urlMapping);
            clickEventRepo.save(clickEvent);

        }
        return urlMapping;
    }
}



