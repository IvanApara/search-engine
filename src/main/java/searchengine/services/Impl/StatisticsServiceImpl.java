package searchengine.services.Impl;

import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteModel;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.StatisticsService;


import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public record StatisticsServiceImpl(PageRepository pageRepository, LemmaRepository lemmaRepository,
                                    SiteRepository siteRepository) implements StatisticsService {

    @Override
    public TotalStatistics getTotalStatistics() {
        long sites = siteRepository.count();
        long pages = pageRepository.count();
        long lemmas = lemmaRepository.count();
        return new TotalStatistics(sites, pages, lemmas, true);
    }

    @Override
    public DetailedStatisticsItem getDetailedFromDetailedStatisticItem(SiteModel site) {
        String url = site.getUrl();
        String name = site.getName();
        String status = site.getStatus().toString();
        Date statusTime = site.getStatusTime();
        String error = site.getLastError();
        long pages = pageRepository.countBySiteId(site);
        long lemmas = lemmaRepository.countBySiteModelId(site);
        return new DetailedStatisticsItem(url, name, status, statusTime, error, pages, lemmas);
    }

    @Override
    public List<DetailedStatisticsItem> getDetailedStatisticsItemList() {
        List<SiteModel> siteList = siteRepository.findAll();
        return siteList.stream().map(this::getDetailedFromDetailedStatisticItem).collect(Collectors.toList());
    }

    @Override
    public StatisticsResponse getStatisticsResponse() {
        TotalStatistics total = getTotalStatistics();
        List<DetailedStatisticsItem> list = getDetailedStatisticsItemList();
        return new StatisticsResponse(true, new StatisticsData(total, list));
    }
}
