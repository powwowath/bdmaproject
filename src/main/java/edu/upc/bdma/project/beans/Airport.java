package edu.upc.bdma.project.beans;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Gerard
 */
public class Airport {
    private long id;
    private String name;
    private String city;
    private String country;
    private String region;
    private String code;
    private Double lon;
    private Double lat;
    private long nCultural;
    private long nBeach;
    private long nMountain;
    private long nTourist;
    private long nNightlife;
    private Set<String> routes = new HashSet<String>();

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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
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

    public Set<String> getRoutes() {
        return routes;
    }

    public void setRoutes(Set<String> routes) {
        this.routes = routes;
    }


}
