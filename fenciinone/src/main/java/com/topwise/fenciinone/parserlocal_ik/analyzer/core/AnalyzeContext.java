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
 * 
 */
package com.topwise.fenciinone.parserlocal_ik.analyzer.core;

import android.util.Log;

import com.topwise.fenciinone.parserlocal_ik.analyzer.cfg.Configuration;
import com.topwise.fenciinone.parserlocal_ik.analyzer.dic.Dictionary;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;



/**
 * 
 * 分词器上下文状态
 * 
 */
class AnalyzeContext {
	
	//默认缓冲区大小
	private static final int BUFF_SIZE = 4096;
	//缓冲区耗尽的临界值
	private static final int BUFF_EXHAUST_CRITICAL = 100;	
	
 
	//字符窜读取缓冲
    private char[] segmentBuff;
    //字符类型数组
    private int[] charTypes;
    
    
    //记录Reader内已分析的字串总长度
    //在分多段分析词元时，该变量累计当前的segmentBuff相对于reader起始位置的位移
	private int buffOffset;	
    //当前缓冲区位置指针
    private int cursor;
    //最近一次读入的,可处理的字串长度
	private int available;

	
	//子分词器锁
    //该集合非空，说明有子分词器在占用segmentBuff
    private Set<String> buffLocker;
    
    //原始分词结果集合，未经歧义处理
    private QuickSortSet orgLexemes;    
    //LexemePath位置索引表
    private Map<Integer , LexemePath> pathMap;
    //最终分词结果集
    private LinkedList<Lexeme> results;
    
	//分词器配置项
	private Configuration cfg;
    
    public AnalyzeContext(Configuration cfg){
    	this.cfg = cfg;
    	this.segmentBuff = new char[BUFF_SIZE];
    	this.charTypes = new int[BUFF_SIZE];
    	this.buffLocker = new HashSet<String>();
    	this.orgLexemes = new QuickSortSet();
    	this.pathMap = new HashMap<Integer , LexemePath>();
    	this.results = new LinkedList<Lexeme>();
    }
    
    int getCursor(){
    	return this.cursor;
    }
//    
//    void setCursor(int cursor){
//    	this.cursor = cursor;
//    }
    
    char[] getSegmentBuff(){
    	return this.segmentBuff;
    }
    
    char getCurrentChar(){
    	return this.segmentBuff[this.cursor];
    }
    
    int getCurrentCharType(){
    	return this.charTypes[this.cursor];
    }
    
    int getBufferOffset(){
    	return this.buffOffset;
    }
	
    /**
     * 根据context的上下文情况，填充segmentBuff 
     * @param reader
     * @return 返回待分析的（有效的）字串长度
     * @throws IOException
     */
	int fillBufferTime = 1;
    int fillBuffer(Reader reader) throws IOException {
		Log.d("guohao-1","-->[fillBuffer]第"+fillBufferTime+"次 开始");

		int readCount = 0;
    	if(this.buffOffset == 0){
    		//首次读取reader，读到缓冲区segmentBuff
			readCount = reader.read(segmentBuff);
		}else{
    		int offset = this.available - this.cursor;
			//offset 表示 未处理的偏移量
    		if(offset > 0){
    			//最近一次读取的>最近一次处理的，说明有未处理的内容
				//将未处理的字串拷贝到segmentBuff头部
				//第一个参数，表示源
				//第二个参数，表示源的开始位置
				//第三个参数，表示要拷贝到的目标
				//第四个参数，表示要目标的哪一位开始存入拷贝内容
				//第五个参数，表示拷贝的长度
				Log.d("guohao-1","拷贝前 segmentBuff中已读取的 = "
						+String.valueOf(segmentBuff,0,this.cursor));
				System.arraycopy(this.segmentBuff , this.cursor , this.segmentBuff , 0 , offset);
				Log.d("guohao-1","拷贝后 segmentBuff未读取的 = "
						+String.valueOf(segmentBuff,0,offset));
				readCount = offset;
				Log.d("guohao-1","拷贝后，已经读到的字数readCount = "
						+ readCount );

			}
    		//继续读取reader ，以onceReadIn - onceAnalyzed为起始位置，继续填充segmentBuff剩余的部分
    		readCount += reader.read(this.segmentBuff , offset , BUFF_SIZE - offset);

			Log.d("guohao-1","读取结束，读到的字数readCount = "+ readCount );

		}

		String s = String.valueOf(segmentBuff,0,readCount);
		if (s==null||s.equals("")){
			Log.d("guohao-410",""
					+"第"+fillBufferTime+"次"
					+"填入的待分析文本为空，分词结束！ "
			);
		}else{
			Log.d("guohao-410",""
					+"第"+fillBufferTime+"次"
					+"填入的待分析文本 = "+ String.valueOf(segmentBuff,0,readCount)
			);
		}
		;
    	//记录最后一次从Reader中读入的可用字符长度
    	this.available = readCount;
		//重置当前指针
    	this.cursor = 0;
		Log.d("guohao-1","-->[fillBuffer]第"+fillBufferTime+"次 结束");
		fillBufferTime++;
    	return readCount;
    }

