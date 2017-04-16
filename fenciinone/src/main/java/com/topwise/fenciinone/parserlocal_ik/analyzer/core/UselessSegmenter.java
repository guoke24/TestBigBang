package com.topwise.fenciinone.parserlocal_ik.analyzer.core;

import android.util.Log;

/**
 * Created by baoyongzhang on 2016/10/31.
 */
public class UselessSegmenter implements ISegmenter {

    static final String SEGMENTER_NAME = "USELESS_SEGMENTER";

    @Override
    public void analyze(AnalyzeContext context) {
        if (CharacterUtil.CHAR_USELESS == context.getCurrentCharType()) {
            Log.d("guohao-410","检测为符号"
                    +",当前curosr = "+context.getCursor()
                    +",偏移量 = "+context.getBufferOffset()
                    +",词元类型 = " + Lexeme.TYPE_UNKNOWN
            );
            //Lexeme newLexeme = new Lexeme(context.getBufferOffset() , 0 , 1 , Lexeme.TYPE_UNKNOWN);
            //add by guohao
            Lexeme newLexeme = new Lexeme(context.getBufferOffset() , context.getCursor() , 1 , Lexeme.TYPE_UNKNOWN);
            //end by guohao
            context.addLexeme(newLexeme);
            context.lockBuffer(SEGMENTER_NAME);
        }
        //add by guohao 20170410 如果读完应该移除锁定
        if(context.isBufferConsumed()){
            context.unlockBuffer(SEGMENTER_NAME);
        }
        //end by guohao
    }

    @Override
    public void reset() {

    }
}
