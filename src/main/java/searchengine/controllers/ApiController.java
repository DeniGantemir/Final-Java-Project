package searchengine.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;

import searchengine.services.SearchWordsService;
import searchengine.services.SiteIndexingService;
import searchengine.services.StatisticsService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final SiteIndexingService siteIndexingService;
    private final SearchWordsService searchWordsService;


    public ApiController(StatisticsService statisticsService,
                         SiteIndexingService siteIndexingService,
                         SearchWordsService searchWordsService) {
        this.statisticsService = statisticsService;
        this.siteIndexingService = siteIndexingService;
        this.searchWordsService = searchWordsService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity startIndexing() {
        try {
            siteIndexingService.startIndexing();
            return ResponseEntity.ok(Map.of("result", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("result", false, "error", "Индексация уже запущена"));
        }
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity stopIndexing() {
        try {
            siteIndexingService.stopIndexing();
            return ResponseEntity.ok(Map.of("result", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("result", false, "error", "Индексация уже запущена"));
        }
    }
    @PostMapping("/indexPage")
    public ResponseEntity indexPage(@RequestParam("url") String url) {
        try {
            siteIndexingService.indexPageMethod(url);
            return ResponseEntity.ok(Map.of("result", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("result", false, "error", "Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam("query") String query,
                                                 @RequestParam(value = "site", required = false) String site,
                                                 @RequestParam(value = "offset", defaultValue = "0", required = false) int offset,
                                                 @RequestParam(value = "limit", defaultValue = "0", required = false) int limit) throws IOException {
        try {
            if (query.isEmpty()) {
                SearchResponse errorResponse = new SearchResponse(false, 0, null, "Задан пустой поисковый запрос");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            SearchResponse response = searchWordsService.searchLemmaMethod(query, site, offset, limit);
            if (response.getData() == null || response.getData().isEmpty()) {
                SearchResponse errorResponse = new SearchResponse(false, 0, null, "Данный запрос не найден");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SearchResponse errorResponse = new SearchResponse(false, 0, null, "Указанная страница не найдена");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
