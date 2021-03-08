package heizi.heizi.data;

public class DataSet {

    private int tag;

    private int ty;

    private int po;

    private int pu;

    private double dtag;

    private double dty;

    private double dpo;

    private double dpu;

    private int time;

    private int tur;

    public DataSet() { }

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

    public double getDtag() {
        return dtag;
    }

    public double getDty() {
        return dty;
    }

    public double getDpo() {
        return dpo;
    }

    public double getDpu() {
        return dpu;
    }

    public int getTime() {
        return time;
    }

    public int getTur() {
        return tur;
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

    public void setDtag(double dtag) {
        this.dtag = dtag;
    }

    public void setDty(double dty) {
        this.dty = dty;
    }

    public void setDpo(double dpo) {
        this.dpo = dpo;
    }

    public void setDpu(double dpu) {
        this.dpu = dpu;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setTur(int tur) {
        this.tur = tur;
    }
}
