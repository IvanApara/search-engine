package searchengine.controllers;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.response.ResultDTO;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repository.SiteRepository;
import searchengine.processing.SearchStarter;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;


@RestController
@RequestMapping("/api")
@Slf4j
public record ApiController(StatisticsService statisticsService, IndexingService indexingService,
                            SiteRepository siteRepository, SearchStarter searchStarter,
                            SearchService searchService) {



    @ApiOperation("Get all statistics")
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics() {
        return ResponseEntity.ok(statisticsService.getStatisticsResponse());
    }

    @ApiOperation("Start parsing web")
    @GetMapping("/startIndexing")
    public ResultDTO startIndexing() {
        return indexingService.startIndexing();
    }

    @ApiOperation("Stop parsing web")
    @GetMapping("/stopIndexing")
    public ResultDTO stopIndexing() {
        log.info("ОСТАНОВКА ИНДЕКСАЦИИ");
        return indexingService.stopIndexing();
    }

    @PostMapping("/indexPage")
    @ApiOperation("Индексация отдельной страницы")
    public ResultDTO indexPage(@RequestParam(name = "url") String url) {
        return indexingService.indexOnePage(url);
    }



    @ApiOperation("Search in sites")
    @GetMapping("/search")
    public ResultDTO search(@RequestParam(name = "query", required = false, defaultValue = "") String query,
                                      @RequestParam(name = "site", required = false, defaultValue = "") String site,
                                    @RequestParam(name = "offset", required = false, defaultValue = "0") int offset) {
        return searchService.search(query, site, offset, siteRepository, searchStarter);

    }
}
