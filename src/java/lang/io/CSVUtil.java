package lang.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Function;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import org.apache.commons.lang3.time.StopWatch;

import eval.EvaluationContext;
import eval.Relation2;
import eval.Tuple;
import lang.Compiler;
import lang.ast.IntegerType;
import lang.ast.PredicateRefType;
import lang.ast.PredicateType;
import lang.ast.StringType;

public class CSVUtil {

	public static <K, V> void readMap(Map<K, V> m, Function<String, K> keyParser, Function<String, V> valueParser, String path) throws IOException {
		CSVParser parser = new CSVParserBuilder().withSeparator(Compiler.getCSVSeparator()).build();
		CSVReader reader = new CSVReaderBuilder(new FileReader(new File(path))).withCSVParser(parser).build();

		String[] line;
		while ((line = reader.readNext()) != null) {
			K key = keyParser.apply(line[0]);
			V val = valueParser.apply(line[1]);
			m.put(key, val);
		}

		reader.close();
	}

	public static <K, V> void writeMap(Map<K, V> m, String path) throws IOException {
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(path)),
										 Compiler.getCSVSeparator(),
										 CSVWriter.NO_QUOTE_CHARACTER,
										 CSVWriter.DEFAULT_ESCAPE_CHARACTER,
										 CSVWriter.RFC4180_LINE_END);
		String[] line = new String[2];
		for (Map.Entry<K, V> entry : m.entrySet()) {
			line[0] = entry.getKey().toString();
			line[1] = entry.getValue().toString();
			writer.writeNext(line);
		}
		writer.close();
	}


	public static void readRelation(EvaluationContext ctx, PredicateType t, Relation2 rel, String path) throws IOException {
		assert t.arity() == rel.arity();

		SimpleLogger.logger().log("Read file " + path, SimpleLogger.LogLevel.Level.DEBUG);

		CSVParser parser = new CSVParserBuilder().withSeparator(Compiler.getCSVSeparator()).build();
		CSVReader reader = new CSVReaderBuilder(new FileReader(new File(path))).withCSVParser(parser).build();

		String[] line;
		while ((line = reader.readNext()) != null) {
			if (line.length != rel.arity()) {
				SimpleLogger
					.logger().error(
									"Relation " + rel.getName() + " has arity " + rel.arity() + " but got "
									+ line.length + "-sized tuple in .csv file");
				throw new RuntimeException("Error while loading relation from file " + path);
			}

			Tuple tup = new Tuple(rel.arity());
			for (int i = 0; i < line.length; ++i) {
				if (t.get(i).storageType() == IntegerType.get()) {
					tup.set(i, Long.parseLong(line[i]));
				} else if (t.get(i).storageType() == StringType.get()) {
					tup.set(i, ctx.internalizeString(line[i]));
				} else {
					assert t.get(i).storageType() == PredicateRefType.get();
					if (line[i].isEmpty() || line[i].charAt(0) != '\'') {
						throw new RuntimeException("Invalid CSV entry for PredicateRefType");
					}
					tup.set(i, ctx.internalizeString(line[i].substring(1)));
				}
			}

			rel.insert(tup);
		}

		SimpleLogger.logger().log("End Read file " + path + " into " + rel.getName(), SimpleLogger.LogLevel.Level.DEBUG);
	}

	public static void writeRelation(EvaluationContext ctx, PredicateType t, Relation2 rel, String path) throws IOException {
		StopWatch timer = StopWatch.createStarted();

		File fpath = new File(path);
		if (!fpath.exists()) {
			File parent = fpath.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
		}

		CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(path)),
										 Compiler.getCSVSeparator(),
										 CSVWriter.NO_QUOTE_CHARACTER,
										 CSVWriter.DEFAULT_ESCAPE_CHARACTER,
										 CSVWriter.RFC4180_LINE_END);

		SimpleLogger.logger().log("Write Relation: " + path, SimpleLogger.LogLevel.Level.DEBUG);

		String[] line = new String[t.arity()];
		for (Tuple tup : rel.tuples()) {
			for (int i = 0; i < t.arity(); ++i) {
				if (t.get(i).storageType() == IntegerType.get()) {
					line[i] = Long.toString(tup.get(i));
				} else if (t.get(i).storageType() == StringType.get()) {
					line[i] = ctx.externalizeString(tup.get(i));
				} else {
					assert t.get(i).storageType() == PredicateRefType.get();
					line[i] = "'" + ctx.externalizeString(tup.get(i));
				}

			}
			writer.writeNext(line);
		}

		writer.close();

		timer.stop();
		SimpleLogger.logger().time("Writing CSV file: " + timer.getTime() + "ms ");
	}
}
