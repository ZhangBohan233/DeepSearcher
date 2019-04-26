package trashsoftware.deepSearcher.itemUnits;

public abstract class Cell implements Comparable<Cell> {

    private String name1;

    private String name2;


    /**
     * The constructor of an abstract class Cell.
     *
     * @param name1 the first name.
     * @param name2 the second name.
     */
    Cell(String name1, String name2) {
        this.name1 = name1;
        this.name2 = name2;
    }


    /**
     * Returns the first name.
     *
     * @return first name.
     */
    public String getName1() {
        return name1;
    }


    /**
     * Returns the second name.
     *
     * @return second name.
     */
    public String getName2() {
        return name2;
    }


    @Override
    public int compareTo(Cell target) {
        String targetName = target.getName1();
        for (int i = 0; i < Math.min(targetName.length(), name1.length()); i++) {
            if (Character.compare(name1.charAt(i), targetName.charAt(i)) > 0) {
                return 1;
            } else if (Character.compare(name1.charAt(i), targetName.charAt(i)) < 0) {
                return -1;
            }
        }
        return 0;
    }
}
