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
import android.view.MotionEvent;
import android.view.SurfaceView;

import static com.example.my17_1.Constant.*;

public class MySurfaceView extends GLSurfaceView{

	private SceneRenderer mRenderer;
	DiscreteDynamicsWorld dynamicsWorld;
	ArrayList<TexCube> tca = new ArrayList<TexCube>();
	ArrayList<TexCube> tcaForAdd = new ArrayList<TexCube>();
	CollisionShape boxShape; //���õ�������
	CollisionShape planeShape; //���õ�ƽ����״
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
		
		int[] cubeTextureId = new int[2]; //��������
		int floorTextureId; //��������
		TexFloor floor;
		
		@Override
		public void onDrawFrame(GL10 arg0) {
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
			//������
//			synchronized (tca) {
//				for(TexCube tc : tca){
//					MatrixState.pushMatrix();
//					tc.drawSelf(cubeTextureId);
//					MatrixState.popMatrix();
//				}
//			}
			//���ذ�
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
			//������ɫ����
			ShaderManager.loadCodeFrmFile(mContext.getResources());
			ShaderManager.compileShader();	//������ɫ����
			
			//��ʼ������
			cubeTextureId[0] = ShaderUtil.initTexture(MySurfaceView.this, 0, R.drawable.wood_bin2);
			cubeTextureId[1] = ShaderUtil.initTexture(MySurfaceView.this, 0, R.drawable.wood_bin1);
			floorTextureId = ShaderUtil.initTexture(MySurfaceView.this, 1, R.drawable.f6);
			
			floor = new TexFloor(ShaderManager.getTextureShaderProgram(), 
					80*UNIT_SIZE, -UNIT_SIZE, planeShape, dynamicsWorld	);
			
			//0.4Ϊ�к���֮��Ŀ�϶��0.02Ϊÿ��֮��Ŀ�϶
			int size = 2;
			float xStart = (-size/2.0f + 0.5f)*(2 + 0.4f)*UNIT_SIZE;
			float yStart = 0.02f;
			float zStart = (-size/2.0f + 0.5f)*(2 + 0.4f)*UNIT_SIZE - 4f;
			
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
			
//			new Thread(){
//				public void run(){
//					while(true){
//						try {
//							synchronized (tcaForAdd) {
//								synchronized (tca) {
//									for (TexCube tc : tcaForAdd) {
//										tca.add(tc);
//									}
//								}
//								tcaForAdd.clear();
//							}
//							
//							dynamicsWorld.stepSimulation(TIME_STEP, MAX_SUB_STEPS);
//							Thread.sleep(20);
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//						
//					}
//				}
//			}.start();
		}
		
	}
	
	
	public void initWord(){
		//�洢������Ϣ
		CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
		//����������
		CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
		
		Vector3f worldAabbMin = new Vector3f(-10000,-10000,-10000);
		Vector3f worldAabbMax = new Vector3f(10000,10000,10000);
		int maxProxies = 1024;//��������Ŀ
		//������ײ���ֽ׶εļ����㷨����
		AxisSweep3 overlappingPairCache = new AxisSweep3(worldAabbMin, worldAabbMax, maxProxies);
		//�����ƶ�Լ������߶���
		SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
		//ʵ����һ����������
		dynamicsWorld = new DiscreteDynamicsWorld(
				dispatcher, overlappingPairCache, solver, collisionConfiguration);
		dynamicsWorld.setGravity(new Vector3f(0, -10, 0));
		//�������õ�������
		boxShape = new BoxShape(new Vector3f(Constant.UNIT_SIZE, Constant.UNIT_SIZE,Constant.UNIT_SIZE));
		//�������õ�ƽ����״
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
			}
			
			break;
		}
		
		
		return true;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
