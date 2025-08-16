package com.example;

import java.time.LocalDate;

public class CottonPrice {
    private LocalDate date;
    private String cottonType;
    private double price;

    public CottonPrice(LocalDate date, String cottonType, double price) {
        this.date = date;
        this.cottonType = cottonType;
        this.price = price;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCottonType() {
        return cottonType;
    }

    public void setCottonType(String cottonType) {
        this.cottonType = cottonType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "CottonPrice{" +
                "date=" + date +
                ", cottonType='" + cottonType + '\'' +
                ", price=" + price +
                '}';
    }
}
