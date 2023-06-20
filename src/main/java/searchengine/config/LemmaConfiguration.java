package searchengine.config;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class LemmaConfiguration {

    public LuceneMorphology russianLuceneMorphology() throws IOException {
        return new RussianLuceneMorphology();
    }

    public LuceneMorphology englishLuceneMorphology() throws IOException {
        return new EnglishLuceneMorphology();
    }
}
