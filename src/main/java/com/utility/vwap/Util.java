package com.utility.vwap;


import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
class Util {
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    NamedFunction<Trade, Integer> getHourlySession = new NamedFunction<>() {
        @Override
        public Integer apply(Trade t) {
            return getHour(t);
        }

        @Override
        public String getName() {
            return "Hour";
        }
    };

    public <T extends Trade> int getHour(T obj) {
        return obj.getTimeStamp().getHour();
    }
}