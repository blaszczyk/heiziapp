package heizi.heizi.data;

public class DataRange {

    private int[][] tag;

    private int[][] ty;

    private int[][] po;

    private int[][] pu;

    private int[][] owm;

    private int[] tur;

    public int[][] getTag() {
        return tag;
    }

    public int[][] getTy() {
        return ty;
    }

    public int[][] getPo() {
        return po;
    }

    public int[][] getPu() {
        return pu;
    }

    public int[][] getOwm() {
        return owm;
    }

    public int[] getTur() {
        return tur;
    }

    public void setTag(int[][] tag) {
        this.tag = tag;
    }

    public void setTy(int[][] ty) {
        this.ty = ty;
    }

    public void setPo(int[][] po) {
        this.po = po;
    }

    public void setPu(int[][] pu) {
        this.pu = pu;
    }

    public void setOwm(int[][] owm) {
        this.owm = owm;
    }

    public void setTur(int[] tur) {
        this.tur = tur;
    }
}
