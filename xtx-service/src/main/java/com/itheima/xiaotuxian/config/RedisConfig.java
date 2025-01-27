package com.itheima.xiaotuxian.config;



import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * 缓存Redis配置
 * @author: lbc
 * @Date: 2023-05-12 14:24:49
 * @Descripttion:
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

	/**
	 * 使用Jackson2JsonRedisSerialize 替换默认序列化
	 */
	@Bean
	public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
		// 使用Jackson2JsonRedisSerialize 替换默认序列化
		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(
				Object.class);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
		// 设置value的序列化规则和 key的序列化规则
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
		// hash参数序列化方式
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
		// 缓存支持回滚(事务管理)
		redisTemplate.setEnableTransactionSupport(true);
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}

	// 配置事务管理器
	@Bean
	public PlatformTransactionManager transactionManager(DataSource dataSource) throws SQLException {
		return new DataSourceTransactionManager(dataSource);
	}

	/**
	 * 配置自动化缓存使用的序列化方式以及过期时间
	 */
	@Bean
	public RedisCacheConfiguration redisCacheConfiguration() {
		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(
				Object.class);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
		RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig();
		configuration = configuration
				.serializeValuesWith(
						RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
				.entryTtl(Duration.ofDays(1));
		return configuration;
	}

	/**
	 * 缓存过期时间自定义配置
	 */
	// @Bean
	// public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
	// 	// 设置CacheManager的值序列化方式为Jackson2JsonRedisSerializer,默认就是使用StringRedisSerializer序列化key,JdkSerializationRedisSerializer序列化value
	// 	Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(
	// 			Object.class);
	// 	ObjectMapper objectMapper = new ObjectMapper();
	// 	objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
	// 	objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
	// 	jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
	// 	// 配置value序列化方式为Jackson2JsonRedisSerializer,key序列化方式采用默认的StringRedisSerializer
	// 	RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(
	// 			RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer));
	// 	// 每一类信息进行缓存配置
	// 	Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = new HashMap<>();
	// 	// 用户信息
	// 	redisCacheConfigurationMap.put("test", cacheConfiguration.entryTtl(Duration.ofDays(10))
	// 			.disableCachingNullValues().prefixKeysWith("test"));

	// 	// 初始化一个RedisCacheWriter
	// 	RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);
	// 	RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(
	// 			RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer));
	// 	// 设置默认超过期时间是1天(短时间的已经做了其他的处理,不会采用注解形式加入缓存)
	// 	defaultCacheConfig.entryTtl(Duration.ofDays(1));
	// 	// 初始化RedisCacheManager
	// 	RedisCacheManager cacheManager = new RedisCacheManager(redisCacheWriter, defaultCacheConfig,
	// 			redisCacheConfigurationMap);
	// 	return cacheManager;
	// }
	@Bean
    public CacheManager cacheManager(RedisConnectionFactory factory){
        //创建FastJson对象，用于序列化
        GenericFastJsonRedisSerializer fastJsonRedisSerializer = new GenericFastJsonRedisSerializer();
        //创建一个RedisCache的配置对象
        RedisCacheConfiguration config=RedisCacheConfiguration.defaultCacheConfig();
        //过期时间设置为一天
        //对存的的key,value进行序列化
        config=config.entryTtl(Duration.ofHours(8))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(fastJsonRedisSerializer))
                .disableCachingNullValues();
        //自己定义要缓存的内存空间名字
        Set<String> cacheNames=new HashSet<>();
        cacheNames.add("xiaotuxian");
        //对缓存空间设置单独的配置，在这个可根据业务，如果数据很久才会变一次时间就可设成永久
        Map<String,RedisCacheConfiguration> configMap=new HashMap<>();
        configMap.put("xiaotuxian",config);
        //创建RedisCacheManage对象，将上面的配置导入
        RedisCacheManager redisCacheManager=RedisCacheManager.builder(factory)
                .initialCacheNames(cacheNames)
                .withInitialCacheConfigurations(configMap)
                .build();
        return redisCacheManager;
    }
// ————————————————
// 版权声明：本文为CSDN博主「不叫浮云」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
// 原文链接：https://blog.csdn.net/weixin_43882435/article/details/104027450
}
