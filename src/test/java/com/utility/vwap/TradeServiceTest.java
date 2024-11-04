package com.utility.vwap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private Util util;

    @InjectMocks
    private TradeService tradeService;
    private List<Trade> trades;

    @BeforeEach
    void setUp() {
        trades = Arrays.asList(
                new Trade("10:00 AM", "USD/EUR", 1.5, 1000),
                new Trade("10:30 AM", "USD/EUR", 1.6, 2000)
        );
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInputTradesCalculator() {
        // Mocking the property of Util
        NamedFunction<Trade, Integer> mockedFunction = mock(NamedFunction.class);
        util.getHourlySession = mockedFunction;
        when(mockedFunction.apply(any(Trade.class))).thenReturn(10);
        when(mockedFunction.getName()).thenReturn("Hour");
        Map<String, List<VWAPWindowPeriod>> result = tradeService.inputTradesCalculator(trades);
        assertEquals(1, result.get("USD/EUR").size());
        assertEquals(1.5666666666666667, result.get("USD/EUR").get(0).getVwap());
    }

    @Test
    void testCalculate() {
        Double vwap = tradeService.calculate(trades);
        assertEquals(1.5666666666666667, vwap);
    }

    @Test
    void testSave() throws Exception {
        String data = "timeStamp,currencyPair,exchangeRate,volume\n" +
                "10:00 AM,USD/EUR,1.5,1000\n" +
                "10:30 AM,USD/EUR,1.6,2000";
        InputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(inputStream);

        tradeService.save(file);

        verify(tradeRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testGetAllTrades() {
        when(tradeRepository.findAll()).thenReturn(trades);

        List<Trade> result = tradeService.getAllTrades();

        assertEquals(2, result.size());
        assertEquals("USD/EUR", result.get(0).getCurrencyPair());
    }
}
