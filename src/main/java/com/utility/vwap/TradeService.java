package com.utility.vwap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class TradeService {
    private static final Logger logger = LogManager.getLogger(TradeService.class);
    @Autowired
    Util util;

    @Autowired
    TradeRepository tradeRepository;

    public Map<String, List<VWAPWindowPeriod>> inputTradesCalculator(List<Trade> tradesList) {
        Map<String, Map<Integer, List<Trade>>> groupedTrades = tradesList.stream()
                .collect(Collectors.groupingBy(Trade::getCurrencyPair,
                        Collectors.groupingBy(util.getHourlySession, TreeMap::new, Collectors.toList())));

        Map<String, List<VWAPWindowPeriod>> outputData = new HashMap<>();
        groupedTrades.forEach((currencyPair, sessionTradesSet) -> {
            List<VWAPWindowPeriod> vwaps = new ArrayList<>();
            logger.info("Currency Pair: %s".formatted(currencyPair));
            sessionTradesSet.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        logger.info(String.format("  %s during: %s to %s", util.getHourlySession.getName(), entry.getKey(), entry.getKey() + 1));
                        VWAPWindowPeriod vwap = new VWAPWindowPeriod();
                        vwap.setSession(String.format("  %s during: %s to %s", util.getHourlySession.getName(), entry.getKey(), entry.getKey() + 1));
                        vwap.setVwap(calculate(entry.getValue()));
                        vwaps.add(vwap);
                        logger.info("");
                    });
            outputData.put(currencyPair, vwaps);
        });
        return outputData;
    }

    public Double calculate(List<Trade> sessionTrades) {
        double totalVolumePrice = sessionTrades.stream().mapToDouble(trade -> trade.getExchangeRate() * trade.getVolume()).sum();
        int totalVolume = sessionTrades.stream().mapToInt(Trade::getVolume).sum();
        Double vwap = totalVolumePrice / totalVolume;
        logger.info("  VWAP is    : %s".formatted(vwap));
        return vwap;
    }

    @Transactional(rollbackFor = UnexpectedRollbackException.class)
    public void save(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<Trade> trades = reader.lines().skip(1)
                    .map(line -> line.split(","))
                    .map(data -> new Trade(data[0], data[1], Double.parseDouble(data[2]), Integer.parseInt(data[3].replace(",", ""))))
                    .collect(Collectors.toList());
            tradeRepository.saveAll(trades);
        } catch (UnexpectedRollbackException e) {
            logger.error("UnexpectedRollbackException occurred while save(...) on uploaded Trades information.", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }
}