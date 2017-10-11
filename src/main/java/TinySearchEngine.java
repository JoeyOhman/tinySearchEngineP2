import se.kth.id1020.TinySearchEngineBase;
import se.kth.id1020.util.Attributes;
import se.kth.id1020.util.Document;
import se.kth.id1020.util.Sentence;
import se.kth.id1020.util.Word;

import java.util.HashMap;
import java.util.List;

public class TinySearchEngine implements TinySearchEngineBase {

    private HashMap<Word, List<DocumentProperties>> words;

    public  TinySearchEngine() {
        words = new HashMap<Word, List<DocumentProperties>>();
    }

    public void preInserts() {

    }

    public void insert(Sentence sentence, Attributes attributes) {

    }

    public void postInserts() {

    }

    public List<Document> search(String s) {
        return null;
    }

    public String infix(String s) {
        return null;
    }
}