    /**
     * 初始化buff指针，处理第一个字符
     */
    void initCursor(){
    	this.cursor = 0;
    	this.segmentBuff[this.cursor] = CharacterUtil.regularize(this.segmentBuff[this.cursor]);
    	this.charTypes[this.cursor] = CharacterUtil.identifyCharType(this.segmentBuff[this.cursor]);
    }
    
    /**
     * 指针+1
     * 成功返回 true； 指针已经到了buff尾部，不能前进，返回false
     * 并处理当前字符
     */
    boolean moveCursor(){
    	if(this.cursor < this.available - 1){
    		this.cursor++;
        	this.segmentBuff[this.cursor] = CharacterUtil.regularize(this.segmentBuff[this.cursor]);
        	this.charTypes[this.cursor] = CharacterUtil.identifyCharType(this.segmentBuff[this.cursor]);
    		return true;
    	}else{
    		return false;
    	}
    }
	
    /**
     * 设置当前segmentBuff为锁定状态
     * 加入占用segmentBuff的子分词器名称，表示占用segmentBuff
     * @param segmenterName
     */
	void lockBuffer(String segmenterName){
		this.buffLocker.add(segmenterName);
	}
	
	/**
	 * 移除指定的子分词器名，释放对segmentBuff的占用
	 * @param segmenterName
	 */
	void unlockBuffer(String segmenterName){
		this.buffLocker.remove(segmenterName);
	}
	
	/**
	 * 只要buffLocker中存在segmenterName
	 * 则buffer被锁定
	 * @return boolean 缓冲区是否被锁定
	 */
	boolean isBufferLocked(){
		return this.buffLocker.size() > 0;
	}

	/**
	 * 判断当前segmentBuff是否已经用完
	 * 当前执针cursor移至segmentBuff末端this.available - 1
	 * @return
	 */
	boolean isBufferConsumed(){
		return this.cursor == this.available - 1;
	}
	
	/**
	 * 判断segmentBuff是否需要读取新数据
	 * 
	 * 满足一下条件时，
	 * 1.available == BUFF_SIZE 表示buffer满载
	 * 2.buffIndex < available - 1 && buffIndex > available - BUFF_EXHAUST_CRITICAL表示当前指针处于临界区内
	 * 3.!context.isBufferLocked()表示没有segmenter在占用buffer
	 * 要中断当前循环（buffer要进行移位，并再读取数据的操作）
	 * @return
	 */
	boolean needRefillBuffer(){
		return this.available == BUFF_SIZE 
			&& this.cursor < this.available - 1   
			&& this.cursor  > this.available - BUFF_EXHAUST_CRITICAL
			&& !this.isBufferLocked();
	}
	
	/**
	 * 累计当前的segmentBuff相对于reader起始位置的位移
	 */
	void markBufferOffset(){
		this.buffOffset += this.cursor;
	}
	
	/**
	 * 向分词结果集添加词元
	 * @param lexeme
	 */
	void addLexeme(Lexeme lexeme){
		Log.d("guohao-410","[addLexemePath] "
				+"内容 = "+String.valueOf(segmentBuff,lexeme.getBegin(),lexeme.getLength())
				+",起点 = "+ lexeme.getBegin()
				+",长度 = "+ lexeme.getLength()
		);
		this.orgLexemes.addLexeme(lexeme);
	}
	
