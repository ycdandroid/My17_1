package com.example.my17_1;

import java.util.ArrayList;
import java.util.Iterator;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;


import static com.example.my17_1.Constant.*;

public class MySurfaceView extends GLSurfaceView{

	private SceneRenderer mRenderer;
	DiscreteDynamicsWorld dynamicsWorld;
	ArrayList<TexCube> tca = new ArrayList<TexCube>();
	ArrayList<TexCube> tcaForAdd = new ArrayList<TexCube>();
	CollisionShape boxShape; //共用的立方体
	CollisionShape planeShape; //共用的平面形状
	Context mContext;
	
	public MySurfaceView(Context context) {
		super(context);
		this.mContext = context;
		this.setEGLContextClientVersion(2);
		initWord();
		mRenderer = new SceneRenderer();
		this.setRenderer(mRenderer);
		this.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	}
	
	
	class SceneRenderer implements GLSurfaceView.Renderer{
		
		int[] cubeTextureId = new int[2]; //箱子纹理
		int floorTextureId; //地面纹理
		TexFloor floor;
		
		@Override
		public void onDrawFrame(GL10 arg0) {
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//			画箱子
			synchronized (tca) {
				for(TexCube tc : tca){
					MatrixState.pushMatrix();
					tc.drawSelf(cubeTextureId);
					MatrixState.popMatrix();
				}
			}
//			Log.i("ondrawfram", tca.size()+"=================");
			//画地板
			MatrixState.pushMatrix();
			floor.drawSelf(floorTextureId);
			MatrixState.popMatrix();
		}

		@Override
		public void onSurfaceChanged(GL10 arg0, int width, int height) {
			GLES20.glViewport(0, 0, width, height);
			float ratio =  (float)width/height;
			MatrixState.setProjectFrustum(-ratio, ratio, -1, 1, 2, 100);
		}

		@Override
		public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
			
			GLES20.glClearColor(0, 0, 0, 0);
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			GLES20.glEnable(GLES20.GL_CULL_FACE);
			MatrixState.setInitStack();
			MatrixState.setCamera(
					EYE_X, EYE_Y, EYE_Z, TARGET_X, TARGET_Y, TARGET_Z, 0, 1, 0);
			//加载着色程序
			ShaderManager.loadCodeFromFile(mContext.getResources());
			ShaderManager.compileShader();	//编译着色程序
			
			//初始化纹理
			cubeTextureId[0] = ShaderUtil.initTexture(MySurfaceView.this, 0, R.drawable.wood_bin2);
			cubeTextureId[1] = ShaderUtil.initTexture(MySurfaceView.this, 0, R.drawable.wood_bin1);
			floorTextureId = ShaderUtil.initTexture(MySurfaceView.this, 1, R.drawable.f6);
			
			floor = new TexFloor(ShaderManager.getTextureShaderProgram(), 
					80*UNIT_SIZE, -UNIT_SIZE, planeShape, dynamicsWorld	);
			
			//0.4为行和列之间的空隙，0.02为每层之间的空隙
			int size = 2;
			float xStart = (-size/2.0f + 0.5f)*(2 + 0.4f)*UNIT_SIZE;
			float yStart = 0.02f;
			float zStart = (-size/2.0f + 0.5f)*(2 + 0.4f)*UNIT_SIZE - 4f;
			
			//初始的8个箱子
			for(int i=0; i<size; i++){
				for (int j = 0; j < size; j++) {
					for (int k = 0; k < size; k++) {
						
						TexCube tcTemp = new TexCube
						(
							MySurfaceView.this, 
							ShaderManager.getTextureShaderProgram(),
							UNIT_SIZE, boxShape, 
							dynamicsWorld, 
							1, 
							xStart + i*(2 + 0.4f)*UNIT_SIZE, 
							yStart + j*(2 + 0.02f)*UNIT_SIZE,
							zStart + k*(2 + 0.4f)*UNIT_SIZE
						);
						tca.add(tcTemp);
						tcTemp.body.forceActivationState(RigidBody.WANTS_DEACTIVATION);
					}
				}
			}//end of for
			
