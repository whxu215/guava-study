package com.gtt.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;


/**
 * Created by xupanpan on 2017/12/6.
 */
public class CacheTest {

  @Getter
  @Setter
  @Builder
  private static class User {
    private String uid;
    private String name;
    private int age;
  }

  @Test
  public void cachePerfTest() {
    LoadingCache<String, User> userCache = CacheBuilder.newBuilder()
            .maximumSize(1000000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats()
            .build(new CacheLoader<String, User>() {
              public User load(String uid) {
                // Load the user information from DB
                System.out.println("Load user");
                return User.builder().uid(uid).name("andy").age(30).build();
              }
            });

    IntStream.rangeClosed(1, 100000)
            .forEach(i -> {
              try {
                userCache.get(i + "");
              } catch (ExecutionException e) {
                e.printStackTrace();
              }
            });

    System.out.println("Start:" + System.currentTimeMillis());
    IntStream.range(0, 10)
            .forEach( j -> {
              IntStream.rangeClosed(1, 100000)
                      .forEach(i -> {
                        try {
                          userCache.get(i + "");
                        } catch (ExecutionException e) {
                          e.printStackTrace();
                        }
                      });
            });

    System.out.println("End:" + System.currentTimeMillis());
    System.out.println(userCache.stats().toString());
  }

  @Test
  public void cacheGetTest() throws ExecutionException, InterruptedException {
    LoadingCache<String, User> userCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .recordStats()
            .build(new CacheLoader<String, User>() {
              public User load(String uid) {
                // Load the user information from DB
                System.out.println("Load user");
                return User.builder().uid(uid).name("andy").age(30).build();
              }
            });
    User user = userCache.get("100000000001");
    Assert.assertThat(user, CoreMatchers.<User>notNullValue());
    Assert.assertThat(user.getName(), is("andy"));
    Assert.assertThat(user.getAge(), is(30));

    userCache.get("100000000001");
    userCache.get("100000000001");
    userCache.get("100000000001");
    Thread.sleep(10000);

    System.out.println("after sleep");
    userCache.get("100000000001");

    System.out.println(userCache.stats().toString());
  }
}