	/**
	 * 添加分词结果路径
	 * 路径起始位置 ---> 路径 映射表
	 * @param path
	 */
	void addLexemePath(LexemePath path){
		Log.d("guohao-410","[addLexemePath] "
				+"内容 = "+String.valueOf(segmentBuff,path.getPathBegin(),path.getPathLength())
			+",起点 = "+path.getPathBegin()
			+",长度 = "+(path.getPathLength())
		);



		if(path != null){
			this.pathMap.put(path.getPathBegin(), path);
		}
	}
	
	
	/**
	 * 返回原始分词结果
	 * @return
	 */
	QuickSortSet getOrgLexemes(){
		return this.orgLexemes;
	}
	
	/**
	 * 推送分词结果到结果集合
	 * 1.从buff头部遍历到this.cursor已处理位置
	 * 2.将map中存在的分词结果推入results
	 * 3.将map中不存在的CJDK字符以单字方式推入results
	 */
	void outputToResult(){
		Log.d("guohao-1","-->[outputToResult] 开始 this.cursor = "+this.cursor);
		int index = 0;
		for( ; index <= this.cursor ;){
			Log.d("guohao-410","[outputToResult] 扫描到的下标为 = "+index);
			// TODO 跳过非CJK字符
//			if(CharacterUtil.CHAR_USELESS == this.charTypes[index]){
//				index++;
//				continue;
//			}
			//从pathMap找出对应index位置的LexemePath
			//所以解析好的内容先装在path里
			LexemePath path = this.pathMap.get(index);
			Log.d("guohao-1","-->【pathMap.get(index)】获取path");
			if(path != null){
				//输出LexemePath中的lexeme到results集合
				Lexeme l = path.pollFirst();
				Log.d("guohao-1","-->【path.pollFirst()】从path中取出词元 ");
				while(l != null){
					Log.d("guohao-1","-->[results.add(l)] 添加词元 l = "+l.toString()
							//+",l.getBegin() = "+l.getBegin()
							//+",l.getLength() = "+l.getLength()
					);
					Log.d("guohao-410","[outputToResult] 输出的词元为 = "
							+String.valueOf(segmentBuff,l.getBegin(),l.getLength()));
					this.results.add(l);
					//将index移至lexeme后
					index = l.getBegin() + l.getLength();
					Log.d("guohao-410","[outputToResult] 移动下标到 = "+index);
					//这次取出下一个词元，其目的只是添加两个词元之间的单个字符
					l = path.pollFirst();
					if(l != null){
						//输出path内部，词元间遗漏的单字
						for(;index < l.getBegin();index++){
							this.outputSingleCJK(index);
						}
					}else{
						Log.d("guohao-410","[outputToResult] 下标:"+index+",无对应词元！");
					}
					//该词元l，下一次循环在添加到result
				}
			}else{//pathMap中找不到index对应的LexemePath
				//单字输出
				this.outputSingleCJK(index);
				index++;
			}
		}//end for循环

		//清空当前的Map
		this.pathMap.clear();
		Log.d("guohao-1","-->[outputToResult] 结束 ");
	}
	
	/**
	 * 对CJK字符进行单字输出
	 * @param index
	 */
	private void outputSingleCJK(int index){
		if(CharacterUtil.CHAR_CHINESE == this.charTypes[index]){			
			Lexeme singleCharLexeme = new Lexeme(this.buffOffset , index , 1 , Lexeme.TYPE_CNCHAR);
			Log.d("guohao-410","[outputToResult-->outputSingleCJK] +"+"\n"
					+"输出的单字词元为 = “"+String.valueOf(segmentBuff,index,1)+"”"
			);
			this.results.add(singleCharLexeme);
		}else if(CharacterUtil.CHAR_OTHER_CJK == this.charTypes[index]){
			Lexeme singleCharLexeme = new Lexeme(this.buffOffset , index , 1 , Lexeme.TYPE_OTHER_CJK);
			Log.d("guohao-410","[outputToResult-->outputSingleCJK] +"+"\n"
					+"输出的单字词元为 = “"+String.valueOf(segmentBuff,index,1)+"”"
			);
			this.results.add(singleCharLexeme);
		}
	}
		
