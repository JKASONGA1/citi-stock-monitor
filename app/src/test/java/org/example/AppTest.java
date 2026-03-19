package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void stockRecordStoresValues() {
        java.math.BigDecimal price = new java.math.BigDecimal("42750.50");
        App.StockRecord record = new App.StockRecord(1, "09:30:00", price);

        assertEquals(1, record.tick);
        assertEquals("09:30:00", record.timestamp);
        assertEquals(price, record.price);
    }
}