			new Thread(){
				public void run(){
					while(true){
						try {
							synchronized (tcaForAdd) {
								synchronized (tca) {
									for (TexCube tc : tcaForAdd) {
										tca.add(tc);
									}
								}
								tcaForAdd.clear();
							}
//							Log.i("onsurfacecreate--num", tca.size()+"======");
							dynamicsWorld.stepSimulation(TIME_STEP, MAX_SUB_STEPS);
							Thread.sleep(20);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
				}
			}.start();
		}
		
	}
	
	
	public void initWord(){
		//存储配置信息
		CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
		//创建分配器
		CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
		
		Vector3f worldAabbMin = new Vector3f(-10000,-10000,-10000);
		Vector3f worldAabbMax = new Vector3f(10000,10000,10000);
		int maxProxies = 1024;//最大代理数目
		//创建碰撞检测粗阶段的加速算法对象
		AxisSweep3 overlappingPairCache = new AxisSweep3(worldAabbMin, worldAabbMax, maxProxies);
		//创建推动约束解决者对象
		SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
		//实例化一个物理世界
		dynamicsWorld = new DiscreteDynamicsWorld(
				dispatcher, overlappingPairCache, solver, collisionConfiguration);
		dynamicsWorld.setGravity(new Vector3f(0, -10, 0));
		//创建共用的立方体
		boxShape = new BoxShape(new Vector3f(Constant.UNIT_SIZE, Constant.UNIT_SIZE,Constant.UNIT_SIZE));
		//创建共用的平面形状
		planeShape = new StaticPlaneShape(new Vector3f(0,1,0), 0);
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e){
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			TexCube tcTemp = new TexCube
			(
				MySurfaceView.this, 
				ShaderManager.getTextureShaderProgram(), 
				UNIT_SIZE, 
				boxShape, 
				dynamicsWorld, 
				1, 
				0, 2, 4
			);
			tcTemp.body.setLinearVelocity(new Vector3f(0,2,-12));
			tcTemp.body.setAngularVelocity(new Vector3f(0,0,0));
			synchronized (tcaForAdd) {
				tcaForAdd.add(tcTemp);
//				Log.i("on-touch-num", tcaForAdd.size()+"/");
			}
			
			break;
		}
		
		
		return true;
	}

	
	
	
	
	
	
//	private class SceneRenderer implements GLSurfaceView.Renderer 
//    {
//		int[] cubeTextureId=new int[2];//箱子面纹理
//		int floorTextureId;//地面纹理
//		TexFloor floor;//纹理矩形1		
//		
//        public void onDrawFrame(GL10 gl) { 
//        	//清除颜色缓存于深度缓存
//        	GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);            
//            //绘制箱子
//            synchronized(tca)
//			{
//	            for(TexCube tc:tca)
//	            {
//	            	MatrixState.pushMatrix();
//	                tc.drawSelf(cubeTextureId); 
//	                MatrixState.popMatrix();         
//	            }            
//			}
//            
//            //绘制地板
//            MatrixState.pushMatrix();
//            floor.drawSelf( floorTextureId);
//            MatrixState.popMatrix();         
//        }
//
//        public void onSurfaceChanged(GL10 gl, int width, int height) {
//            //设置视窗大小及位置 
//        	GLES20.glViewport(0, 0, width, height);
//            //计算透视投影的比例
//            float ratio = (float) width / height;
//            //调用此方法计算产生透视投影矩阵
//            MatrixState.setProjectFrustum(-ratio, ratio, -1, 1, 2, 100);
//            
//        }
//
//        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//            //设置屏幕背景色黑色RGBA
//            GLES20.glClearColor(0,0,0,0);            
//            //启用深度测试
//            GLES20.glEnable(GL10.GL_DEPTH_TEST);  
//            //设置为打开背面剪裁
//            GLES20.glEnable(GL10.GL_CULL_FACE);
//            //初始化变换矩阵
//            MatrixState.setInitStack();
//            MatrixState.setCamera( 
//            		EYE_X,   //人眼位置的X
//            		EYE_Y, 	//人眼位置的Y
//            		EYE_Z,   //人眼位置的Z
//            		TARGET_X, 	//人眼球看的点X
//            		TARGET_Y,   //人眼球看的点Y
//            		TARGET_Z,   //人眼球看的点Z
//            		0, 
//            		1, 
//            		0);
//            //初始化所用到的shader程序
//            ShaderManager.loadCodeFromFile(mContext.getResources());
//            ShaderManager.compileShader();
//            //初始化纹理
//            cubeTextureId[0]=ShaderUtil.initTexture(MySurfaceView.this,0,R.drawable.wood_bin2);
//            cubeTextureId[1]=ShaderUtil.initTexture(MySurfaceView.this, 0, R.drawable.wood_bin1);
//            floorTextureId=ShaderUtil.initTexture(MySurfaceView.this, 1, R.drawable.f6);            
//            
//            //创建地面矩形
//            floor=new TexFloor(ShaderManager.getTextureShaderProgram(),80*Constant.UNIT_SIZE,-Constant.UNIT_SIZE,planeShape,dynamicsWorld);
//           
//            //创建立方体       
//            int size=2;   //立方体尺寸
//            float xStart=(-size/2.0f+0.5f)*(2+0.4f)*Constant.UNIT_SIZE;//x坐标起始值
//            float yStart=0.02f;//y坐标起始值
//            float zStart=(-size/2.0f+0.5f)*(2+0.4f)*Constant.UNIT_SIZE-4f;//z坐标起始值
//            for(int i=0;i<size;i++)
//            {
//            	for(int j=0;j<size;j++)
//            	{
//            		for(int k=0;k<size;k++)
//            		{
//            			TexCube tcTemp=new TexCube       //创建纹理立方体
//            			(
//            					MySurfaceView.this,		//MySurfaceView的引用
//            					ShaderManager.getTextureShaderProgram(),//着色器程序引用
//                				Constant.UNIT_SIZE,		//尺寸
//                				boxShape,				//碰撞形状
//                				dynamicsWorld,			//物理世界
//                				1,						//刚体质量		
//                				xStart+i*(2+0.4f)*Constant.UNIT_SIZE,//起始x坐标
//                				yStart+j*(2.02f)*Constant.UNIT_SIZE, //起始y坐标        
//                				zStart+k*(2+0.4f)*Constant.UNIT_SIZE//起始z坐标
//                		);            			
//            			tca.add(tcTemp);
//            			//使得立方体一开始是不激活的
//            			tcTemp.body.forceActivationState(RigidBody.WANTS_DEACTIVATION);
//            		}
//            	}
//            }
//            
//            new Thread()
//            {
//            	public void run()
//            	{
//            		while(true)
//            		{            			
//            			try 
//            			{
//            				synchronized(tcaForAdd)//锁定新箱子所在集合
//            	            {
//            					synchronized(tca)//锁定当前箱子的集合
//            					{
//            						for(TexCube tc:tcaForAdd)
//                	                {
//                	            		tca.add(tc);  //向箱子集合中添加箱子
//                	                }
//            					}            	            	
//            	            	tcaForAdd.clear();		//将新箱子的集合清空
//            	            }           
//            				//开始模拟
//                			dynamicsWorld.stepSimulation(TIME_STEP, MAX_SUB_STEPS);
//							Thread.sleep(20);	//当前线程睡眠20毫秒
//						} catch (Exception e) 
//						{
//							e.printStackTrace();
//						}
//            		}
//            	}
//            }.start();					//启动线程
//        }
//    }
//	
//	//触摸事件回调方法
//    @Override public boolean onTouchEvent(MotionEvent e) 
//    {
//        switch (e.getAction()) 
//        {
//           case MotionEvent.ACTION_DOWN:			//处理屏幕被按下的事件
//        	TexCube tcTemp=new TexCube				//创建一个纹理立方体
//   			(
//   					this,							//MySurfaceView的引用
//   					ShaderManager.getTextureShaderProgram(),//着色器程序引用
//       				UNIT_SIZE,				//尺寸
//       				boxShape,						//碰撞形状
//       				dynamicsWorld,					//物理世界
//       				1,								//刚体质量
//       				0,								//起始x坐标
//       				2,         						//起始y坐标 
//       				4								//起始z坐标
//       				
//       		);        
//        	//设置箱子的初始速度
//        	tcTemp.body.setLinearVelocity(new Vector3f(0,2,-12));//箱子直线运动的速度--Vx,Vy,Vz三个分量
//        	tcTemp.body.setAngularVelocity(new Vector3f(0,0,0)); //箱子自身旋转的速度--绕箱子自身的x,y,x三轴旋转的速度
//        	//将新立方体加入到列表中
//        	synchronized(tcaForAdd)//锁定集合
//            {
//        	   tcaForAdd.add(tcTemp);//添加箱子
//            }
//           break;
//        }
//        return true;
//    }	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
