package com.example.my17_1;

import android.content.res.Resources;

public class ShaderManager {

	final static int shaderCount = 1;
	final static String[][] shaderName = 
	{
		{"vertex.sh","frag.sh"}
	};
	static String[] mVertexShader = new String[shaderCount];
	static String[] mFragmentShader = new String[shaderCount];
	static int[] program = new int[shaderCount];
	
	public static void loadCodeFromFile(Resources r){
		for (int i = 0; i < shaderCount; i++) {
			mVertexShader[i] = ShaderUtil.loadFromAssetsFile(shaderName[i][0], r);
			mFragmentShader[i] = ShaderUtil.loadFromAssetsFile(shaderName[i][1], r);
		}
	}
	
	public static void compileShader(){
		for (int i = 0; i < shaderCount; i++) {
			program[i] = ShaderUtil.createProgram(mVertexShader[i], mFragmentShader[i]);
		}
	}
	
	public static int getTextureShaderProgram(){
		return program[0];
	}
	
}
