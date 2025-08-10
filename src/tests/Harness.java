package tests;

import FileManager.TableEngine;
import Util.Logger;

import java.nio.file.Path;
import java.util.*;

public class Harness {
    public static void main(String[] args) throws Exception {
        Logger.setConsoleLevel(Logger.Level.WARN); // keep console quiet during harness
        Logger.setFileLevel(Logger.Level.INFO);

        // Prepare two CSVs under out/test to avoid touching storage
        Path base = Path.of("out", "test");
        java.nio.file.Files.createDirectories(base);
        Path t1 = base.resolve("people.csv");
        Path t2 = base.resolve("cities.csv");

        // Seed people
        java.nio.file.Files.writeString(t1, String.join("\n",
                "id,name,city_id,age",
                "1,Alice,10,30",
                "2,Bob,10,25",
                "3,Charlie,20,35"
        ));
        // Seed cities
        java.nio.file.Files.writeString(t2, String.join("\n",
                "id,city",
                "10,Paris",
                "20,London"
        ));

        // Insert a row
        Map<String,String> newRow = new LinkedHashMap<>();
        newRow.put("id","4"); newRow.put("name","Diana"); newRow.put("city_id","20"); newRow.put("age","28");
        TableEngine.insert(t1, newRow);

        // Update rows age where city_id=10
        int updated = TableEngine.update(t1, r -> Objects.equals(r.get("city_id"), "10"), Map.of("age","26"));
        System.out.println("Updated=" + updated);

        // Select with filter and sort
        List<Map<String,String>> filtered = TableEngine.select(t1, r -> Integer.parseInt(r.get("age")) >= 28, List.of("age","name"));
        System.out.println("Select>=28 size=" + filtered.size());

        // Join people.city_id = cities.id
        TableEngine.Table left = TableEngine.load(t1);
        TableEngine.Table right = TableEngine.load(t2);
        List<Map<String,String>> joined = TableEngine.join(left, right, "city_id", "id");
        System.out.println("Joined size=" + joined.size());

        // Delete where name=Bob
        int deleted = TableEngine.delete(t1, r -> Objects.equals(r.get("name"), "Bob"));
        System.out.println("Deleted=" + deleted);

        // Final select all sorted by id
        List<Map<String,String>> all = TableEngine.select(t1, null, List.of("id"));
        System.out.println("Final rows=" + all.size());
    }
}
