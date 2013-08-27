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
	 * @param mv ��ͼ����
	 * @param mProgram ������shader����
	 * @param halfSize �߳���һ��
	 * @param colShape ��ײ��״������
	 * @param dynamicsWorld 
	 * @param mass
	 * @param cx ��ʼ��x����
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
		Vector3f localInertia = new Vector3f(0,0,0); //��������
		if (isDynamic) {
			colShape.calculateLocalInertia(mass, localInertia);//ͨ�������������������
		}
		Transform startTransform = new Transform();//����һ���任��
		startTransform.setIdentity();//��������
		startTransform.origin.set(new Vector3f(cx,cy,cz));//���ó�ʼλ��
		
		//�½�һ���˶�״̬
		DefaultMotionState myMotionState = new DefaultMotionState(startTransform);
		//����Ĺ��췽����
		RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, colShape, localInertia);
		body = new RigidBody(rbInfo);
		body.setRestitution(0.6f);
		body.setFriction(0.8f);
		dynamicsWorld.addRigidBody(body);//�Ѹ���������̬��������
		
		tr = new TextureRect(mProgram,halfSize);
	}
	
	public void drawSelf(int[] texIda){
		
		//��һ������Ҫ����������³�ʼ������ô���ĳ�����Զ���ʼ�ģ�
		//��������¼���Ķ�����Ϣ,Ҳ�����µ����Ӳ��ᱻ������
		tr.initShader(mProgram);//������γ�ʼ����ɫ��
		int texId = texIda[0];
		if (!body.isActive()) {//δ����ʱ�����þ�ֹʱ������
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
		
		//===========���������ĳ�ʼλ��������������ģ������ŵ���Ϊ������
		//����
		MatrixState.pushMatrix();
		MatrixState.translate(0, halfSize, 0);
		MatrixState.rotate(-90, 1, 0, 0);
		tr.drawSelf(texId);
		MatrixState.popMatrix();
		
		//����
		MatrixState.pushMatrix();
		MatrixState.translate(0, -halfSize, 0);
		MatrixState.rotate(90, 1, 0, 0);
		tr.drawSelf(texId);
		MatrixState.popMatrix();
		
		//����
		MatrixState.pushMatrix();
		MatrixState.translate(-halfSize, 0, 0);
		MatrixState.rotate(-90, 0, 1, 0);
		tr.drawSelf(texId);
		MatrixState.popMatrix();
		
		//����
		MatrixState.pushMatrix();
		MatrixState.translate(halfSize, 0, 0);
		MatrixState.rotate(90, 0, 1, 0);
		tr.drawSelf(texId);
		MatrixState.popMatrix();
		
		//ǰ�棬��Ϊ��ʼʱ����ǰ�棬���Բ�������ת
		MatrixState.pushMatrix();
		MatrixState.translate(0, 0, halfSize);
		tr.drawSelf(texId);
		MatrixState.popMatrix();
		
		//����
		MatrixState.pushMatrix();
		MatrixState.translate(0, 0, -halfSize);
		MatrixState.rotate(180, 0, 1, 0);
		tr.drawSelf(texId);
		MatrixState.popMatrix();
		
		MatrixState.popMatrix();
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}	
