package searchengine.controllers;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.response.ResultDTO;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repository.SiteRepository;
import searchengine.services.Impl.IndexingServiceImpl;
import searchengine.services.Impl.StatisticsServiceImpl;
import searchengine.processing.SearchStarter;
import searchengine.services.Impl.SearchServiceImpl;


@RestController
@RequestMapping("/api")
@Slf4j
public record ApiController(StatisticsServiceImpl statisticsServiceImpl, IndexingServiceImpl indexingServiceImpl,
                            SiteRepository siteRepository, SearchStarter searchStarter,
                            SearchServiceImpl searchServiceImpl) {

    @ApiOperation("Get all statistics")
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics() {
        return ResponseEntity.ok(statisticsServiceImpl.getStatisticsResponse());
    }

    @ApiOperation("Start parsing web")
    @GetMapping("/startIndexing")
    public ResultDTO startIndexing() {
        return indexingServiceImpl.startIndexing();
    }

    @ApiOperation("Stop parsing web")
    @GetMapping("/stopIndexing")
    public ResultDTO stopIndexing() {
        log.info("ОСТАНОВКА ИНДЕКСАЦИИ");
        return indexingServiceImpl.stopIndexing();
    }

    @PostMapping("/indexPage")
    @ApiOperation("Индексация отдельной страницы")
    public ResultDTO indexPage(@RequestParam(name = "url") String url) {
        return indexingServiceImpl.indexOnePage(url);
    }



    @ApiOperation("Search in sites")
    @GetMapping("/search")
    public ResultDTO search(@RequestParam(name = "query", required = false, defaultValue = "") String query,
                                      @RequestParam(name = "site", required = false, defaultValue = "") String site,
                                    @RequestParam(name = "offset", required = false, defaultValue = "0") int offset) {
        return searchServiceImpl.search(query, site, offset, siteRepository, searchStarter);

    }
}
