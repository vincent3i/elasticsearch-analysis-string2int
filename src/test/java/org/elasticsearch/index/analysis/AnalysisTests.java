///*
//* Licensed to ElasticSearch and Shay Banon under one
//* or more contributor license agreements.  See the NOTICE file
//* distributed with this work for additional information
//* regarding copyright ownership. ElasticSearch licenses this
//* file to you under the Apache License, Version 2.0 (the
//* "License"); you may not use this file except in compliance
//* with the License.  You may obtain a copy of the License at
//*
//*    http://www.apache.org/licenses/LICENSE-2.0
//*
//* Unless required by applicable law or agreed to in writing,
//* software distributed under the License is distributed on an
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
//* KIND, either express or implied.  See the License for the
//* specific language governing permissions and limitations
//* under the License.
//*/

package org.elasticsearch.index.analysis;

import static org.elasticsearch.common.settings.ImmutableSettings.Builder.EMPTY_SETTINGS;
import static org.hamcrest.Matchers.instanceOf;
import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_VERSION_CREATED;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
*/
public class AnalysisTests {

	@Test
	public void testPinyinAnalysis() {
		Index index = new Index("test");

		Settings settings = ImmutableSettings
				.settingsBuilder()
				.put(SETTING_VERSION_CREATED, org.elasticsearch.Version.CURRENT)
				.build();

		Injector parentInjector = new ModulesBuilder().add(
				new SettingsModule(settings),
				new EnvironmentModule(new Environment(EMPTY_SETTINGS)))
				.createInjector();
		Injector injector = new ModulesBuilder().add(
				new IndexSettingsModule(index, settings),
				new IndexNameModule(index),
				new AnalysisModule(settings, parentInjector
						.getInstance(IndicesAnalysisService.class))
						.addProcessor(new String2IntAnalysisBinderProcessor()))
				.createChildInjector(parentInjector);

		AnalysisService analysisService = injector
				.getInstance(AnalysisService.class);

		TokenizerFactory tokenizerFactory = analysisService
				.tokenizer("string2int");
		MatcherAssert.assertThat(tokenizerFactory,
				instanceOf(String2IntTokenizerFactory.class));

		TokenFilterFactory tokenFilterFactory = analysisService
				.tokenFilter("string2int");
		MatcherAssert.assertThat(tokenFilterFactory,
				instanceOf(String2IntTokenFilterFactory.class));
	}

	@Test
	public void testTokenFilter() throws IOException {
		StringReader sr = new StringReader("刘德华 张学友");
		Analyzer analyzer = new WhitespaceAnalyzer();
		analyzer.setVersion(Version.LATEST);
		String2IntTokenFilter filter = new String2IntTokenFilter(
				analyzer.tokenStream("f", sr), "localhost", 6379, "key123",
				true, false);
		List<String> list = new ArrayList<String>();
		filter.reset();
		while (filter.incrementToken()) {
			CharTermAttribute ta = filter.getAttribute(CharTermAttribute.class);
			list.add(ta.toString());
			System.out.println(ta.toString());
		}
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("1", list.get(0));
		Assert.assertEquals("2", list.get(1));

		filter.close();
		analyzer.close();
	}

	@Test
	public void TestTokenFilter1() throws IOException {
		StringReader sr1 = new StringReader("刘德华");
		Analyzer analyzer = new WhitespaceAnalyzer();
		analyzer.setVersion(Version.LATEST);

		TokenFilter filter = new String2IntTokenFilter(analyzer.tokenStream(
				"f", sr1), "localhost", 6379, "key123", true, false);
		List<String> list = new ArrayList<String>();
		try {
			filter.reset();
			while (filter.incrementToken()) {
				CharTermAttribute ta = filter
						.getAttribute(CharTermAttribute.class);
				list.add(ta.toString());
				System.out.println("value:" + ta.toString());

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(1, list.size());
		Assert.assertEquals("1", list.get(0));

		filter.close();
		analyzer.close();
		sr1.close();
	}

	@Test
	public void TestTokenizer() throws IOException {
		String[] s = { "斯巴达", "300" };
		for (String value : s) {
			System.out.println(value);
			StringReader sr = new StringReader(value);

			String2IntTokenizer tokenizer = new String2IntTokenizer(sr,
					"localhost", 6379, "key11", true, false);

			tokenizer.reset();
			boolean hasnext = tokenizer.incrementToken();

			while (hasnext) {

				CharTermAttribute ta = tokenizer
						.getAttribute(CharTermAttribute.class);

				System.out.println(ta.toString());

				hasnext = tokenizer.incrementToken();

			}
			tokenizer.close();
		}

	}

	@Test
	public void TestLRUTokenizer() throws IOException {
		
		int i = 100;
		while (i-- > 0) {
			String value = DateTime.now().toLocalDateTime().toString();
			System.out.println(value);
			StringReader sr = new StringReader(value);

			String2IntTokenizer tokenizer;
			if(i % 2 == 0) {
				System.out.println("use_lru_cache is false");
				tokenizer = new String2IntTokenizer(sr, "localhost", 6379, "key11", true, false);
			} else {
				tokenizer = new String2IntTokenizer(sr,"localhost",6379,"key11",true,true);
			}

			tokenizer.reset();
			boolean hasnext = tokenizer.incrementToken();

			while (hasnext) {

				CharTermAttribute ta = tokenizer.getAttribute(CharTermAttribute.class);

				System.out.println(ta.toString());

				hasnext = tokenizer.incrementToken();

			}
			tokenizer.close();
		}

	}

}
