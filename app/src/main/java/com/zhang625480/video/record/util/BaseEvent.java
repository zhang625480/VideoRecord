package com.zhang625480.video.record.util;

import java.util.Map;

/**
 * Created by AFWang on 15/12/28.
 * 通用EventBus使用的数据
 */
public class BaseEvent {
  public int what;
  public int arg0;
  public String content;
  public Object object;
  public Map map;

  public BaseEvent(){}

  public BaseEvent(int w){
    what = w;
  }

  public BaseEvent(int w, String s, Object o){
    what = w;
    content = s;
    object = o;
  }
}