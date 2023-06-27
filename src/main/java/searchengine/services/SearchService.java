package searchengine.services;

import searchengine.dto.SearchDto;
import searchengine.dto.response.ResultDTO;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.processing.SearchStarter;
import searchengine.repository.SiteRepository;

import java.util.List;
import java.util.Map;

public interface SearchService {
    List<SearchDto> createSearchDtoList(List<LemmaModel> lemmaList,
                                        List<String> textLemmaList,
                                        int start, int limit);
    Map<PageModel, Float>  pageListBulkhead(List<PageModel> pageList, List<IndexModel> indexList,
                                            Map<PageModel, Float> relevanceMap );
    List<LemmaModel> getLemmaModelFromSite(List<String> lemmas, SiteModel site);
    List<String> getLemmaFromSearchText(String text);
    String clearCodeFromTag(String text, String element);
    ResultDTO search(String query, String site, int offset,
                     SiteRepository siteRepository, SearchStarter searchStarter);
}
