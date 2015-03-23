package org.mshare.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.mshare.file.browser.FileBrowserFile;

import android.content.Context;
import android.widget.Toast;

public class MshareFileManage {
	//�ж��Ƿ����ļ���ѡ�и��ƻ����
	private boolean selected = false;
	private FileBrowserFile[] file = null;
	private Context context = null;
	private boolean cut = false;
	private int pasteCount = 0;
	
	public MshareFileManage(Context c) {
		context = c;
	}
	
	//��ȡcut��ֵ
	public boolean getCut() {
		return cut;
	}
	
	//��ȡselect��ֵ
	public boolean getSelect() {
		return selected;
	}
	
	//���Ƽ���ѡ��
	public void copySelect(FileBrowserFile[] f, boolean c) {
		selected = true;
		file = f;
		cut = c;
		Toast.makeText(context, "��ѡ��Ŀ���ļ���", Toast.LENGTH_SHORT).show();
	}
	
	//ȡ��ճ��
	public void pasteCancel() {
		selected = false;
		file = null;
	}
	
	//���ж���ļ�
	public void moveMultiFiles(String path) {
		int length = this.file.length;
		for(int i = 0; i < length; i++) {
			String oldPath = this.file[i].getAbsolutePath();
			String newPath = path + File.separator + this.file[i].getName();
			moveFile(oldPath, newPath);
		}
		if(this.pasteCount == length) {
			Toast.makeText(context, "�ļ�ȫ�����гɹ�", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(context, String.valueOf(length-this.pasteCount) + "���ļ����г���", Toast.LENGTH_SHORT).show();
		}
		this.pasteCount = 0;
		pasteCancel();
	}
	
	//����
	private void moveFile(String oldPath, String newPath) {
		if(new File(newPath).exists()) {
			Toast.makeText(context, "���г���", Toast.LENGTH_SHORT).show();
		}
		else {
			new File(oldPath).renameTo(new File(newPath));
			this.pasteCount++;
		}
	}
	
	//���ƶ���ļ�
	public void CopyMultiFiles(String path) {
		int length = this.file.length;
		for(int i = 0; i < length; i++) {
			String oldPath = this.file[i].getAbsolutePath();
			String newPath = path + File.separator + this.file[i].getName();
			pasteOfCopy(oldPath, newPath);
		}
		if(this.pasteCount == length) {
			Toast.makeText(context, "�ļ�ȫ�����Ƴɹ�", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(context, String.valueOf(length-this.pasteCount) + "���ļ����Ƴ���", Toast.LENGTH_SHORT).show();
		}
		this.pasteCount = 0;
		pasteCancel();
	}
	
	//ճ��-����
	private void pasteOfCopy(String fromPath, String toPath) {
		if(selected) {
			if(toPath.equals(fromPath) || new File(toPath).exists()) {
				Toast.makeText(context, "����ʧ��", Toast.LENGTH_SHORT).show();
			}
			else {
				if(copy(fromPath, toPath)) {
					this.pasteCount++;
				}
				else {
					Toast.makeText(context, "���Ƴ���", Toast.LENGTH_SHORT).show();
				}
			}
		}
		else {
			pasteCancel();
		}
	}
	
	//�����ļ�
	private boolean copy(String fromPath, String toPath) {
		File file = new File(fromPath);
		if(file.isFile()) {
			return copyFile(fromPath, toPath);
		}
		else {
			return copyFolder(fromPath, toPath);
		}
	}
	
	//���Ƶ����ļ�
	public boolean copyFile(String fromPath, String toPath) { 
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
	private boolean copyFolder(String fromPath, String toPath) { 
	   boolean isok = true;
       try { 
           (new File(fromPath)).mkdirs(); //����ļ��в����� �������ļ��� 
           File a=new File(fromPath); 
           (new File(toPath)).mkdirs();
           String[] file=a.list(); 
           File temp=null; 
           
           for (int i = 0; i < file.length; i++) { 
               if(fromPath.endsWith(File.separator)){ 
                   temp=new File(fromPath+file[i]); 
               } 
               else
               { 
                   temp=new File(fromPath+File.separator+file[i]); 
               } 

               if(temp.isFile()){ 
            	   
                   FileInputStream input = new FileInputStream(temp); 
                   FileOutputStream output = new FileOutputStream(toPath + "/" + 
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
                   copyFolder(fromPath+"/"+file[i],toPath+"/"+file[i]); 
               } 
           } 
       } 
       catch (Exception e) { 
    	   isok = false;
       } 
       return isok;
	}	
	
	//ɾ�������ļ����ļ���
	private boolean deleteAll(String path) {
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
	
	//ɾ���ļ����ļ���������
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
	
	//ɾ������ļ����ļ���
	public void deleteMultiFiles(FileBrowserFile[] f) {
		int length = f.length;
		for(int i = 0; i < length; i++) {
			deleteAll(f[i].getAbsolutePath());
		}
	}
	
	//�������ļ�
	public void renameFile(String oldPath, String newPath) {
		File file = new File(oldPath);
		file.renameTo(new File(newPath));
	}
	
	//�½��ļ���
	public void newFolder(String path) {
		(new File(path)).mkdirs();
	}
}
