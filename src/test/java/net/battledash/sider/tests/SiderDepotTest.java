package net.battledash.sider.tests;

import net.battledash.sider.Sider;
import net.battledash.sider.depot.SiderDepot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SiderDepotTest {

    private SiderDepot<Integer> depot;

    @BeforeEach
    public void setup() {
        Sider sider = new Sider(System.getenv("REDIS_HOST"), Integer.parseInt(System.getenv("REDIS_PORT")));
        depot = sider.createDepot(Integer.class, "sider_test");
    }

    @Test
    public void testGetSet() {
        depot.put("test1", 1);

        assertEquals(1, depot.get("test1").intValue());
    }

    @Test
    public void testSize() {
        depot.put("test1", 1);
        depot.put("test2", 2);
        depot.put("test3", 3);

        assertEquals(3, depot.size());
    }

    @Test
    public void testRemove() {
        depot.put("test1", 1);
        depot.put("test2", 2);
        depot.put("test3", 3);

        depot.remove("test2");

        assertEquals(2, depot.size());
    }

    @Test
    public void testClear() {
        depot.put("test1", 1);
        depot.put("test2", 2);
        depot.put("test3", 3);

        depot.clear();

        assertEquals(0, depot.size());
    }

}
