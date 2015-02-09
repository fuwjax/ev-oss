/*
 * Copyright (C) 2015 EchoVantage (info@echovantage.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.echovantage.gild.proxy.db;

import org.echovantage.gild.proxy.AbstractServiceProxy;
import org.echovantage.util.ReadOnlyPath;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.nio.file.Files.newBufferedWriter;
import static org.echovantage.util.Assert2.assertCompletes;
import static org.junit.Assert.*;

public class DataSourceProxy extends AbstractServiceProxy {
	private Charset charset = Charset.forName("UTF-8");
	private final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC"));
	private final DateTimeFormatter GMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSx").withZone(ZoneId.of("UTC"));
	private DataSource db;

	@Override
	protected boolean preserveImpl(final Path output, final ReadOnlyPath golden) throws IOException, SQLException {
		try(Connection c = db.getConnection();
				Statement s = c.createStatement();
				DirectoryStream<ReadOnlyPath> schemas = golden.newDirectoryStream()) {
			for(final ReadOnlyPath schema : schemas) {
				final Path schemaTarget = output.resolve(schema.getFileName());
				Files.createDirectories(schemaTarget);
				try(DirectoryStream<ReadOnlyPath> tables = schema.newDirectoryStream()) {
					for(final ReadOnlyPath table : tables) {
						final Path tableTarget = schemaTarget.resolve(table.getFileName());
						try(BufferedWriter w = newBufferedWriter(tableTarget, charset);
								ResultSet rs = s.executeQuery("SELECT * FROM " + tableName(table) + " ORDER BY id")) {
							while(rs.next()) {
								if(rs.isFirst()) {
									for(int col = 1; col <= rs.getMetaData().getColumnCount(); col++) {
										final String colName = rs.getMetaData().getColumnName(col);
										w.append(col == 1 ? "" : ",").append(colName);
									}
									w.newLine();
								}
								for(int col = 1; col <= rs.getMetaData().getColumnCount(); col++) {
									w.append(col == 1 ? "" : ",").append(stringify(rs.getObject(col)));
								}
								w.newLine();
							}
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	protected void prepareImpl(final ReadOnlyPath input, final Path output) throws IOException, SQLException {
		try(Connection c = db.getConnection();
				Statement s = c.createStatement();
				DirectoryStream<ReadOnlyPath> schemas = input.newDirectoryStream()) {
			for(final ReadOnlyPath schema : schemas) {
				loadSchema(schema, s);
			}
		}
	}

	private void loadSchema(final ReadOnlyPath schema, final Statement s) throws IOException, SQLException {
		final List<ReadOnlyPath> tables = new ArrayList<>();
		try(DirectoryStream<ReadOnlyPath> schemaTables = schema.newDirectoryStream()) {
			schemaTables.forEach(table -> tables.add(table));
		}
		for(final ReadOnlyPath table : tables) {
			final String tableName = table.getParent().getFileName() + "." + table.getFileName();
			s.execute("TRUNCATE TABLE " + tableName + " RESTART IDENTITY CASCADE");
		}
		while(!tables.isEmpty()) {
			final int count = tables.size();
			final Iterator<ReadOnlyPath> iter = tables.iterator();
			while(iter.hasNext()) {
				try {
					loadTable(iter.next(), s);
					iter.remove();
				} catch(final SQLException e) {
					if(!"23503".equals(e.getSQLState())) {
						throw e;
					}
				}
			}
			assertNotEquals("Cannot load any more tables from " + tables, count, tables.size());
		}
	}

	private void loadTable(final ReadOnlyPath table, final Statement s) throws IOException, SQLException {
		final String tableName = tableName(table);
		try(BufferedReader r = new BufferedReader(new InputStreamReader(table.newInputStream(), charset))) {
			String line = r.readLine();
			final String sql = "INSERT INTO " + tableName + " (" + line + ") VALUES (";
			int count = 1;
			while((line = r.readLine()) != null) {
				count++;
				s.execute(sql + line + ")");
			}
			s.execute("ALTER SEQUENCE IF EXISTS " + tableName + "_id_seq RESTART WITH " + count);
		}
	}

	private static String tableName(final ReadOnlyPath table) {
		return table.getParent().getFileName() + "." + table.getFileName();
	}

	public DataSourceProxy setDataSource(final DataSource db) {
		this.db = db;
		assertCompletes(this::configured);
		return this;
	}

	public DataSourceProxy setCharset(final Charset charset) {
		assertNotConfigured();
		this.charset = charset;
		return this;
	}

	protected CharSequence stringify(final Object object) throws SQLException {
		if(object instanceof String) {
			return "'" + object.toString().replaceAll("'", "''") + "'";
		}
		if(object instanceof java.sql.Date){
			return "'" + DATE.format(((java.sql.Date) object).toLocalDate()) +"'";
		}
		if(object instanceof Timestamp) {
			return "'" + GMT.format(((Timestamp) object).toInstant()) + "'";
		}
		if(object instanceof Map) {
			final StringBuilder builder = new StringBuilder("'");
			String delim = "";
			for(final Map.Entry<String, String> entry : new TreeMap<>((Map<String, String>) object).entrySet()) {
				builder.append(delim).append(inner(entry.getKey())).append("=>").append(inner(entry.getValue()));
				delim = ",";
			}
			return builder.append("'").toString();
		}
		if(object instanceof Array) {
			final StringBuilder builder = new StringBuilder("'{");
			String delim = "";
			for(final Object value : (Object[]) ((Array) object).getArray()) {
				builder.append(delim).append(value instanceof String ? inner((String) value) : String.valueOf(value));
				delim = ",";
			}
			return builder.append("}'").toString();
		}
		return String.valueOf(object);
	}

	private static String inner(final String text) {
		return "\"" + text.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("'", "''") + "\"";
	}
}
