import se.kth.id1020.util.Attributes;
import se.kth.id1020.util.Document;

import java.util.Comparator;

public class DocumentProperties {

    //public static final Comparator<DocumentProperties> BY_COUNT = new ByCount();
    public static final Comparator<DocumentProperties> BY_POPULARITY = new ByPopularity();
    public static final Comparator<DocumentProperties> BY_RELEVANCE = new ByRelevance();


    private final Attributes attributes;
    public int count = 1; // Only created when word occurred
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
    }

    double getTfidf() {
        return tfidf;
    }

    void setCombinedTfidf(double combinedTfidf) {
        this.combinedTfidf = combinedTfidf;
    }

    double getCombinedTfidf() {
        return combinedTfidf;
    }

    private static class ByRelevance implements Comparator<DocumentProperties> {

        public int compare(DocumentProperties docP1, DocumentProperties docP2) {
            //System.out.println("docP1 relevance: " + docP1.tfidf + " docP2 relevance: " + docP2.tfidf);
            return Double.compare(docP1.tfidf, docP2.tfidf);
            //return (int)(docP1.tfidf - docP2.tfidf);
        }
    }

    private static class ByPopularity implements Comparator<DocumentProperties> {

        public int compare(DocumentProperties docP1, DocumentProperties docP2) {
            return docP1.getPopularity() - docP2.getPopularity();
        }
    }

}
