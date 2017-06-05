package edu.upc.bdma.project.beans;

/**
 * Created by Gerard on 09/05/2017.
 */
public class City {
    private long id;
    private String name;
    private String country;
    private long nCultural;
    private long nBeach;
    private long nMountain;
    private long nTourist;
    private long nNightlife;

    private long pctCultural;
    private long pctBeach;
    private long pctMountain;
    private long pctTourist;
    private long pctNightlife;

    private long pctYoung;
    private long pctRelax;

    private String[] airportCodes;

    private int cost; // 0-Low, 1-Regular, 2-High
    private int promo; // 0-100%


    public void City(){
        nCultural = 0;
        nBeach = 0;
        nMountain = 0;
        nTourist = 0;
        nNightlife = 0;

        cost = 1;
        promo = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPctCultural() {
        return pctCultural;
    }

    public void setPctCultural(long pctCultural) {
        this.pctCultural = pctCultural;
    }

    public long getPctBeach() {
        return pctBeach;
    }

    public void setPctBeach(long pctBeach) {
        this.pctBeach = pctBeach;
    }

    public long getPctMountain() {
        return pctMountain;
    }

    public void setPctMountain(long pctMountain) {
        this.pctMountain = pctMountain;
    }

    public long getPctTourist() {
        return pctTourist;
    }

    public void setPctTourist(long pctTourist) {
        this.pctTourist = pctTourist;
    }

    public long getPctNightlife() {
        return pctNightlife;
    }

    public void setPctNightlife(long pctNightlife) {
        this.pctNightlife = pctNightlife;
    }

    public String[] getAirportCodes() {
        return airportCodes;
    }

    public void setAirportCodes(String[] airportCodes) {
        this.airportCodes = airportCodes;
    }

    public long getnCultural() {
        return nCultural;
    }

    public void setnCultural(long nCultural) {
        this.nCultural = nCultural;
    }

    public long getnBeach() {
        return nBeach;
    }

    public void setnBeach(long nBeach) {
        this.nBeach = nBeach;
    }

    public long getnMountain() {
        return nMountain;
    }

    public void setnMountain(long nMountain) {
        this.nMountain = nMountain;
    }

    public long getnTourist() {
        return nTourist;
    }

    public void setnTourist(long nTourist) {
        this.nTourist = nTourist;
    }

    public long getnNightlife() {
        return nNightlife;
    }

    public void setnNightlife(long nNightlife) {
        this.nNightlife = nNightlife;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getPromo() {
        return promo;
    }

    public void setPromo(int promo) {
        this.promo = promo;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public long getPctYoung() {
        return pctYoung;
    }

    public void setPctYoung(long pctYoung) {
        this.pctYoung = pctYoung;
    }

    public long getPctRelax() {
        return pctRelax;
    }

    public void setPctRelax(long pctRelax) {
        this.pctRelax = pctRelax;
    }
}