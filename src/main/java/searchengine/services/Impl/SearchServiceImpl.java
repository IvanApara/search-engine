package searchengine.services.Impl;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.dto.SearchDto;
import searchengine.dto.response.ResultDTO;
import searchengine.exception.CurrentIOException;
import searchengine.lemma.LemmaEngine;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.processing.SearchStarter;
import searchengine.services.SearchService;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public record SearchServiceImpl(LemmaEngine lemmaEngine, LemmaRepository lemmaRepository,
                                PageRepository pageRepository, IndexRepository indexRepository)
        implements SearchService {



    private List<SearchDto> getSearchDtoList(ConcurrentHashMap<PageModel, Float> pageList,
                                             List<String> textLemmaList) {
        List<SearchDto> searchDtoList = new ArrayList<>();
        StringBuilder titleStringBuilder = new StringBuilder();

        for (PageModel page : pageList.keySet()) {
            String uri = page.getPath();
            String content = page.getContent();
            SiteModel pageSite = page.getSiteId();
            String site = pageSite.getUrl();
            String siteName = pageSite.getName();
            String title = clearCodeFromTag(content, "title");
            String body = clearCodeFromTag(content, "body");
            titleStringBuilder.append(title).append(body);
            float pageValue = pageList.get(page);
            List<Integer> lemmaIndex = new ArrayList<>();

            addedLemmaIndexInList(textLemmaList, lemmaIndex, titleStringBuilder);
            if (lemmaIndex.size() == 0) {
                continue;
            }
            Collections.sort(lemmaIndex);
            StringBuilder snippetBuilder = new StringBuilder();
            List<String> wordList = getWordsFromSiteContent(titleStringBuilder.toString(), lemmaIndex, textLemmaList);

            addedSnippetBuilder(wordList,snippetBuilder);
            if (wordList.size() == 0) {
                continue;
            }
            searchDtoList.add(new SearchDto(site, siteName, uri, title, snippetBuilder.toString(), pageValue));
            titleStringBuilder.setLength(0);
        }
        return searchDtoList.stream().distinct().collect(Collectors.toList());
    }

    private void addedSnippetBuilder(List<String> wordList, StringBuilder snippetBuilder) {
        for (int y = 0; y < wordList.size(); y++) {
            snippetBuilder.append(wordList.get(y)).append(".");
            if (y > 3) {
                break;
            }
        }
    }

    private void addedLemmaIndexInList(List<String> textLemmaList, List<Integer> lemmaIndex, StringBuilder titleStringBuilder) {
        for (int i = 0; i < textLemmaList.size(); i++) {
            String lemma = textLemmaList.get(i);
            try {
                lemmaIndex.addAll(lemmaEngine.findLemmaIndexInText(titleStringBuilder.toString(), lemma));
                if (lemmaIndex.size() == 0) {
                    break;
                }
            } catch (IOException e) {
                new CurrentIOException(e.getMessage());
            }
        }
    }

    private List<String> getWordsFromSiteContent(String content, List<Integer> lemmaIndex, List<String> textLemmaList) {
        List<String> result = new ArrayList<>();
        List<Integer> lemmaIndexCroppedText = new ArrayList<>();

        int i = 0;
        while (i < lemmaIndex.size()) {
            int start = lemmaIndex.get(i);

            String textFinish = "";
            if (start + 250 >= content.length() || start == 0) {
                textFinish += content.substring(start, content.length() - 1);
            } else {

                int textСroppingStart = content.lastIndexOf('.', start);
                if (textСroppingStart < 0) {
                    textСroppingStart = content.lastIndexOf("", start);
                }
                int textСroppingEnd = textСroppingStart + 250;
                textFinish += content.substring(textСroppingStart + 1, textСroppingEnd).
                        replaceFirst(",", "");
            }

            String finishText = textFinish.replace("[^А-Яа-я0-9,.]", "");


            switch (finishText.length()) {
                case (250) -> finishText.substring(0, 250);
                default -> finishText.substring(0, finishText.length() - 1);
            }


            searchForWordsInTheOutputText(textLemmaList,lemmaIndexCroppedText,finishText);
                if (lemmaIndexCroppedText.size() < 2 && textLemmaList.size() > 1 || textLemmaList.size() == 1 &&
                        lemmaIndexCroppedText.size() == 0) {
                    i++;
                    continue;
                }else {

                    if(lemmaIndexCroppedText.size() != 0){
                        finishText = textSelection(lemmaIndexCroppedText,finishText);
                }
                    result.add(finishText.replaceFirst("[)]", ""));
                    break;
                }

        }
        result.sort(Comparator.comparing(String::length).reversed());
        return result;
    }

    public String textSelection(List<Integer> lemmaIndexCroppedText,String finishText ){
        for (int j = 0; j <= lemmaIndexCroppedText.size(); j++) {

            int oneWordStart = lemmaIndexCroppedText.get(j);
            int oneWordEnd = finishText.indexOf(" ", oneWordStart);
            String word1 = finishText.substring(oneWordStart, oneWordEnd);


            if (lemmaIndexCroppedText.size() == 2 || lemmaIndexCroppedText.size() == 3) {
                int twoWordStart = lemmaIndexCroppedText.get(1);
                int twoWordEnd = finishText.indexOf(" ", twoWordStart);
                String word2 = finishText.substring(twoWordStart, twoWordEnd);

                if (lemmaIndexCroppedText.size() == 3) {
                    int threeWordStart = lemmaIndexCroppedText.get(2);
                    int threeWordEnd = finishText.indexOf(" ", threeWordStart);
                    String word3 = finishText.substring(threeWordStart, threeWordEnd);
                    finishText = finishText.replace(word1, "<b>".concat(word1).concat("</b>")).
                            replace(word2, "<b>".concat(word2).concat("</b>")).
                            replace(word3, "<b>".concat(word3).concat("</b>"));
                    break;
                }

                finishText = finishText.replace(word1, "<b>".concat(word1).concat("</b>")).
                        replace(word2, "<b>".concat(word2).concat("</b>"));
                break;
            }

            finishText = finishText.replace(word1, "<b>".concat(word1).concat("</b>"));
            break;

        }
        return finishText;
    }
    public void searchForWordsInTheOutputText(List<String> textLemmaList, List<Integer> lemmaIndexCroppedText,
                                              String finishText) {
        try {
            for (int a = 0; a < textLemmaList.size(); a++) {
                String lemmaOne = textLemmaList.get(a);

                if (lemmaIndexCroppedText.size() > 0) {

                    String lemmaTwo = textLemmaList.get(a);
                    lemmaIndexCroppedText.addAll(lemmaEngine.findLemmaIndexInText(finishText, lemmaTwo));

                    if (lemmaIndexCroppedText.size() == 2) {
                        if (textLemmaList.size() == 3) {
                            if (a == 2 && lemmaIndexCroppedText.size() == 2) {
                                lemmaIndexCroppedText.clear();
                            }
                            continue;
                        }
                        break;
                    }

                    if (textLemmaList.size() == 3 && lemmaIndexCroppedText.size() == 3 && a == 2) {
                        continue;
                    }
                    lemmaIndexCroppedText.clear();
                    continue;
                }

                if (lemmaIndexCroppedText.size() == 0 && a == 0) {
                    lemmaIndexCroppedText.addAll(lemmaEngine.findLemmaIndexInText(finishText, lemmaOne));
                    if (lemmaIndexCroppedText.size() > 1) {
                        lemmaIndexCroppedText.clear();
                    }
                    continue;
                }
            }
        } catch (IOException e) {
            new CurrentIOException(e.getMessage());
        }
    }

    private Map<PageModel, Float> getRelevanceFromPage(List<PageModel> pageList,
                                                       List<IndexModel> indexList) {
        Map<PageModel, Float> relevanceMap = new HashMap<>();
        pageListBulkhead(pageList, indexList, relevanceMap);

        Map<PageModel, Float> allRelevanceMap = new HashMap<>();

        relevanceMap.keySet().forEach(page -> {
            float relevance = relevanceMap.get(page) / Collections.max(relevanceMap.values());
            allRelevanceMap.put(page, relevance);
        });

        List<Map.Entry<PageModel, Float>> sortList = new ArrayList<>(allRelevanceMap.entrySet());
        sortList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        Map<PageModel, Float> map = new ConcurrentHashMap<>();
        Entry<PageModel, Float> pageModelFloatEntry;

        for (int k = 0; k < sortList.size(); k++) {
            pageModelFloatEntry = sortList.get(k);
            map.putIfAbsent(pageModelFloatEntry.getKey(), pageModelFloatEntry.getValue());
        }
        return map;
    }

    @Override
    public Map<PageModel, Float> pageListBulkhead(List<PageModel> pageList, List<IndexModel> indexList,
                                                  Map<PageModel, Float> relevanceMap) {
        for (int i = 0, j = 0; i < pageList.size(); ) {
            PageModel page = pageList.get(i);
            float relevance = 0;
            while (j < indexList.size()) {
                IndexModel index = indexList.get(j);
                if (index.getPage() == page) {
                    relevance += index.getRank();
                }
                j++;
            }
            relevanceMap.put(page, relevance);
            i++;
        }
        return relevanceMap;
    }

    @Override
    public List<LemmaModel> getLemmaModelFromSite(List<String> lemmas, SiteModel site) {
        lemmaRepository.flush();
        List<LemmaModel> lemmaModels = lemmaRepository.findLemmaListBySite(lemmas, site);
        List<LemmaModel> result = new ArrayList<>(lemmaModels);
        result.sort(Comparator.comparingInt(LemmaModel::getFrequency));
        return result;
    }

    @Override
    public List<String> getLemmaFromSearchText(String text) {
        String[] words = text.toLowerCase(Locale.ROOT).split(" ");
        List<String> lemmaList = new ArrayList<>();
        int i = 0;
        List<String> list;
        while (i < words.length) {
            String lemma = words[i];
            try {
                list = lemmaEngine.getLemma(lemma);
                lemmaList.addAll(list);
            } catch (IOException e) {
                new CurrentIOException(e.getMessage());
            }
            i++;
        }
        return lemmaList;
    }

    @Override
    public List<SearchDto> createSearchDtoList(List<LemmaModel> lemmaList,
                                               List<String> textLemmaList,
                                               int start, int limit) {
        List<SearchDto> result = new ArrayList<>();
        pageRepository.flush();
        if (lemmaList.size() >= textLemmaList.size()) {

            List<PageModel> pagesList = pageRepository.findByLemmaList(lemmaList);
            indexRepository.flush();
            List<IndexModel> indexesList = indexRepository.findByPageAndLemmas(lemmaList, pagesList);
            Map<PageModel, Float> relevanceMap = getRelevanceFromPage(pagesList, indexesList);
            List<SearchDto> searchDtos = getSearchDtoList((ConcurrentHashMap<PageModel, Float>) relevanceMap, textLemmaList);

            if (start > searchDtos.size()) {
                return new ArrayList<>();
            }
            if (searchDtos.size() > limit) {
                resultAdded(result, searchDtos, start, limit);
                return result;
            } else return searchDtos;

        } else return result;
    }

    @Override
    public String clearCodeFromTag(String text, String element) {
        Document doc = Jsoup.parse(text);
        Elements elements = doc.select(element);
        String html = elements.stream().map(Element::html).collect(Collectors.joining());
        return Jsoup.parse(html).text();
    }

    @Override
    public ResultDTO search(String query, String site, int offset,
                            SiteRepository siteRepository, SearchStarter searchStarter) {

        String lowerCase = query.toLowerCase();
        List<SearchDto> searchData;
        if (!site.isEmpty()) {
            if (siteRepository.findByUrl(site) == null) {

                return new ResultDTO(false, "Данная страница находится за пределами сайтов,\n" +
                        "указанных в конфигурационном файле", HttpStatus.BAD_REQUEST);
            } else {
                searchData = searchStarter.getSearchFromOneSite(lowerCase, site, offset, 20);
            }
        } else {
            searchData = searchStarter.getFullSearch(lowerCase, offset, 20);
        }
        return new ResultDTO(true, searchData.size(), searchData, HttpStatus.OK);
    }

    private List<SearchDto> resultAdded(List<SearchDto> result, List<SearchDto> searchDtos, int start, int limit) {
        for (int i = start; i < limit; i++) {
            result.add(searchDtos.get(i));
        }
        return result;
    }

}