	/**
	 * 返回lexeme 
	 * 
	 * 同时处理合并
	 * @return
	 */
	int getNextLexemeTime = 1;
	Lexeme getNextLexeme(){
		Log.d("guohao-1","-->[getNextLexeme]第"+getNextLexemeTime+"次 开始");


		//从结果集取出，并移除第一个Lexme
		Lexeme result = this.results.pollFirst();
		int time = 1;

		Log.d("guohao-1","--->[results.pollFirst()] 取出词元");
		if (result == null) Log.d("guohao-1"," 无词元，不进入循环");
		while(result != null){//不为空就循环，那就说明最后返回的一定是空！
			Log.d("guohao-1"," 有词元，进入while循环【while(result != null)】");
			Log.d("guohao-1"," 第"+time+"次while循环");
			time++;
			//数量词合并
    		this.compound(result);
			Log.d("guohao-1","--->[Dictionary.getSingleton().isStopWord(...)]");
    		if(Dictionary.getSingleton().isStopWord(this.segmentBuff ,
													result.getBegin() ,
													result.getLength()))
			{
       			//如果是停止词继续取列表的下一个
				Log.d("guohao-1"," 词元是停止词！"+result.getLexemeText()+",继续取列表的下一个");
    			result = this.results.pollFirst(); 				
    		}else{
				Log.d("guohao-1"," 词元不是停止词！开始生成词元文本：");
	 			//不是停止词, 生成lexeme的词元文本,输出
	    		result.setLexemeText(String.valueOf(segmentBuff , result.getBegin() , result.getLength()));
				Log.d("guohao-1"," 整段文本 = "+String.valueOf(segmentBuff,0,this.available));
				Log.d("guohao-1"," result.getBegin() = "+result.getBegin());
				Log.d("guohao-1"," result.getLength() = "+result.getLength());
				Log.d("guohao-1"," 提取的词元 = "+String.valueOf(segmentBuff,
						result.getBegin(),result.getLength()));
				//结束循环
				Log.d("guohao-1"," 提取的词元结束，跳出while循环【break】");
				break;
    		}
		}
		//Log.d("guohao-1","[getNextLexeme]第"+getNextLexemeTime+"次,返回result = "+result.toString());
		if (result == null){
			Log.d("guohao-1","-->[getNextLexeme] 第"+getNextLexemeTime+"次结束，返回 null ");

		}else{
			Log.d("guohao-1","-->[getNextLexeme] 返回 = "+result.getLexemeText());

		}
		getNextLexemeTime++;
		return result;
	}
	
	/**
	 * 重置分词上下文状态
	 */
	void reset(){		
		this.buffLocker.clear();
        this.orgLexemes = new QuickSortSet();
        this.available =0;
        this.buffOffset = 0;
    	this.charTypes = new int[BUFF_SIZE];
    	this.cursor = 0;
    	this.results.clear();
    	this.segmentBuff = new char[BUFF_SIZE];
    	this.pathMap.clear();
	}
	
	/**
	 * 组合词元
	 */
	private void compound(Lexeme result){
		if(!this.cfg.useSmart()){
			return ;
		}
   		//数量词合并处理
		if(!this.results.isEmpty()){

			if(Lexeme.TYPE_ARABIC == result.getLexemeType()){
				Lexeme nextLexeme = this.results.peekFirst();
				boolean appendOk = false;
				if(Lexeme.TYPE_CNUM == nextLexeme.getLexemeType()){
					//合并英文数词+中文数词
					appendOk = result.append(nextLexeme, Lexeme.TYPE_CNUM);
				}else if(Lexeme.TYPE_COUNT == nextLexeme.getLexemeType()){
					//合并英文数词+中文量词
					appendOk = result.append(nextLexeme, Lexeme.TYPE_CQUAN);
				}
				if(appendOk){
					//弹出
					this.results.pollFirst(); 
				}
			}
			
			//可能存在第二轮合并
			if(Lexeme.TYPE_CNUM == result.getLexemeType() && !this.results.isEmpty()){
				Lexeme nextLexeme = this.results.peekFirst();
				boolean appendOk = false;
				 if(Lexeme.TYPE_COUNT == nextLexeme.getLexemeType()){
					 //合并中文数词+中文量词
 					appendOk = result.append(nextLexeme, Lexeme.TYPE_CQUAN);
 				}  
				if(appendOk){
					//弹出
					this.results.pollFirst();   				
				}
			}

		}
	}
	
}
