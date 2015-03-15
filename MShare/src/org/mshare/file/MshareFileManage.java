package org.mshare.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.widget.Toast;

public class MshareFileManage {
	//�ж��Ƿ����ļ���ѡ�и��ƻ����
	private boolean selected = false;
	private String fromPath = null;
	private String toPath = null;
	private String name = null;
	private Context context = null;
	private boolean cut = false;
	
	//����context
	public void setContext(Context c) {
		context = c;
	}
	
	//��ȡselect��ֵ
	public boolean getSelected() {
		return selected;
	}
	
	//���Ƽ���ѡ��
	public void copySelect(String fp, String n, boolean c) {
		selected = true;
		fromPath = fp;
		name = n;
		cut = c;
		Toast.makeText(context, "��ѡ��Ŀ���ļ���", Toast.LENGTH_SHORT).show();
	}
	
	//ȡ��ճ��
	public void copyCancel() {
		selected = false;
		fromPath = null;
	}
	
	//ճ��
	public void paste(String tp) {
		if(selected) {
			toPath = tp + "/" + name;
			if(toPath.equals(fromPath)) {
				Toast.makeText(context, "ճ��ʧ��", Toast.LENGTH_SHORT).show();
			}
			else {
				if(copy()) {
					Toast.makeText(context, "ճ���ɹ�", Toast.LENGTH_SHORT).show();
					if(cut) {
						deleteAll(fromPath);
					}
					copyCancel();
				}
				else {
					Toast.makeText(context, "ճ������", Toast.LENGTH_SHORT).show();
				}
			}
		}
		else {
			copyCancel();
		}
	}
	
	//�����ļ�
	private boolean copy() {
		File file = new File(fromPath);
		if(file.isFile()) {
			return copyFile();
		}
		else {
			return copyFolder(fromPath, toPath);
		}
	}
	
	//���Ƶ����ļ�
	public boolean copyFile() { 
	   boolean isok = true;
       try { 
           int bytesum = 0; 
           int byteread = 0; 
           File oldfile = new File(fromPath); 
           if (oldfile.exists()) { //�ļ�������ʱ 
               InputStream inStream = new FileInputStream(fromPath); //����ԭ�ļ� 
               FileOutputStream fs = new FileOutputStream(toPath); 
               byte[] buffer = new byte[1024]; 
               int length; 
               while ( (byteread = inStream.read(buffer)) != -1) { 
                   bytesum += byteread; //�ֽ��� �ļ���С 
                   //System.out.println(bytesum); 
                   fs.write(buffer, 0, byteread); 
               } 
               fs.flush(); 
               fs.close(); 
               inStream.close(); 
           }
           else
           {
			isok = false;
		   }
       } 
       catch (Exception e) { 
          // System.out.println("���Ƶ����ļ���������"); 
          // e.printStackTrace(); 
           isok = false;
       } 
       return isok;
	} 
	
	//���������ļ���
	private boolean copyFolder(String oldPath, String newPath) { 
	   boolean isok = true;
       try { 
           (new File(oldPath)).mkdirs(); //����ļ��в����� �������ļ��� 
           File a=new File(oldPath); 
           (new File(newPath)).mkdirs();
           String[] file=a.list(); 
           File temp=null; 
           
           for (int i = 0; i < file.length; i++) { 
               if(oldPath.endsWith(File.separator)){ 
                   temp=new File(oldPath+file[i]); 
               } 
               else
               { 
                   temp=new File(oldPath+File.separator+file[i]); 
               } 

               if(temp.isFile()){ 
            	   
                   FileInputStream input = new FileInputStream(temp); 
                   FileOutputStream output = new FileOutputStream(newPath + "/" + 
                           (temp.getName()).toString()); 
                   
                   byte[] b = new byte[1024]; 
                   
                   int len; 
                   while ( (len = input.read(b)) != -1) { 
                       output.write(b, 0, len); 
                   }
                   output.close(); 
                   input.close(); 
                   
               } 
               if(temp.isDirectory()){//��������ļ��� 
                   copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i]); 
               } 
           } 
       } 
       catch (Exception e) { 
    	   isok = false;
       } 
       return isok;
	}	
	
	public boolean deleteAll(String path) {
		boolean isok = true;
		try {
			File file = new File(path);
			if(file.isDirectory()) {
				deleteFiles(path);//����ļ�������
			}
			file.delete();
		}
		catch(Exception e) {
			isok = false;
		}
		return isok;
	}
	
	private boolean deleteFiles(String path) {
		boolean isok = true;
		try {
			File f = new File(path);
			String[] files = f.list();
			for(int i = 0; i < files.length; i++) {
				File temp = new File(path + File.separator + files[i]);
				if(temp.isFile()) {
					temp.delete();
				}
				else {
					deleteAll(path + File.separator + files[i]);
				}
			}
		}
		catch(Exception e) {
			isok = false;
		}
		return isok;
	}
}
