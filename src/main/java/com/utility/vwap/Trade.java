package com.utility.vwap;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "trade", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"currencyPair", "timeStamp"})
})
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double exchangeRate;
    private Integer volume;
    private String currencyPair;
    private LocalTime timeStamp;

    Trade() {

    }

    Trade(String timeStr, String currencyPair, double exchangeRate, int volume) {
        this.timeStamp = LocalTime.parse(timeStr.toLowerCase(), Util.TIME_FORMATTER);
        this.currencyPair = currencyPair;
        this.exchangeRate = exchangeRate;
        this.volume = volume;
    }


    public LocalTime getTimeStamp() {
        return timeStamp;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public Double getExchangeRate() {
        return exchangeRate;
    }

    public Integer getVolume() {
        return volume;
    }
}
