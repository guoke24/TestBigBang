/**
 * IK 中文分词  版本 5.0
 * IK Analyzer release 5.0
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 源代码由林良益(linliangyi2005@gmail.com)提供
 * 版权声明 2012，乌龙茶工作室
 * provided by Linliangyi and copyright 2012 by Oolong studio
 */
package com.topwise.fenciinone.parserlocal_ik.analyzer.core;

import android.app.Application;
import android.util.Log;

import com.topwise.fenciinone.parserlocal_ik.analyzer.cfg.Configuration;
import com.topwise.fenciinone.parserlocal_ik.analyzer.cfg.DefaultConfig;
import com.topwise.fenciinone.parserlocal_ik.analyzer.dic.Dictionary;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;



/**
 * IK分词器主类
 *
 */
public final class IKSegmenter {
	
	//字符窜reader
	private Reader input;
	//分词器配置项
	private Configuration cfg;
	//分词器上下文
	private AnalyzeContext context;
	//分词处理器列表
	private List<ISegmenter> segmenters;
	//分词歧义裁决器
	private IKArbitrator arbitrator;
	

	/**
	 * IK分词器构造函数
	 * @param input 
	 * @param useSmart 为true，使用智能分词策略
	 * 
	 * 非智能分词：细粒度输出所有可能的切分结果
	 * 智能分词： 合并数词和量词，对分词结果进行歧义判断
	 */
	public IKSegmenter(Application application, Reader input , boolean useSmart){
		this.input = input;
		this.cfg = DefaultConfig.getInstance(application);
		this.cfg.setUseSmart(useSmart);
		this.init();
	}
	
	/**
	 * IK分词器构造函数
	 * @param input
	 * @param cfg 使用自定义的Configuration构造分词器
	 * 
	 */
	public IKSegmenter(Reader input , Configuration cfg){
		this.input = input;
		this.cfg = cfg;
		this.init();
	}
	
	/**
	 * 初始化
	 */
	private void init(){
		//初始化词典单例
		Dictionary.initial(this.cfg);
		//初始化分词上下文
		this.context = new AnalyzeContext(this.cfg);
		//加载子分词器
		this.segmenters = this.loadSegmenters();
		//加载歧义裁决器
		this.arbitrator = new IKArbitrator();
	}
	
	/**
	 * 初始化词典，加载子分词器实现
	 * @return List<ISegmenter>
	 */
	private List<ISegmenter> loadSegmenters(){
		List<ISegmenter> segmenters = new ArrayList<ISegmenter>(4);
		//处理特殊符号
		segmenters.add(new UselessSegmenter());
		//处理字母的子分词器
		segmenters.add(new LetterSegmenter());
		//处理中文数量词的子分词器
		segmenters.add(new CN_QuantifierSegmenter());
		//处理中文词的子分词器
		segmenters.add(new CJKSegmenter());

		return segmenters;
	}
	
	/**
	 * 分词，获取下一个词元
	 * @return Lexeme 词元对象
	 * @throws IOException
	 */
	public synchronized Lexeme next()throws IOException {
		Log.d("guohao-4","[next]开始");
		Lexeme l = null;
		//循环直到获取到一个有内容的Lexeme实例l
		while((l = context.getNextLexeme()) == null ){
			Log.d("guohao-4","[next]进入while循环");
			//如果获取到的 l 为空，就进入循环，处理一下，目的就是获取下一侧词源
			//需要分词的整个文本都存在context这个变量里

			/*
			 * 从reader中读取数据，填充buffer
			 * 如果reader是分次读入buffer的，那么buffer要  进行移位处理
			 * 移位处理上次读入的但未处理的数据
			 */
			//available 表示已读取到的字数
			int available = context.fillBuffer(this.input);
			//先判断有没有读取完
			if(available <= 0){
				//reader已经读完
				context.reset();
				return null;
				
			}else{
				//初始化指针，把cursor置为0
				context.initCursor();
				//=======do-While循环开始=========
				//一个一个字符的分析
				//如果遇到标点和空格，直接被作为单独的词元生成，而且前面的英文或数字串，
				// 也被作为词元生成，例如当扫描到you，的“，”的时候。you和“，”都被作为词元生成。
				do{
					Log.d("guohao-410",""
							+"扫描到第"+(context.getCursor()+1)+"个字"
							+",该字符为 = “"+context.getCurrentChar()+"”"
							+",该字符类型为 = "+context.getCurrentCharType()
					);
					//遍历子分词器，分别调用子分词器的分词函数
        			for(ISegmenter segmenter : segmenters){
						//采用代表不同的分词策略的分词器对context里面的内容进行分词
        				segmenter.analyze(context);
        			}
        			//字符缓冲区接近读完，需要读入新的字符
        			if(context.needRefillBuffer()){
        				break;
        			}
   				//向前移动指针，还能移动就返回true，不行就返回false
				}while(context.moveCursor());
				//=======do-While循环结束==========

				//重置子分词器，为下轮循环进行初始化
				for(ISegmenter segmenter : segmenters){
					segmenter.reset();
				}
			}
			//对分词进行歧义处理
			//例如，神通广大海捞针，会被切分为：神通广 | 大海捞针
			this.arbitrator.process(context, /*this.cfg.useSmart()*/true);
			//将分词结果输出到结果集，并处理未切分的单个CJK字符
			//保存到AnalyzeContext.results
			context.outputToResult();
			//记录本次分词的缓冲区位移，代码this.buffOffset += this.cursor
			context.markBufferOffset();			
		}
		Log.d("guohao-1","[next]结束，返回 = "+l.getLexemeText());
		Log.d("guohao-4","[next]返回 = "+l.getLexemeText());
		return l;
	}

	/**
     * 重置分词器到初始状态
     * @param input
     */
	public synchronized void reset(Reader input) {
		this.input = input;
		context.reset();
		for(ISegmenter segmenter : segmenters){
			segmenter.reset();
		}
	}
}
