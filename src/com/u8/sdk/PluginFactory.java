package com.u8.sdk;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;

@SuppressLint("UseSparseArrays")
public class PluginFactory {

	private static PluginFactory instance;
	
	private Map<Integer, String> supportedPlugins;
	
	private PluginFactory(){
		supportedPlugins = new HashMap<Integer, String>();
	}
	
	public static PluginFactory getInstance(){
		if(instance == null){
			instance = new PluginFactory();
		}
		
		return instance;
	}

	private boolean isSupportPlugin(int type){
		
		return supportedPlugins.containsKey(type);
	}
	
	private String getPluginName(int type){
		if(supportedPlugins.containsKey(type)){
			return supportedPlugins.get(type);
		}
		return null;
	}
	
	public Bundle getMetaData(Context context)
	{
		try {
			ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			
			if (appInfo != null && appInfo.metaData != null)
			{
				return appInfo.metaData;
			}
		} catch (NameNotFoundException e) {
		}
		
		return new Bundle();
	}
	
	public SDKParams getSDKParams(Context context){
		Map<String, String> configs = SDKTools.getAssetPropConfig(context, "developer_config.properties");
		return new SDKParams(configs);		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object initPlugin(int type){
		Class localClass = null;
		
		try {
			
			if(!isSupportPlugin(type)){
				
				Log.e("U8SDK", "The config of the U8SDK is not support plugin type:"+type);
				return null;
			}
			
			String pluginName = getPluginName(type);
			
			localClass = Class.forName(pluginName);
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		try {
			return localClass.getDeclaredConstructor(new Class[]{Activity.class}).newInstance(new Object[]{U8SDK.getInstance().getContext()});
		} catch (Exception e) {

			e.printStackTrace();
		}
		return null;
	}
	
	
	public void loadPluginInfo(Context context){
		String xmlPlugins = SDKTools.getAssetConfigs(context, "plugin_config.xml");
		
		if (xmlPlugins == null)
		{
			Log.e("U8SDK", "fail to load plugin_config.xml");
			return;
		}
		
		XmlPullParser parser = Xml.newPullParser();

		try {
			parser.setInput(new StringReader(xmlPlugins));
			
			int eventType = parser.getEventType();
			while(eventType != XmlPullParser.END_DOCUMENT){
				
				switch(eventType){
				case XmlPullParser.START_TAG:
					String tag = parser.getName();
					if("plugin".equals(tag)){
						String name = parser.getAttributeValue(0);
						int type = Integer.parseInt(parser.getAttributeValue(1));
						this.supportedPlugins.put(type, name);
						Log.d("U8SDK", "Curr Supported Plugin: "+type+"; name:"+name);
					}
				}
				eventType = parser.next();
			}
			
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

