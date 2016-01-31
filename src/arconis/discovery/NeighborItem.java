package arconis.discovery;

/**
 * Created by ww on 2016-01-30.
 */
public class NeighborItem {
    private int id;
    private int hops;
    private int offset;
    private String dutycycle;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * @return the hops
     */
    public int getHops() {
        return hops;
    }
    /**
     * @param hops the hops to set
     */
    public void setHops(int hops) {
        this.hops = hops;
    }

    /**
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }
    /**
     * @param offset the offset to set
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * @return the dutycycle
     */
    public String getDutycycle() {
        return dutycycle;
    }


    /**
     * @return the dutycycles
     */
    public int[] getDutycycles() {
        int dutycycles[] = new int[2];
        String[] temp;
        temp = dutycycle.split(":");
        for(int i =0; i < temp.length ; i++){
            dutycycles[i]= Integer.parseInt(temp[i]);
        }
        return dutycycles;
    }
    /**
     * @param dutycycle the dutycycle to set
     */
    public void setDutycycle(String dutycycle) {
        this.dutycycle = dutycycle;
    }



    public NeighborItem(int id, int hops, int offset, String dutycycle) {
        this.id = id;
        this.hops = hops;
        this.offset = offset;
        this.dutycycle = dutycycle;
    }

    @Override
    public String toString(){
        return this.id + ":" + this.hops + ":" + this.offset + ":" + this.dutycycle;
    }
}
