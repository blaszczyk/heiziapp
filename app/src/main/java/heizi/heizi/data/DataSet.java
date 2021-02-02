package heizi.heizi.data;

public class DataSet {

    private int tag;

    private int ty;

    private int po;

    private  int pu;

    private int time;

    public DataSet() { }

    public DataSet(int tag, int ty, int po, int pu, int time) {
        this.tag = tag;
        this.ty = ty;
        this.po = po;
        this.pu = pu;
        this.time = time;
    }

    public int getTag() {
        return tag;
    }

    public int getTy() {
        return ty;
    }

    public int getPo() {
        return po;
    }

    public int getPu() {
        return pu;
    }

    public int getTime() {
        return time;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public void setTy(int ty) {
        this.ty = ty;
    }

    public void setPo(int po) {
        this.po = po;
    }

    public void setPu(int pu) {
        this.pu = pu;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
