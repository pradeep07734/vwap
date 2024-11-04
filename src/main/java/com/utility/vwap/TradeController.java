package com.utility.vwap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TradeController {
    @Autowired
    private TradeService tradeService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCSV(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a CSV file!");
        }

        tradeService.save(file);
        return ResponseEntity.ok("File uploaded and data saved successfully!");
    }

    @PostMapping(value = "/vwapcalculator", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Map<String, List<VWAPWindowPeriod>>>> calculateVWAP() {
        List<Trade> trades = tradeService.getAllTrades();
        Map<String, Map<String, List<VWAPWindowPeriod>>> result = new HashMap<>();
        result.put("data", tradeService.inputTradesCalculator(trades));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/trades")
    public ResponseEntity<List<Trade>> getAllTrades() {
        List<Trade> trades = tradeService.getAllTrades();
        return ResponseEntity.ok(trades);
    }
}
