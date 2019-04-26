package trashsoftware.deepSearcher.itemUnits;

import java.util.ArrayList;
import java.util.Arrays;

public class TemporarySearchUnit {

    private String name;

    private String[] allMarks;

    private ArrayList<Integer> unSatisfiesIndices = new ArrayList<>();

    public static void main(String[] args) {
        TemporarySearchUnit tsu = new TemporarySearchUnit("abcdefghhhdd", new String[]{"abc", "dd", "hhh"});
        System.out.println(tsu.isSatisfied());
        System.out.println(Arrays.toString(tsu.getUnSatisfies()));
    }


    /**
     * Constructor of a TemporarySearchUnit.
     *
     * @param nameOrText the String object to be searched.
     * @param allMarks   all targets to search inside the name.
     */
    public TemporarySearchUnit(String nameOrText, String[] allMarks) {
        this.name = nameOrText;
        this.allMarks = allMarks;
        markUnSatIndices();
    }


    /**
     * Returns whether all targets are found in the String "nameOrText".
     *
     * @return true iff all targets are found.
     */
    public boolean isSatisfied() {
        return unSatisfiesIndices.size() == 0;
    }


    /**
     * Returns an String Array object containing all targets that were not found in "nameOrText".
     *
     * @return all unmatched targets.
     */
    public String[] getUnSatisfies() {
        String[] uns = new String[unSatisfiesIndices.size()];
        for (int i = 0; i < uns.length; i++) {
            uns[i] = allMarks[unSatisfiesIndices.get(i)];
        }
        return uns;
    }


    /**
     * Generates satisfied and un-satisfied items.
     */
    private void markUnSatIndices() {
        for (int i = 0; i < allMarks.length; i++) {
            if (!name.contains(allMarks[i])) {
                unSatisfiesIndices.add(i);
            }
        }
    }
}
