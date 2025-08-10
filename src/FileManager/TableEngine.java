package FileManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import Util.Logger;

public class TableEngine {

	private static String joinCsv(List<String> cols, Map<String, String> row) {
		return cols.stream().map(c -> row.getOrDefault(c, "")).collect(Collectors.joining(","));
	}

	private static List<String> splitCsv(String line) {
		List<String> out = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		boolean inQuotes = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '"') inQuotes = !inQuotes; else if (c == ',' && !inQuotes) { out.add(sb.toString()); sb.setLength(0); } else sb.append(c);
		}
		out.add(sb.toString());
		return out;
	}

	public static class Table {
		public final List<String> columns;
		public final List<Map<String, String>> rows;
		public Table(List<String> columns, List<Map<String, String>> rows) { this.columns = columns; this.rows = rows; }
	}

	// --------- Paged table helpers (page_0.csv, page_1.csv, ...) ---------

	private static List<Path> listPages(Path tableDir) throws IOException {
		if (!Files.exists(tableDir)) return List.of();
		try (var stream = Files.list(tableDir)) {
			return stream
				.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().matches("page_\\d+\\.csv"))
				.sorted(Comparator.comparingInt(p -> Integer.parseInt(p.getFileName().toString().replaceAll("^page_(\\d+)\\.csv$", "$1"))))
				.collect(Collectors.toList());
		}
	}

	private static List<String> readHeader(Path csv) throws IOException {
		try (BufferedReader br = Files.newBufferedReader(csv)) {
			String header = br.readLine();
			if (header == null) throw new IOException("Empty page: " + csv);
			return Arrays.asList(header.split(","));
		}
	}

	private static Table loadSinglePage(Path csvPath) throws IOException {
		try (BufferedReader br = Files.newBufferedReader(csvPath)) {
			String header = br.readLine();
			if (header == null) throw new IOException("Empty table: " + csvPath);
			List<String> cols = Arrays.asList(header.split(","));
			List<Map<String, String>> rows = new ArrayList<>();
			String line;
			while ((line = br.readLine()) != null) {
				List<String> vals = splitCsv(line);
				Map<String, String> row = new LinkedHashMap<>();
				for (int i = 0; i < cols.size(); i++) row.put(cols.get(i), i < vals.size() ? vals.get(i) : "");
				rows.add(row);
			}
			return new Table(cols, rows);
		}
	}

	public static Table loadDir(Path tableDir) throws IOException {
		List<Path> pages = listPages(tableDir);
		if (pages.isEmpty()) throw new IOException("No pages in table dir: " + tableDir);
		List<String> header = readHeader(pages.get(0));
		List<Map<String,String>> all = new ArrayList<>();
		for (Path p : pages) {
			Table t = loadSinglePage(p);
			all.addAll(t.rows);
		}
		return new Table(header, all);
	}

	private static Path ensureLastPage(Path tableDir, List<String> header) throws IOException {
		Files.createDirectories(tableDir);
		List<Path> pages = listPages(tableDir);
		if (pages.isEmpty()) {
			Path first = tableDir.resolve("page_0.csv");
			save(first, new Table(header, new ArrayList<>()));
			return first;
		}
		return pages.get(pages.size()-1);
	}

	public static void insertDir(Path tableDir, Map<String,String> values, int pageSizeKB) throws IOException {
		List<Path> pages = listPages(tableDir);
		List<String> header;
		if (pages.isEmpty()) {
			// If no pages, infer header from values order
			header = new ArrayList<>(values.keySet());
		} else {
			header = readHeader(pages.get(0));
		}
		Path last = ensureLastPage(tableDir, header);
		Table t = loadSinglePage(last);
		Map<String,String> row = new LinkedHashMap<>();
		for (String c : t.columns) row.put(c, values.getOrDefault(c, ""));
		String rowLine = joinCsv(t.columns, row) + System.lineSeparator();
		long currentSize = Files.size(last);
		long limit = (long) pageSizeKB * 1024L;
		if (currentSize + rowLine.getBytes().length > limit) {
			// roll to next page
			int nextIndex = pages.isEmpty() ? 1 : Integer.parseInt(pages.get(pages.size()-1).getFileName().toString().replaceAll("^page_(\\d+)\\.csv$", "$1")) + 1;
			last = tableDir.resolve("page_" + nextIndex + ".csv");
			save(last, new Table(t.columns, new ArrayList<>()));
			t = loadSinglePage(last);
		}
		t.rows.add(row);
		save(last, t);
		Logger.info("Inserted into " + last + ": " + row);
	}

	public static int updateDir(Path tableDir, Predicate<Map<String, String>> where, Map<String,String> updates) throws IOException {
		List<Path> pages = listPages(tableDir);
		int count = 0;
		for (Path p : pages) {
			Table t = loadSinglePage(p);
			for (Map<String,String> row : t.rows) {
				if (where.test(row)) { for (var e : updates.entrySet()) if (t.columns.contains(e.getKey())) row.put(e.getKey(), e.getValue()); count++; }
			}
			save(p, t);
		}
		Logger.info("Updated " + count + " rows across " + pages.size() + " pages in " + tableDir);
		return count;
	}

	public static int deleteDir(Path tableDir, Predicate<Map<String,String>> where) throws IOException {
		List<Path> pages = listPages(tableDir);
		int count = 0;
		for (Path p : pages) {
			Table t = loadSinglePage(p);
			int before = t.rows.size();
			t.rows.removeIf(where);
			save(p, t);
			count += (before - t.rows.size());
		}
		Logger.info("Deleted " + count + " rows across " + pages.size() + " pages in " + tableDir);
		return count;
	}

	public static List<Map<String,String>> selectDir(Path tableDir, Predicate<Map<String,String>> where, List<String> orderByAsc) throws IOException {
		Table all = loadDir(tableDir);
		Predicate<Map<String, String>> pred = (where != null) ? where : new Predicate<Map<String, String>>() { public boolean test(Map<String, String> x) { return true; } };
		List<Map<String,String>> result = all.rows.stream().filter(pred).collect(Collectors.toList());
		if (orderByAsc != null && !orderByAsc.isEmpty()) {
			Comparator<Map<String,String>> cmp = null;
			for (String col : orderByAsc) {
				Comparator<Map<String,String>> c = Comparator.comparing(m -> m.getOrDefault(col, ""));
				cmp = (cmp == null) ? c : cmp.thenComparing(c);
			}
			if (cmp != null) result.sort(cmp);
		}
		Logger.info("Selected " + result.size() + " rows from " + tableDir + " across pages");
		return result;
	}

	// Load a table CSV: first line header, rest rows
	public static Table load(Path csvPath) throws IOException {
		if (!Files.exists(csvPath)) throw new IOException("Table file not found: " + csvPath);
		try (BufferedReader br = Files.newBufferedReader(csvPath)) {
			String header = br.readLine();
			if (header == null) throw new IOException("Empty table: " + csvPath);
			List<String> cols = Arrays.asList(header.split(","));
			List<Map<String, String>> rows = new ArrayList<>();
			String line;
			while ((line = br.readLine()) != null) {
				List<String> vals = splitCsv(line);
				Map<String, String> row = new LinkedHashMap<>();
				for (int i = 0; i < cols.size(); i++) {
					row.put(cols.get(i), i < vals.size() ? vals.get(i) : "");
				}
				rows.add(row);
			}
			return new Table(cols, rows);
		}
	}

	// Save a table back to CSV
	public static void save(Path csvPath, Table table) throws IOException {
		Files.createDirectories(csvPath.getParent());
		try (BufferedWriter bw = Files.newBufferedWriter(csvPath)) {
			bw.write(String.join(",", table.columns));
			bw.newLine();
			for (Map<String, String> row : table.rows) {
				bw.write(joinCsv(table.columns, row));
				bw.newLine();
			}
		}
	}

	// Insert
	public static void insert(Path csvPath, Map<String, String> values) throws IOException {
		Table t = load(csvPath);
		Map<String, String> row = new LinkedHashMap<>();
		for (String c : t.columns) row.put(c, values.getOrDefault(c, ""));
		t.rows.add(row);
		save(csvPath, t);
		Logger.info("Inserted into " + csvPath + ": " + row);
	}

	// Update with predicate
	public static int update(Path csvPath, Predicate<Map<String, String>> where, Map<String, String> updates) throws IOException {
		Table t = load(csvPath);
		int count = 0;
		for (Map<String, String> row : t.rows) {
			if (where.test(row)) {
				for (Map.Entry<String, String> e : updates.entrySet()) if (t.columns.contains(e.getKey())) row.put(e.getKey(), e.getValue());
				count++;
			}
		}
		save(csvPath, t);
		Logger.info("Updated " + count + " rows in " + csvPath);
		return count;
	}

	// Delete with predicate
	public static int delete(Path csvPath, Predicate<Map<String, String>> where) throws IOException {
		Table t = load(csvPath);
		int before = t.rows.size();
		t.rows.removeIf(where);
		save(csvPath, t);
		int count = before - t.rows.size();
		Logger.info("Deleted " + count + " rows from " + csvPath);
		return count;
	}

	// Filter + sort
	public static List<Map<String, String>> select(Path csvPath, Predicate<Map<String, String>> where, List<String> orderByAsc) throws IOException {
		Table t = load(csvPath);
	Predicate<Map<String, String>> pred = (where != null) ? where : new Predicate<Map<String, String>>() { public boolean test(Map<String, String> x) { return true; } };
	List<Map<String, String>> result = t.rows.stream().filter(pred).collect(Collectors.toList());
		if (orderByAsc != null && !orderByAsc.isEmpty()) {
			Comparator<Map<String, String>> cmp = null;
			for (String col : orderByAsc) {
				Comparator<Map<String, String>> c = Comparator.comparing(m -> m.getOrDefault(col, ""));
				cmp = (cmp == null) ? c : cmp.thenComparing(c);
			}
			if (cmp != null) result.sort(cmp);
		}
		Logger.info("Selected " + result.size() + " rows from " + csvPath);
		return result;
	}

	// Nested-loop join on equality of column names colLeft and colRight
	public static List<Map<String, String>> join(Table left, Table right, String colLeft, String colRight) {
		List<Map<String, String>> out = new ArrayList<>();
		for (Map<String, String> l : left.rows) {
			for (Map<String, String> r : right.rows) {
				if (Objects.equals(l.get(colLeft), r.get(colRight))) {
					Map<String, String> merged = new LinkedHashMap<>();
					left.columns.forEach(c -> merged.put("left." + c, l.get(c)));
					right.columns.forEach(c -> merged.put("right." + c, r.get(c)));
					out.add(merged);
				}
			}
		}
		Logger.info("Join produced " + out.size() + " rows");
		return out;
	}
}
