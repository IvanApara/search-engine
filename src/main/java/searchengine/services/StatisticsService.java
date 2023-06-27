package searchengine.services;

import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteModel;

import java.util.List;

public interface StatisticsService {
    TotalStatistics getTotalStatistics();
    DetailedStatisticsItem getDetailedFromDetailedStatisticItem(SiteModel site);
    List<DetailedStatisticsItem> getDetailedStatisticsItemList();
    StatisticsResponse getStatisticsResponse();
}
