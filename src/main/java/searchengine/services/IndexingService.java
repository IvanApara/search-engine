package searchengine.services;

import searchengine.dto.response.ResultDTO;

public interface IndexingService {
    ResultDTO startIndexing();
    ResultDTO stopIndexing();
    ResultDTO indexOnePage(String url);
    boolean indexPage(String urlPage);

}
