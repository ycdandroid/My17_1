package com.example.my17_1;

import java.io.Console;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


import android.opengl.GLES20;
import android.util.Log;

public class TextureRect {
	
	int mProgram;
	int muMVPMatrixHandle;
	int muMMatrixHandle;
	int muCameraHandle;
	int muTextureHandle;
	
	int maPositionHandle;
	int maTexCoorHandle;
	
	String mVertexShader;
	String mFragmentShader;
	
	FloatBuffer mVertexBuffer;
	FloatBuffer mTextureBuffer;
	
	int VCount;
	
	public TextureRect(int mProgram, final float UNIT_SIZE){
		this.mProgram = mProgram;
		initVertexData(UNIT_SIZE);
		initShader(mProgram);
	}
	
	public void initVertexData(final float UNIT_SIZE){
		VCount = 6;
		float[] vertices = new float[]
		{
				-1*UNIT_SIZE, 1*UNIT_SIZE, 0,
				-1*UNIT_SIZE, -1*UNIT_SIZE, 0,
				1*UNIT_SIZE, 1*UNIT_SIZE, 0,
				
				1*UNIT_SIZE, 1*UNIT_SIZE, 0,
				-1*UNIT_SIZE, -1*UNIT_SIZE, 0,
				1*UNIT_SIZE, -1*UNIT_SIZE, 0
		};
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer = vbb.asFloatBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);
		
		float[] textures = new float[]
		{
				0,1,  0,0,  1,1,
				1,1,   0,0,  1,0
		};
		
		ByteBuffer tbb = ByteBuffer.allocateDirect(textures.length * 4);
		tbb.order(ByteOrder.nativeOrder());
		mTextureBuffer = tbb.asFloatBuffer();
		mTextureBuffer.put(textures);
		mTextureBuffer.position(0);
	}
	
	public void initShader(int mProgram){

		this.mProgram = mProgram;
		muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
		muCameraHandle = GLES20.glGetUniformLocation(mProgram, "uCamera");
		muTextureHandle = GLES20.glGetUniformLocation(mProgram, "sTexture");
		
		maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
		maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoor");
//		Log.w("texturerect-inishader", maTexCoorHandle+"------>"+maPositionHandle);
	}
	
	public void drawSelf(int texId){
		GLES20.glUseProgram(mProgram);
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
		GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, MatrixState.getMMatrix(), 0);
		GLES20.glUniform3fv(muCameraHandle, 1, MatrixState.cameraFB);
		
		GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3*4, mVertexBuffer);
		GLES20.glVertexAttribPointer(maTexCoorHandle, 2, GLES20.GL_FLOAT, false, 2*4, mTextureBuffer);
		
		GLES20.glEnableVertexAttribArray(maPositionHandle);
		GLES20.glEnableVertexAttribArray(maTexCoorHandle);
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
		GLES20.glUniform1i(muTextureHandle, 0);
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VCount);
	}
	
	
	
	
	
	
	
	
	
	
	
}
