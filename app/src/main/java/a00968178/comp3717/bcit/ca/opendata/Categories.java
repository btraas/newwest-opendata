package a00968178.comp3717.bcit.ca.opendata;

import java.util.ArrayList;

/**
 * Created by Brayden on 2/2/2017.
 */

public abstract class Categories {
    private static ArrayList<String> all = new ArrayList<String>();
    static {
        // examples
        all.add("Business and Economy");
        all.add("City Government");
        all.add("Commnuity");
        all.add("Electrical");
    }

    public static String[] getNames() {
        return all.toArray(new String[all.size()]);
    }
}
