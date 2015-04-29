package org.elasticsearch.index.analysis;

import org.junit.Test;

/**
 * User: Medcl Date: 12-11-2 Time: 上午9:29
 */
public class RedisHanlderTest {
	
	@Test
	public void testAddNewItem() throws Exception {

		RedisHanlder redisHanlder = RedisHanlder.getInstance("localhost", 6379,
				true, false);
		System.out.println(redisHanlder.convert("key1", "北京"));
		System.out.println(redisHanlder.convert("key1", "北京"));
		System.out.println(redisHanlder.convert("key1", "北京2"));
		System.out.println(redisHanlder.convert("key1", "北京3"));
		System.out.println(redisHanlder.convert("key1", "北京4"));
	}
}
