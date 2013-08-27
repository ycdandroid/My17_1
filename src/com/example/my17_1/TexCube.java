package com.example.my17_1;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

public class TexCube {
	TextureRect tr;
	float halfSize;
	RigidBody body;
	int mProgram;
	MySurfaceView mv;
	
	/**
	 * 
	 * @param mv 视图对象
	 * @param mProgram 创建的shader程序
	 * @param halfSize 边长的一半
	 * @param colShape 碰撞形状抽象类
	 * @param dynamicsWorld 
	 * @param mass
	 * @param cx 初始的x坐标
	 * @param cy
	 * @param cz
	 */
	public TexCube(MySurfaceView mv, int mProgram, float halfSize, CollisionShape colShape,
			DiscreteDynamicsWorld dynamicsWorld, float mass, float cx,float cy, float cz)
	{
		this.mv = mv;
		this.mProgram = mProgram;
		this.halfSize = halfSize;
		
		boolean isDynamic = (mass != 0f);
		Vector3f localInertia = new Vector3f(0,0,0); //惯性向量
		if (isDynamic) {
			colShape.calculateLocalInertia(mass, localInertia);//通过质量计算出惯性向量
		}
		Transform startTransform = new Transform();//创建一个变换类
		startTransform.setIdentity();//变量重置
		startTransform.origin.set(new Vector3f(cx,cy,cz));//设置初始位置
		
		//新建一个运动状态
		DefaultMotionState myMotionState = new DefaultMotionState(startTransform);
		//刚体的构造方法类
		RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, colShape, localInertia);
		body = new RigidBody(rbInfo);
		body.setRestitution(0.6f);
		body.setFriction(0.8f);
		dynamicsWorld.addRigidBody(body);//把刚体加入进动态物理世界
		
		tr = new TextureRect(mProgram,halfSize);
	}
	
	public void drawSelf(int[] texIda){
		
		//这一步很重要，如果不重新初始化，那么它的程序永远是最开始的，
		//不会包含新加入的顶点信息,也就是新的箱子不会被画出来
		tr.initShader(mProgram);//纹理矩形初始化着色器
		int texId = texIda[0];
		if (!body.isActive()) {//未激活时，采用静止时的纹理
			texId = texIda[1];
		}
		
		MatrixState.pushMatrix();
		Transform trans = body.getMotionState().getWorldTransform(new Transform());
		MatrixState.translate(trans.origin.x, trans.origin.y, trans.origin.z);
		Quat4f ro = trans.getRotation(new Quat4f());
		if (ro.x != 0 || ro.y !=0 || ro.z != 0) {
			float[] fa = SYSUtil.fromSYStoAXYZ(ro);
			MatrixState.rotate(fa[0], fa[1], fa[2], fa[3]);
		}
		
		//===========立方体的面的初始位置在立方体的中心，正对着的面为正方向
		//上面
		MatrixState.pushMatrix();
		MatrixState.translate(0, halfSize, 0);
		MatrixState.rotate(-90, 1, 0, 0);
		tr.drawSelf(texId);
		MatrixState.popMatrix();
		
		//下面
		MatrixState.pushMatrix();
		MatrixState.translate(0, -halfSize, 0);
		MatrixState.rotate(90, 1, 0, 0);
		tr.drawSelf(texId);
		MatrixState.popMatrix();
		
		//左面
		MatrixState.pushMatrix();
		MatrixState.translate(-halfSize, 0, 0);
		MatrixState.rotate(-90, 0, 1, 0);
		tr.drawSelf(texId);
		MatrixState.popMatrix();
		
		//右面
		MatrixState.pushMatrix();
		MatrixState.translate(halfSize, 0, 0);
		MatrixState.rotate(90, 0, 1, 0);
		tr.drawSelf(texId);
		MatrixState.popMatrix();
		
		//前面，因为初始时就是前面，所以不用在旋转
		MatrixState.pushMatrix();
		MatrixState.translate(0, 0, halfSize);
		tr.drawSelf(texId);
		MatrixState.popMatrix();
		
		//后面
		MatrixState.pushMatrix();
		MatrixState.translate(0, 0, -halfSize);
		MatrixState.rotate(180, 0, 1, 0);
		tr.drawSelf(texId);
		MatrixState.popMatrix();
		
		MatrixState.popMatrix();
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}	
