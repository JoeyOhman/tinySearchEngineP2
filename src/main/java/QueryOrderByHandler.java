import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class QueryOrderByHandler {

    public static void handleQueryKeyWords(String property, String direction, LinkedList<DocumentProperties> list) throws Exception {

        Comparator<DocumentProperties> c;
        boolean ascending;

        if (property.equalsIgnoreCase("count")) {
            c = DocumentProperties.BY_COUNT;
        } else if (property.equalsIgnoreCase("popularity")) {
            c = DocumentProperties.BY_POPULARITY;
        } else {
            //handleQueryError();
            throw new Exception("False query");
        }

        if (direction.equalsIgnoreCase("asc")) {
            ascending = true;
        } else if (direction.equalsIgnoreCase("desc")) {
            ascending = false;
        } else {
            //handleQueryError();
            throw new Exception("False query");
        }

        if(! ascending)
            c = Collections.reverseOrder(c);
        Collections.sort(list, c);
    }

    public static void handleQueryError() {
        System.out.println(
                "Error, Query aborted: Query keywords format should be: orderby \"Property\" \"Direction\"\n" +
                        "Property: relevance, popularity\n" +
                        "Direction: asc, desc"
        );
    }

}
