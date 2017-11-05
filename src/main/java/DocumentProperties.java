import se.kth.id1020.util.Attributes;
import se.kth.id1020.util.Document;

import java.util.Comparator;

public class DocumentProperties {

    //public static final Comparator<DocumentProperties> BY_COUNT = new ByCount();
    public static final Comparator<DocumentProperties> BY_POPULARITY = new ByPopularity();
    public static final Comparator<DocumentProperties> BY_RELEVANCE = new ByRelevance();


    private final Attributes attributes;
    public int count = 1; // Only created when word occurred
    private int searchIdChangedCombinedTfidf = 0;
    private double tfidf, combinedTfidf;

    public DocumentProperties(Attributes attributes) {
        this.attributes = attributes;
    }

    public Document getDocument() {
        return attributes.document;
    }

    public int getPopularity() {
        return attributes.document.popularity;
    }

    public int getOccurrence() {
        return attributes.occurrence;
    }

    public void setTfidf(double tfidf) {
        this.tfidf = tfidf;
        this.combinedTfidf = tfidf; // Not very nice solution
    }

    double getTfidf() {
        return tfidf;
    }

    void setCombinedTfidf(double combinedTfidf, int searchId) {
        this.combinedTfidf = combinedTfidf;
        searchIdChangedCombinedTfidf = searchId;
    }

    double getCombinedTfidf(int searchId) {
        if (searchId != searchIdChangedCombinedTfidf) {
            combinedTfidf = tfidf;
            searchIdChangedCombinedTfidf = searchId;
        }
        return combinedTfidf;
    }

    private static class ByRelevance implements Comparator<DocumentProperties> {

        public int compare(DocumentProperties docP1, DocumentProperties docP2) {
            //System.out.println("docP1 relevance: " + docP1.tfidf + " docP2 relevance: " + docP2.tfidf);
            return Double.compare(docP1.combinedTfidf, docP2.combinedTfidf);
            //return (int)(docP1.tfidf - docP2.tfidf);
        }
    }

    private static class ByPopularity implements Comparator<DocumentProperties> {

        public int compare(DocumentProperties docP1, DocumentProperties docP2) {
            return docP1.getPopularity() - docP2.getPopularity();
        }
    }

}
