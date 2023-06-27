package searchengine.processing;

import org.springframework.stereotype.Service;
import searchengine.dto.SearchDto;
import searchengine.model.LemmaModel;
import searchengine.model.SiteModel;
import searchengine.repository.SiteRepository;
import searchengine.services.Impl.SearchServiceImpl;

import java.util.ArrayList;
import java.util.List;

@Service
public record SearchStarter(SiteRepository siteRepository, SearchServiceImpl searchServiceImpl) {

    public List<SearchDto> getSearchFromOneSite(String text,
                                                String url,
                                                int start,
                                                int limit) {
        SiteModel site = siteRepository.findByUrl(url);
        List<String> textLemmaList = searchServiceImpl.getLemmaFromSearchText(text);
        List<LemmaModel> foundLemmaList = searchServiceImpl.getLemmaModelFromSite(textLemmaList, site);
        return searchServiceImpl.createSearchDtoList(foundLemmaList, textLemmaList, start, limit);
    }


    public List<SearchDto> getFullSearch(String text,
                                         int start,
                                         int limit) {
        List<SiteModel> siteList = siteRepository.findAll();
        List<SearchDto> result = new ArrayList<>();
        List<LemmaModel> foundLemmaList = new ArrayList<>();
        List<String> textLemmaList = searchServiceImpl.getLemmaFromSearchText(text);
        for (int i = 0; i < siteList.size(); i++) {
            SiteModel site = siteList.get(i);
            foundLemmaList.addAll(searchServiceImpl.getLemmaModelFromSite(textLemmaList, site));
        }

        String[] arr = text.split(" ");

        List<SearchDto> searchData = new ArrayList<>();

        for (String oneWord : arr) {
            for (LemmaModel l : foundLemmaList) {
                if (l.getLemma().equals(oneWord)) {
                    searchData = (searchServiceImpl.createSearchDtoList(foundLemmaList, textLemmaList, start, limit));
                    searchData.sort((o1, o2) -> Float.compare(o2.relevance(), o1.relevance()));
                    checkSize(searchData, result, start, limit);
                }
            }
        }
        return searchData;
    }

    public List<SearchDto> checkSize(List<SearchDto> searchData, List<SearchDto> result, int start, int limit) {
        if (searchData.size() > limit) {
            int y = start;
            while (y < limit) {
                result.add(searchData.get(y));
                y++;
            }
            return result;
        }
        return searchData;
    }






}
